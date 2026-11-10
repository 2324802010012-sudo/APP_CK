"""Copy APP_CK's local database and imported images between ADB devices.

Requires the same current debug APK on both devices, opened at least once.
Without --apply this only checks access and displays the proposed transfer.
"""

import argparse
from datetime import datetime
from pathlib import Path
import sqlite3
import subprocess
import tarfile
import tempfile


PACKAGE = "com.example.app_ck"
DATABASE = "du_lich_tai_khoan.db"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", required=True, help="Source ADB serial")
    parser.add_argument("--target", required=True, help="Destination ADB serial")
    parser.add_argument("--adb", default=r"D:\Android\Sdk\platform-tools\adb.exe")
    parser.add_argument("--apply", action="store_true", help="Replace destination data after backups")
    args = parser.parse_args()
    if args.source == args.target:
        parser.error("Source and target must be different devices.")

    def adb(serial, *command, **kwargs):
        return subprocess.run([args.adb, "-s", serial, *command], check=True, **kwargs)

    def app(serial, *command, **kwargs):
        return adb(serial, "shell", "run-as", PACKAGE, *command, **kwargs)

    # Read-only preflight: run-as also verifies that the APK is debuggable.
    for serial in (args.source, args.target):
        adb(serial, "get-state", stdout=subprocess.PIPE)
        app(serial, "test", "-f", f"databases/{DATABASE}", stdout=subprocess.PIPE)

    print(f"Source: {args.source} -> target: {args.target}")
    print("Copies SQLite (including sidecars) and imported images; resets target login.")
    print("Destination data is replaced, not merged. Both apps must use the same debug APK.")
    if not args.apply:
        print("Checks passed. Add --apply to perform the transfer and create backups.")
        return

    stamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
    backup_dir = Path(__file__).resolve().parents[1] / ".local-backups" / "sqlite-transfer" / stamp
    backup_dir.mkdir(parents=True, exist_ok=False)

    def snapshot(serial, filename):
        # Stopping the app prevents concurrent writes; include WAL/journal files.
        adb(serial, "shell", "am", "force-stop", PACKAGE)
        app(serial, "mkdir", "-p", "files/anh_dia_diem")
        with filename.open("wb") as stream:
            adb(serial, "exec-out", "run-as", PACKAGE, "tar", "-cf", "-",
                "databases", "files/anh_dia_diem", stdout=stream)

    def database_version(filename):
        with tempfile.TemporaryDirectory() as folder, tarfile.open(filename) as archive:
            for suffix in ("", "-wal", "-shm", "-journal"):
                name = DATABASE + suffix
                try:
                    member = archive.getmember("databases/" + name)
                except KeyError:
                    if suffix == "":
                        raise RuntimeError("Database missing from backup.")
                    continue
                if not member.isfile():
                    raise RuntimeError("Invalid database entry in backup.")
                with archive.extractfile(member) as source:
                    (Path(folder) / name).write_bytes(source.read())
            connection = sqlite3.connect(str(Path(folder) / DATABASE))
            try:
                if connection.execute("PRAGMA quick_check").fetchall() != [("ok",)]:
                    raise RuntimeError("SQLite integrity check failed; transfer cancelled.")
                return connection.execute("PRAGMA user_version").fetchone()[0]
            finally:
                connection.close()

    source_file = backup_dir / "source.tar"
    target_file = backup_dir / "target-before-transfer.tar"
    snapshot(args.source, source_file)
    snapshot(args.target, target_file)
    print(f"Backups: {backup_dir}")
    source_version = database_version(source_file)
    target_version = database_version(target_file)
    if source_version != target_version:
        raise RuntimeError(f"DB versions differ ({source_version} vs {target_version}); transfer cancelled.")

    remote_backup = f"sqlite_transfer_backups/{stamp}"
    app(args.target, "mkdir", "-p", remote_backup)
    app(args.target, "mv", "databases", remote_backup + "/databases")
    moved_preferences = []
    try:
        # Do not let an old target account ID refer to another imported account.
        for suffix in (".xml", ".xml.bak"):
            filename = "phien_dang_nhap" + suffix
            relative = "shared_prefs/" + filename
            exists = subprocess.run(
                [args.adb, "-s", args.target, "shell", "run-as", PACKAGE, "test", "-f", relative],
                stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            )
            if exists.returncode == 0:
                app(args.target, "mv", relative, remote_backup + "/" + filename)
                moved_preferences.append(filename)
        with source_file.open("rb") as stream:
            adb(args.target, "exec-in", "run-as", PACKAGE, "tar", "-xf", "-", stdin=stream)
    except Exception:
        print(f"Transfer failed. Original target DB remains in {PACKAGE}/{remote_backup}/databases")
        # Restore the original snapshot and session when the device is reachable.
        try:
            app(args.target, "mkdir", "-p", "databases")
            app(args.target, "mv", "databases", remote_backup + "/failed-import")
            app(args.target, "mv", remote_backup + "/databases", "databases")
            with target_file.open("rb") as stream:
                adb(args.target, "exec-in", "run-as", PACKAGE, "tar", "-xf", "-", stdin=stream)
            for filename in moved_preferences:
                app(args.target, "mv", remote_backup + "/" + filename, "shared_prefs/" + filename)
            print("Original target data restored.")
        except Exception as error:
            print(f"Automatic restore failed: {error}. Keep the backups for recovery.")
        raise

    print(f"Transfer complete (SQLite v{source_version}). Reopen the target app and log in again.")
    print(f"Original target DB also retained under app-private {remote_backup}/databases.")


if __name__ == "__main__":
    try:
        main()
    except (subprocess.CalledProcessError, OSError, RuntimeError, sqlite3.Error, tarfile.TarError) as error:
        raise SystemExit(f"Transfer stopped: {error}")

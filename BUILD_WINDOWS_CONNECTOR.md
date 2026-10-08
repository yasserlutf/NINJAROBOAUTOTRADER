# Build the Windows installer

The Windows setup program is built on a Windows runner because PyInstaller packages for the operating system it runs on. The repository workflow builds a standalone connector executable, checks that its MT5 and NumPy imports work, wraps it in an Inno Setup installer, then publishes `NinjaRoboConnectorSetup.exe` as a downloadable GitHub Actions artifact.

## Produce the installer

1. Push the connector source and `.github/workflows/build-windows-connector.yml` to the repository's `main` branch, or start **Build Windows connector installer** from the GitHub Actions tab.
2. Wait for the workflow to finish successfully.
3. Open that workflow run, download the `NinjaRoboForex-Windows-Installer` artifact, and extract it. The extracted file is `NinjaRoboConnectorSetup.exe`.
4. Copy that installer to the Windows computer or VMware Fusion Windows VM and double-click it.

The installer installs the connector under the current Windows user (no administrator prompt), offers automatic startup at sign-in, and starts the connector after setup. It supports x64 Windows and Windows 11 on Arm through x64 emulation. On first launch it asks for the Ninja Robo app email and password. It does not ask for MT5's trading password. The bridge attaches to the MT5 terminal already running on that Windows computer and only syncs account telemetry; it does not place trades.

This build is unsigned. Windows may show a SmartScreen warning until the executable is signed with a code-signing certificate. The workflow artifact is for installation/testing and expires after 30 days; it is not yet a public download page or auto-updater.

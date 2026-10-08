#define AppName "Ninja Robo Forex Connector"
#define AppVersion "1.0.2"
#define AppPublisher "Ninja Robo Forex"

[Setup]
AppId={{A2AB6701-ED8D-4CB8-A7E5-5A7E648D862B}}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
DefaultDirName={localappdata}\Programs\Ninja Robo Forex Connector
DefaultGroupName={#AppName}
PrivilegesRequired=lowest
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
OutputBaseFilename=NinjaRoboConnectorSetup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
UninstallDisplayIcon={app}\NinjaRoboConnector.exe

[Tasks]
Name: "startup"; Description: "Start the connector automatically when I sign in to Windows"; Flags: checkedonce

[Files]
Source: "..\dist\NinjaRoboConnector.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\bridge\README.md"; DestDir: "{app}"; DestName: "Connector guide.txt"; Flags: ignoreversion

[Icons]
Name: "{group}\Ninja Robo MT5 Connector"; Filename: "{app}\NinjaRoboConnector.exe"; WorkingDir: "{app}"
Name: "{userstartup}\Ninja Robo MT5 Connector"; Filename: "{app}\NinjaRoboConnector.exe"; WorkingDir: "{app}"; Tasks: startup

[Run]
Filename: "{app}\NinjaRoboConnector.exe"; Description: "Start Ninja Robo MT5 Connector"; WorkingDir: "{app}"; Flags: postinstall nowait skipifsilent

[UninstallDelete]
Type: files; Name: "{localappdata}\NinjaRoboForex\firebase-session.bin"

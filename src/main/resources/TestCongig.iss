[Setup]
AppName=TestJpa
AppVersion=1.0
DefaultDirName={pf}\test
OutputDir=.
OutputBaseFilename=testInstaller
Compression=lzma
SolidCompression=yes

[Files]
Source: "C:\JAVAPROGRAM_TEST\testJpe2.exe"; DestDir: "{app}"
Source: "C:\JAVAPROGRAM_TEST\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs

[Run]
Filename: "{app}\testJpe2.exe"; Description: "Запустить test"; Flags: nowait postinstall

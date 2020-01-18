@echo off

SET MOVETO=TestServer\HexaCord v246\plugins
SET path=%M2_HOME%\bin;%SystemRoot%\system32;C:\Program Files\Java\jdk1.8.0_201\bin

call mvn install

rem jarファイルの移動
move /y "target\ServerManager*.jar" "%MOVETO%"

rem サーバーの起動
cd /d %MOVETO%\..\
java -Xms512M -Xmx512M -jar BungeeCord.jar

exit
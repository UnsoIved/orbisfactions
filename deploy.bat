@ECHO OFF
SET SERVER_DIR=D:\MCThings\servers\pure-vanilla-1.14.4

CALL mvn clean package

DEL %SERVER_DIR%\plugins\OrbisFactions-*.jar
COPY .\target\OrbisFactions-1.0-SNAPSHOT.jar %SERVER_DIR%\plugins
PAUSE

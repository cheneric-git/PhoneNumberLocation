@echo off
setlocal
set curpath=%cd%
set genname=%curpath%\build.xml
echo ^<?xml version="1.0" encoding="UTF-8"?^>>%genname%
echo ^<project name="build" default="help"^>>>%genname%

echo    ^<property file="local.properties" /^>>>%genname%

echo    ^<property file="build.properties" /^>>>%genname%

echo    ^<property file="default.properties" /^>>>%genname%

echo    ^<path id="android.antlibs"^>>>%genname%
echo        ^<pathelement path="${sdk.dir}/tools/lib/anttasks.jar" /^>>>%genname%
echo        ^<pathelement path="${sdk.dir}/tools/lib/sdklib.jar" /^>>>%genname%
echo        ^<pathelement path="${sdk.dir}/tools/lib/androidprefs.jar" /^>>>%genname%
echo    ^</path^>>>%genname%

echo    ^<taskdef name="setup">>%genname%
echo        classname="com.android.ant.SetupTask">>%genname%
echo        classpathref="android.antlibs" /^>>>%genname%
echo    ^<setup /^>>>%genname%
echo ^</project^>>>%genname%
endlocal
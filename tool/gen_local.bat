@echo off
setlocal
set curpath=%cd%
set genname=%curpath%\local.properties
if DEFINED ANDROID_HOME (
set "var=%ANDROID_HOME:\=\\%"
) else (
set var=C:\\android-sdk-windows
)
echo sdk.dir=%var%>%genname%
endlocal

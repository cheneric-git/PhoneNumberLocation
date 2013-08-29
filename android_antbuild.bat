@echo off
type %1
setlocal
set curpath=%CD%
if NOT DEFINED ANDROID_HOME (
set ANT_HOME=C:\apache-ant-1.8.2
) 
@rem if NOT DEFINED JAVA_HOME (
@rem set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_17
@rem )
@rem set CLASSPATH=.;%JAVA_HOME%\lib
@rem set path=%ANT_HOME%\bin;%JAVA_HOME%\bin;%path%

rd bin /S /q
rd gen /S /q
@rem rd .\res\drawable /S /q
@rem rd .\res\drawable-hdpi /S /q
@rem rd .\res\drawable-ldpi /S /q
@rem rd .\res\drawable-mdpi /S /q
md bin
md gen
md .\bin\release

call .\tool\7za.exe x -y .\img\%1.zip -o.\res\

call TYNUMLOCATION_VerNumber.bat
call .\tool\gen_version.bat>.\bin\build.log
@rem call .\tool\changexmlversionnum %curpath%\AndroidManifest.xml %vernum%>>.\bin\build.log
call java -cp .\tool\jdom.jar;.\tool\ChangeApkVersion.jar ChangeApkVersion %curpath%\AndroidManifest.xml %vernum%>>.\bin\build.log
call .\tool\gen_local.bat>>.\bin\build.log
call .\tool\gen_antbuild.bat>>.\bin\build.log
call .\tool\parse_utf8>>.\bin\build.log
call ant release>>.\bin\build.log
copy .\tool\devAndroid.keystore .\bin
cd bin
call jarsigner -storepass dev2011 -verbose -keystore devAndroid.keystore -signedjar TYNumLocation.apk build-unsigned.apk devAndroid.keystore>>build.log
move TYNumLocation.apk .\release
endlocal
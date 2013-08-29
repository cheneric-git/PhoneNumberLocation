@echo off
setlocal
type %1
type %2
set curpath=%cd%
set genname=%curpath%\build.xml

echo ^<?xml version="1.0"?^>>%genname%
echo ^<project name="K-Market" default="compile"^>>>%genname%

echo ^<property name="source.dir" value="src" /^>>>%genname%
echo ^<property name="source.absolute.dir" location="${source.dir}" /^>>>%genname%
echo ^<property name="gen.dir" value="gen" /^>>>%genname%
echo ^<property name="gen.absolute.dir" location="${gen.dir}" /^>>>%genname%
echo ^<property name="resource.dir" value="res" /^>>>%genname%
echo ^<property name="resource.absolute.dir" location="${resource.dir}" /^>>>%genname%
echo ^<property name="external.libs.dir" value="libs" /^>>>%genname%
echo ^<property name="external.libs.absolute.dir" location="${external.libs.dir}" /^>>>%genname%
echo ^<property name="native.libs.dir" value="libs" /^>>>%genname%
echo ^<property name="native.libs.absolute.dir" location="${native.libs.dir}" /^>>>%genname%
echo ^<property name="out.dir" value="bin" /^>>>%genname%
echo ^<property name="out.absolute.dir" location="${out.dir}" /^>>>%genname%
echo ^<property name="out.dir" value="bin" /^>>>%genname%
echo ^<property name="out.absolute.dir" location="${out.dir}" /^>>>%genname%
echo ^<property name="out.classes.dir" value="${out.absolute.dir}/classes" /^>>>%genname%
echo ^<property name="out.classes.absolute.dir" location="${out.classes.dir}" /^>>>%genname%
echo ^<property name="android.libraries.jars" value="C:\android-sdk-windows\platforms\android-8\android.jar"/^>>>%genname%


echo     ^<target name="-dirs"^>>>%genname%
echo        ^<echo>Creating output directories if needed...^</echo^>
echo        ^<mkdir dir="${resource.absolute.dir}" /^>>>%genname%
echo        ^<mkdir dir="${external.libs.absolute.dir}" /^>>>%genname%
echo        ^<mkdir dir="${gen.absolute.dir}" /^>>>%genname%
echo        ^<mkdir dir="${out.absolute.dir}" /^>>>%genname%
echo        ^<mkdir dir="${out.classes.absolute.dir}" /^>>>%genname%
echo    ^</target^>>>%genname%

echo     ^<target name="-aidl" depends="-dirs"^>>>%genname%
echo        ^<echo>Compiling aidl files into Java classes...^</echo^>
echo        ^<apply executable="${aidl}" failonerror="true"^>>>%genname%
echo            ^<arg value="-p${android.aidl}" /^>>>%genname%
echo            ^<arg value="-I${source.absolute.dir}" /^>>>%genname%
echo            ^<arg value="-o${gen.absolute.dir}" /^>>>%genname%
echo            ^<fileset dir="${source.absolute.dir}"^>>>%genname%
echo                ^<include name="**/*.aidl" /^>>>%genname%
echo            ^</fileset^>>>%genname%
echo        ^</apply^>>>%genname%
echo    ^</target^>>>%genname%

echo ^<target name="compile" depends="-aidl" description="Compiles project's .java files into .class files"^>>>%genname%
echo   ^<condition property="extensible.classpath" value="${tested.project.absolute.dir}/bin/classes" else="."^>>>%genname%
echo     ^<isset property="tested.project.absolute.dir" /^>>>%genname%
echo   ^</condition^>>>%genname%
echo   ^<condition property="extensible.libs.classpath" value="${tested.project.absolute.dir}/libs" else="./libs"^>>>%genname%
echo     ^<isset property="tested.project.absolute.dir" /^>>>%genname%
echo   ^</condition^>>>%genname%
echo     ^<javac encoding="ascii" target="1.5" debug="true" extdirs="">>%genname%
echo                 destdir="${out.classes.absolute.dir}">>%genname%
echo                 bootclasspathref="android.target.classpath">>%genname%
echo                 verbose="${verbose}" classpath="${extensible.classpath}">>%genname%
echo                 classpathref="android.libraries.jars"^>>>%genname%
echo           ^<src path="${source.absolute.dir}" /^>>>%genname%
echo           ^<src path="${gen.absolute.dir}" /^>>>%genname%
echo           ^<src refid="android.libraries.src" /^>>>%genname%
echo           ^<classpath^>>>%genname%
echo               ^<fileset dir="${external.libs.absolute.dir}" includes="*.jar" /^>>>%genname%
echo               ^<fileset dir="${extensible.libs.classpath}" includes="*.jar" /^>>>%genname%
echo           ^</classpath^>>>%genname%
echo      ^</javac^>>>%genname%
echo ^</target^>>>%genname%

echo ^</project^>>>%genname%
endlocal
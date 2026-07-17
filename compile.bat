@echo off
title Compile Silicon Valley Catan
set JAVA_HOME=C:\Users\silkroadnb\Downloads\jdk17-extracted\jdk-17.0.19+10
set JAVAC=%JAVA_HOME%\bin\javac.exe
set SRC=C:\Users\silkroadnb\Downloads\SiliconValley-Catan-master (1)\SiliconValley-Catan-master\src
set OUT=C:\Users\silkroadnb\Downloads\SiliconValley-Catan-master (1)\SiliconValley-Catan-master\out
set JFX=C:\Users\silkroadnb\Downloads\javafx-sdk\javafx-sdk-21.0.11\lib

echo Compiling...
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

"%JAVAC%" -encoding UTF-8 -d "%OUT%" -sourcepath "%SRC%" --module-path "%JFX%" --add-modules javafx.controls,javafx.fxml "%SRC%\*.java" "%SRC%\model\*.java" "%SRC%\controller\*.java" "%SRC%\view\*.java" "%SRC%\exception\*.java" "%SRC%\util\*.java"

if %ERRORLEVEL%==0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)
pause

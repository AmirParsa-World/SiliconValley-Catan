@echo off
title Silicon Valley Catan
set JAVA=C:\Users\silkroadnb\Downloads\jdk17-extracted\jdk-17.0.19+10\bin\java.exe
set JFX=C:\Users\silkroadnb\Downloads\javafx-sdk\javafx-sdk-21.0.11\lib
set OUT=C:\Users\silkroadnb\Downloads\SiliconValley-Catan-master (1)\SiliconValley-Catan-master\out

"%JAVA%" --module-path "%JFX%" --add-modules javafx.controls,javafx.fxml -cp "%OUT%" view.MainApp
pause

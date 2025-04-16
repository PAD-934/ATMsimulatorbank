@echo off
title ATM Interface
echo Checking for required files...

if not exist "bin\main\java\ATMInterface.class" (
    echo Error: ATMInterface.class not found!
    echo Please compile your Java files first.
    pause
    exit /b 1
)

if not exist "sounds" (
    echo Creating sounds directory...
    mkdir sounds
)

echo Starting ATM Interface...
java -cp bin main.java.ATMInterface
pause
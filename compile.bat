@echo off
echo Creating bin directory...
if not exist "bin" mkdir bin

echo Compiling Java files...
javac -d bin src\main\java\*.java

if errorlevel 1 (
    echo Compilation failed!
) else (
    echo Compilation successful!
)

pause
@echo off
echo Compiling Java files...
javac src\main\java\*.java -d bin
if errorlevel 1 (
    echo Compilation failed!
) else (
    echo Compilation successful!
)
pause
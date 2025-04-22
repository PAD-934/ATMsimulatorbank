@echo off
echo Starting BDA ATM System with Web Interface...

REM Compile the Java files
javac -cp ".;lib/*" src/main/java/*.java

REM Start the ATM system with web interface
java -cp ".;lib/*" src.main.java.Main

echo Server stopped.
pause
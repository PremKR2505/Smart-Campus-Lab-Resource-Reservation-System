@echo off
echo =================================================================
echo   Building Smart Campus Lab & Resource Reservation System...
echo =================================================================

if not exist bin mkdir bin

javac -d bin -sourcepath src src/com/vityarthi/campus/Main.java src/com/vityarthi/campus/config/*.java src/com/vityarthi/campus/model/*.java src/com/vityarthi/campus/concurrency/*.java src/com/vityarthi/campus/storage/*.java src/com/vityarthi/campus/service/*.java src/com/vityarthi/campus/ui/*.java src/com/vityarthi/campus/test/*.java

if %ERRORLEVEL% equ 0 (
    echo [BUILD SUCCESSFUL] All Java class files compiled into bin/
) else (
    echo [BUILD FAILED] Check compilation errors above.
    exit /b %ERRORLEVEL%
)

@echo off
echo =================================================================
echo   Building Smart Campus Lab ^& Resource Reservation System...
echo =================================================================

if not exist bin mkdir bin

javac -d bin -sourcepath src src/com/campus/reserve/Main.java src/com/campus/reserve/config/*.java src/com/campus/reserve/model/*.java src/com/campus/reserve/concurrency/*.java src/com/campus/reserve/storage/*.java src/com/campus/reserve/service/*.java src/com/campus/reserve/ui/*.java src/com/campus/reserve/test/*.java

if %ERRORLEVEL% equ 0 (
    echo [BUILD SUCCESSFUL] All Java class files compiled into bin/
) else (
    echo [BUILD FAILED] Check compilation errors above.
    exit /b %ERRORLEVEL%
)

@echo off
echo =================================================================
echo   Launching Smart Campus Lab ^& Resource Reservation System...
echo =================================================================

if not exist bin\com\campus\reserve\Main.class (
    echo Binaries not found. Triggering build...
    call build.bat
)

if "%1"=="" (
    java -cp bin com.campus.reserve.Main --cli
) else (
    java -cp bin com.campus.reserve.Main %*
)

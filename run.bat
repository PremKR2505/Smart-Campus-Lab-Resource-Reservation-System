@echo off
echo =================================================================
echo   Launching Smart Campus Lab & Resource Reservation System...
echo =================================================================

if not exist bin\com\vityarthi\campus\Main.class (
    echo Binaries not found. Triggering build...
    call build.bat
)

if "%1"=="" (
    java -cp bin com.vityarthi.campus.Main --cli
) else (
    java -cp bin com.vityarthi.campus.Main %*
)

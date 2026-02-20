@echo off
echo ==========================================
echo      Updating Shop Application...
echo ==========================================
echo.
call mvn clean package
echo.
echo ==========================================
if %ERRORLEVEL% EQU 0 (
    echo    UPDATE SUCCESSFUL! 
    echo    Your RunShop shortcut is now updated.
) else (
    echo    UPDATE FAILED! Please check the errors above.
)
echo ==========================================
pause


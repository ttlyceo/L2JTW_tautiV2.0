@ECHO OFF
title L2JTW - DP快速解壓縮
if not exist "dist\doc\DP-Name.txt" goto _OPSTART
set DP_Name1=Test
FOR /F "tokens=1" %%g IN (dist\doc\DP-Name.txt) DO set DP_Name1=%%g
FOR /F "tokens=2" %%h IN (dist\doc\DP-Name.txt) DO set DP_Name2=%%h
echo 你現在準備「解壓縮」的 DP，支援的版本是：%DP_Name1% %DP_Name2%
echo.
echo 　　　　　「解壓縮」的位置是：C:\L2JTW\%DP_Name1%\
echo.
pause
CLS

:_OPSTART
if not exist "build\*.zip" goto _failed
if not exist "winrar.exe" goto _winrar
echo 解壓縮中，請稍候 . . .
FOR %%i in (build\*.zip) do winrar x -o+ %%i C:\L2JTW\%DP_Name1%\
goto _finish

:_winrar
if not exist "%ProgramFiles%\WinRAR\winrar.exe" goto _ERR
echo 解壓縮中，請稍候 . . .
FOR %%i in (build\*.zip) do "%ProgramFiles%\WinRAR\winrar.exe" x -o+ %%i C:\L2JTW\%DP_Name1%\
goto _finish

:_ERR
echo 請先安裝 WinRAR 解壓縮程式！
echo.
pause
goto _EXIT

:_finish
CLS
echo 解壓縮完成！
echo.
echo 請再執行 C:\L2JTW\%DP_Name1%\tools\ 裡面的「database_installer」
echo 選擇「更新」資料庫，再一直按 Enter 直到結束，才算完成 DP 的更新
echo.
pause
goto _EXIT
 
:_failed
echo 請完成編譯後再執行解壓縮！
echo.
pause

:_EXIT

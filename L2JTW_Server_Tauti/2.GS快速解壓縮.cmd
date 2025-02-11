@ECHO OFF
title L2JTW - GS快速解壓縮
if not exist "dist\doc\GS-Name.txt" goto _OPSTART
set GS_Name1=Test
FOR /F "tokens=1" %%g IN (dist\doc\GS-Name.txt) DO set GS_Name1=%%g
FOR /F "tokens=2" %%h IN (dist\doc\GS-Name.txt) DO set GS_Name2=%%h
echo 你現在準備「解壓縮」的 GS，支援的版本是：%GS_Name1% %GS_Name2%
echo.
echo 　　　　　「解壓縮」的位置是：C:\L2JTW\%GS_Name1%\
echo.
pause
CLS

:_OPSTART
set GSM=game
REM set GSM=gameserver
REM 最新的 L2J 把 gameserver 的資料夾改名為 game
if not exist "build\*.zip" goto _failed
if not exist C:\L2JTW\%GS_Name1%\login\config_bak1\ md C:\L2JTW\%GS_Name1%\login\config_bak1\
if not exist C:\L2JTW\%GS_Name1%\login\config_bak2\ md C:\L2JTW\%GS_Name1%\login\config_bak2\
if not exist C:\L2JTW\%GS_Name1%\login\config_bak3\ md C:\L2JTW\%GS_Name1%\login\config_bak3\
if not exist C:\L2JTW\%GS_Name1%\%GSM%\config_bak1\ md C:\L2JTW\%GS_Name1%\%GSM%\config_bak1\
if not exist C:\L2JTW\%GS_Name1%\%GSM%\config_bak2\ md C:\L2JTW\%GS_Name1%\%GSM%\config_bak2\
if not exist C:\L2JTW\%GS_Name1%\%GSM%\config_bak3\ md C:\L2JTW\%GS_Name1%\%GSM%\config_bak3\
if exist C:\L2JTW\%GS_Name1%\login\config_bak2\*.properties copy C:\L2JTW\%GS_Name1%\login\config_bak2\*.properties C:\L2JTW\%GS_Name1%\login\config_bak3\ /Y >nul
if exist C:\L2JTW\%GS_Name1%\login\config_bak1\*.properties copy C:\L2JTW\%GS_Name1%\login\config_bak1\*.properties C:\L2JTW\%GS_Name1%\login\config_bak2\ /Y >nul
if exist C:\L2JTW\%GS_Name1%\login\config\*.properties copy C:\L2JTW\%GS_Name1%\login\config\*.properties C:\L2JTW\%GS_Name1%\login\config_bak1\ /Y >nul
if exist C:\L2JTW\%GS_Name1%\%GSM%\config_bak2\*.properties copy C:\L2JTW\%GS_Name1%\%GSM%\config_bak2\*.properties C:\L2JTW\%GS_Name1%\%GSM%\config_bak3\ /Y >nul
if exist C:\L2JTW\%GS_Name1%\%GSM%\config_bak1\*.properties copy C:\L2JTW\%GS_Name1%\%GSM%\config_bak1\*.properties C:\L2JTW\%GS_Name1%\%GSM%\config_bak2\ /Y >nul
if exist C:\L2JTW\%GS_Name1%\%GSM%\config\*.properties copy C:\L2JTW\%GS_Name1%\%GSM%\config\*.properties C:\L2JTW\%GS_Name1%\%GSM%\config_bak1\ /Y >nul
if not exist "winrar.exe" goto _winrar
echo 解壓縮中，請稍候 . . .
FOR %%i in (build\*.zip) do winrar x -o+ %%i C:\L2JTW\%GS_Name1%\
goto _finish

:_winrar
if not exist "%ProgramFiles%\WinRAR\winrar.exe" goto _ERR
echo 解壓縮中，請稍候 . . .
FOR %%i in (build\*.zip) do "%ProgramFiles%\WinRAR\winrar.exe" x -o+ %%i C:\L2JTW\%GS_Name1%\
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
echo GS更新完成！
echo.
echo 「舊的 config」已經備份到「config_bak1」
echo.
echo 請記得開啟 C:\L2JTW\%GS_Name1%\login\config\LoginServer.properties
echo 修改「登入伺服器」的連線密碼和相關設定！
echo.
echo 請記得開啟 C:\L2JTW\%GS_Name1%\%GSM%\config\Server.properties
echo 修改「遊戲伺服器」的連線密碼和相關設定！
echo.
echo 或者使用 WinMerge，比對 config 和 config_bak1 這二個資料夾
echo.
pause
goto _EXIT
 
:_failed
echo 請完成編譯後再執行解壓縮！
echo.
pause

:_EXIT

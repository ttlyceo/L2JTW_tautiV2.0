@echo off
title Login Server Console
REM ------------------------------------------------------
REM 檢查是否存在 DP 支援的版本資訊
if not exist ..\doc\L2J_DataPack_Ver.txt echo 沒有發現 DP 支援的版本資訊！
if not exist ..\doc\L2J_DataPack_Ver.txt echo 請再一次：更新 DP → 編譯 DP → 解壓縮 DP → 安裝資料庫
if not exist ..\doc\L2J_DataPack_Ver.txt echo.
if not exist ..\doc\L2J_DataPack_Ver.txt pause
if not exist ..\doc\L2J_DataPack_Ver.txt exit
REM 取得 DP 支援的版本資訊
FOR /F %%g IN (..\doc\L2J_DataPack_Ver.txt) DO set vdp=%%g
REM 檢查 DP 支援的版本資訊
if not %vdp% == Tauti echo 無法繼續執行伺服器，因為：
if not %vdp% == Tauti echo GS 支援的版本是：Tauti
if not %vdp% == Tauti echo DP 支援的版本是：%vdp%
if not %vdp% == Tauti echo 請確定 GS 和 DP 都使用相同的版本後，再試一次
if not %vdp% == Tauti echo.
if not %vdp% == Tauti pause
if not %vdp% == Tauti exit
REM ------------------------------------------------------

call :init
:: ----- 設定 java 記憶體用量及其他參數 -----
set javaheap=128m

set java_param=com.l2jserver.loginserver.L2LoginServer
set java_param=-cp "%iii%libs\*";"%LLL%l2jlogin.jar" %java_param%
set java_param=-Xms%javaheap% -Xmx%javaheap% %java_param%
:: set java_param=-XX:PermSize=50m %java_param%
:: set java_param=-XX:MaxPermSize=100m %java_param%
set java_param=-XX:+CMSIncrementalMode %java_param%
set java_param=-XX:+UseCMSCompactAtFullCollection %java_param%
set java_param=-XX:+UseParNewGC %java_param%
set java_param=-XX:+CMSParallelRemarkEnabled %java_param%
set java_param=-XX:+UseParNewGC %java_param%
set java_param=-XX:+CMSParallelRemarkEnabled %java_param%
set java_param=-XX:+UseConcMarkSweepGC %java_param%
:: set java_param=-Djava.util.logging.manager=com.l2jserver.util.L2LogManager %java_param%
:: set java_param=-Djava.util.logging.manager=com.l2jserver.util.L2LogManager -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:+CMSIncrementalMode -Xms%javaheap% -Xmx%javaheap% -cp "%iii%libs\*";"%LLL%l2jserver.jar" com.l2jserver.gameserver.GameServer
goto start

:init
set LLL=%~dp0
::FOR /F "delims=login" %%i IN ("%LLL%") do set iii=%%i
set iii=%LLL:~0,-6%
call :check_libs
call :try_java
goto :eof

:check_libs
REM ------------------------------------------------------
if not exist ..\libs\*.jar (
	echo 您必須重新解壓縮「編譯完成」的 GS，才可以繼續執行
	echo.
	pause
	exit
)
goto :eof
REM ------------------------------------------------------

:check_java
if "%found%"=="1" goto :eof
if not exist %1 goto :eof
set found=1
set PATH=%~dp1
REM echo 使用 java 位置 %path%
goto :eof

:set_java
REM echo 搜尋 java 位置
call :check_java "%ProgramW6432%\Java\jre7\bin\java.exe"
call :check_java "%ProgramFiles%\Java\jre7\bin\java.exe"
call :check_java "%ProgramFiles(x86)%\Java\jre7\bin\java.exe"
call :check_java "%windir%\system32\java.exe"
if "%found%"=="" (
	echo 找不到 java.exe
	echo 請先安裝 java 1.7
	echo 下載及安裝可參考論壇
	echo http://www.l2jtw.com/l2jtwbbs/viewtopic.php?f=42^&t=6264
	echo.
	pause
	exit
)
goto :eof

:try_java
call :set_java
goto :eof

:start
java %java_param%
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restart ...
echo.
goto start

:error
echo.
echo Server terminated abnormally
echo.
:end
echo.
echo server terminated
echo.
pause

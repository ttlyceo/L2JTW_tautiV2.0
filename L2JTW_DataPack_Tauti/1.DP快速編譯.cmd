@ECHO OFF
title L2JTW - DP快速編譯

if exist "DP-Support.txt" (
type "DP-Support.txt"
echo.
pause
CLS
)
if not exist "build.xml" goto _WRONG_LOC
:_check_copy
if exist "DP-Support.txt" goto _check_copy_next
if exist "GS-Support.txt" echo 在這個資料夾應該執行「GS快速編譯」才對！
if exist "GS-Support.txt" echo.
if exist "GS-Support.txt" pause
if exist "GS-Support.txt" goto _EXIT
if exist "CB-Support.txt" echo 在這個資料夾應該執行「CB快速編譯」才對！
if exist "CB-Support.txt" echo.
if exist "CB-Support.txt" pause
if exist "CB-Support.txt" goto _EXIT
type build.xml | find /c /i "_Datapack" > %temp%\check.txt
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 goto _check_copy_next
type build.xml | find /c /i "_GameServer" > %temp%\check.txt
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 echo 在這個資料夾應該執行「GS快速編譯」才對！
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 echo.
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 pause
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 goto _EXIT
type build.xml | find /c /i "_Community" > %temp%\check.txt
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 echo 在這個資料夾應該執行「CB快速編譯」才對！
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 echo.
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 pause
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 goto _EXIT
type build.xml | find /c /i "_Server" > %temp%\check.txt
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 echo 在這個資料夾應該執行「GS快速編譯」才對！
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 echo.
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 pause
FOR /F %%i IN (%temp%\check.txt) DO IF %%i GTR 0 goto _EXIT
:_check_copy_next
if not exist "WinRAR.exe" goto _UPDATE
if not exist "ant.jar" goto _UPDATE
if not exist "ant-launcher.jar" goto _UPDATE
if not exist "2.DP*.cmd" goto _UPDATE
if exist "1.GS*.cmd" del "1.GS*.cmd"
if exist "1.CB*.cmd" del "1.CB*.cmd"
if exist "2.GS*.cmd" del "2.GS*.cmd"
if exist "2.CB*.cmd" del "2.CB*.cmd"

:_start
set PF=%ProgramFiles%
if exist "%ProgramFiles(x86)%" set PF=%ProgramFiles(x86)%
if exist "%PF%\MySQL\MySQL Server 5.0\bin\mysql.exe" goto _MySQL
if exist "%PF%\MySQL\MySQL Server 5.1\bin\mysql.exe" goto _MySQL
if exist "%PF%\MySQL\MySQL Server 5.5\bin\mysql.exe" goto _MySQL
if not exist "%ProgramFiles%\Java\jre7\bin\java.exe" goto _JAVA_ERR

:_check_ver
if not exist "%ProgramFiles%\Java\jdk1.7.*" goto _JAVA_ERR
dir "%ProgramFiles%\Java\jdk1.7.*" /A:D /B /O > %temp%\check.txt
FOR /F %%j IN (%temp%\check.txt) DO set ver=%%j

:_JAVA_VER
if not exist "%ProgramFiles%\Java\%ver%\lib\tools.jar" goto _JAVA_ERR
if not exist "%ProgramFiles%\Java\%ver%\bin\java.exe" goto _JAVA_ERR
if exist "%ProgramFiles%\Java\%ver%\lib\ant_184dp.txt" goto _JAVA_VER_NEXT
echo.>> "%ProgramFiles%\Java\%ver%\lib\ant_184dp.txt"
if not exist "%ProgramFiles%\Java\%ver%\lib\ant_184dp.txt" goto _NO_ACCESS
copy /Y "ant.jar" "%ProgramFiles%\Java\%ver%\lib" > nul
copy /Y "ant-launcher.jar" "%ProgramFiles%\Java\%ver%\lib" > nul
:_JAVA_VER_NEXT
if not exist "%ProgramFiles%\Java\%ver%\lib\ant.jar" copy /Y "ant.jar" "%ProgramFiles%\Java\%ver%\lib" > nul
if not exist "%ProgramFiles%\Java\%ver%\lib\ant.jar" goto _NO_ACCESS
if not exist "%ProgramFiles%\Java\%ver%\lib\ant-launcher.jar" copy /Y "ant-launcher.jar" "%ProgramFiles%\Java\%ver%\lib" > nul
if not exist "%ProgramFiles%\Java\%ver%\lib\ant-launcher.jar" goto _NO_ACCESS
goto _NEXT

:_NEXT
if not exist "%PF%\Notepad++\notepad++.exe" goto _NOTEPAD
if not exist "%PF%\WinMerge\WinMergeU.exe" goto _WINMERGE
if not exist "%ProgramFiles%\TortoiseSVN\bin\TortoiseSVN*.dll" goto _SVN
if not exist "%SystemDrive%\AppServ\www\index.php" echo 找不到 %SystemDrive%\AppServ
if not exist "%SystemDrive%\AppServ\www\index.php" echo.
if not exist "%SystemDrive%\AppServ\www\index.php" pause
CLS
if exist "build\*.zip" del "build\*.zip" /Q > nul
if exist "buildX" echo 刪除之前編譯的暫存檔案，請稍候 . . .
if exist "buildX" RMDIR "buildX" /S /Q > nul
CLS
if exist "build" RENAME "build" "buildX" > nul
if exist "build" goto _USE
if exist "buildX" echo 刪除之前編譯的暫存檔案，請稍候 . . .
if exist "buildX" RMDIR "buildX" /S /Q > nul
CLS

echo 編譯中，請稍候 . . .
"%ProgramFiles%\Java\%ver%\bin\java.exe" -classpath "%ProgramFiles%\Java\%ver%\lib\ant-launcher.jar" "-Dant.home=%ProgramFiles%\Java\%ver%" org.apache.tools.ant.launch.Launcher > Log.txt
if not exist "build\*.zip" goto _failed
CLS
if exist "build\*.zip" type Log.txt
if exist "build\*.zip" echo.
if exist "build\*.zip" echo ■■■■■■■■■■■
if exist "build\*.zip" echo ■　 DP編譯成功！ 　■
if exist "build\*.zip" echo ■■■■■■■■■■■
echo.
echo 編譯過程的訊息已儲存在 Log.txt
echo.
pause
goto _EXIT

:_MySQL
echo 請先移除舊版的 MySQL 5，才能進行編譯
echo.
pause
goto _EXIT

:_JAVA_ERR
echo %ProgramFiles%\ 找不到 Java 7
echo.
echo 請先安裝 JAVA JDK 7 的最新版本，並且不要更改預設安裝位置
echo.
echo ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
echo ■ 注意：嗨翻天 和 塔武提，必須安裝 JAVA JDK 7 (沙哈之裔 則是 JDK 8)  ■
echo ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
echo.
pause
goto _EXIT

:_NOTEPAD
echo %PF%\ 找不到 Notepad++
echo.
echo 請先安裝 Notepad++，才能修改設定
echo 並且在安裝時，不要更改預設安裝位置
echo.
pause
goto _EXIT

:_WINMERGE
echo %PF%\ 找不到 WinMerge
echo.
echo 請先安裝 WinMerge，才能比對 config
echo 並且在安裝時，不要更改預設安裝位置
echo.
pause
goto _EXIT

:_SVN
echo %ProgramFiles%\ 找不到 TortoiseSVN
echo.
echo 請先安裝 TortoiseSVN，才能進行更新
echo 並且在安裝時，不要更改預設安裝位置
echo.
pause
goto _EXIT

:_WRONG_LOC
echo 無法在這個資料夾執行「編譯」
echo.
echo 請將 DP 更新到最新版本後，再試一次
echo.
pause
goto _EXIT

:_UPDATE
echo 請先將「工具-Java 7」內的 9 個檔案，複製到 GS/DP/CB 的資料夾
echo.
pause
goto _EXIT

:_NO_ACCESS
color 0c
echo.
echo ◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆
echo ◆　　　你的電腦沒有開放存取的權限！！　　　◆
echo ◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆
echo.
pause
CLS
echo 重點１：
echo 必須以「系統管理員」的帳戶登入 Windows，才擁有足夠的權限
echo 檢查方法：
echo Vista：控制台→新增或移除使用者帳戶→檢查看看是不是「Administrator」
echo Win 7：控制台→新增或移除使用者帳戶→檢查看看是不是「系統管理員」
echo ============================================================================
echo 重點２：
echo 必須關閉 UAC，「快速編譯」才能存取
echo 關閉方法：請按照以下方法，把 UAC 關閉
echo.
echo Vista：控制台→使用者帳戶和家庭安全→使用者帳戶→開啟或關閉使用者帳戶控制→
echo        取消打勾：使用[使用者帳戶控制(UAC)]來協助保護您的電腦→確定→重新開機
echo.
echo Win 7：控制台→系統及安全性→變更使用者帳戶控制設定→
echo        將調整鈕往下拉至「不要通知」→確定→重新開機
echo ============================================================================
echo.
pause
goto _EXIT

:_USE
echo.
echo 請先關閉所有程式，然後再試一次。
echo.
pause
goto _EXIT

:_failed
echo.
type Log.txt
echo.
echo ■■■■■■■■■■■
echo ■　 DP編譯失敗！ 　■
echo ■■■■■■■■■■■
echo.
echo 編譯失敗的訊息已儲存在 Log.txt
echo 您可以將 Log.txt 裡面的內容貼到討論區，尋求幫助...
echo.
pause

:_EXIT

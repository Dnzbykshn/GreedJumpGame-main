@echo off
setlocal enabledelayedexpansion

set STUDENT_ID=20240808701

echo.
echo ===== TESTLER CALISIYOR =====
echo.

set /a total_10=0, count_10=0
set /a total_25=0, count_25=0
set /a total_50=0, count_50=0

:: 10x10 testleri
for %%i in (1 2 3 4 5) do (
    set file=boards\board_10x10_%%i.dat
    if exist "!file!" (
        for /f "tokens=3" %%a in ('java -cp bin game.Tester "!file!" %STUDENT_ID% ^| findstr "Final Score:"') do (
            set /a total_10+=%%a
            set /a count_10+=1
        )
    )
)

:: 25x25 testleri
for %%i in (1 2 3 4 5) do (
    set file=boards\board_25x25_%%i.dat
    if exist "!file!" (
        for /f "tokens=3" %%a in ('java -cp bin game.Tester "!file!" %STUDENT_ID% ^| findstr "Final Score:"') do (
            set /a total_25+=%%a
            set /a count_25+=1
        )
    )
)

:: 50x50 testleri
for %%i in (1 2 3 4 5) do (
    set file=boards\board_50x50_%%i.dat
    if exist "!file!" (
        for /f "tokens=3" %%a in ('java -cp bin game.Tester "!file!" %STUDENT_ID% ^| findstr "Final Score:"') do (
            set /a total_50+=%%a
            set /a count_50+=1
        )
    )
)

:: Ortalamalari hesapla ve goster
echo.
echo ===== ORTALAMA SKORLAR =====
echo.

if %count_10% gtr 0 (
    set /a avg_10=total_10/count_10
    echo 10x10: %avg_10%
) else echo 10x10: Test yapilmadi

if %count_25% gtr 0 (
    set /a avg_25=total_25/count_25
    echo 25x25: %avg_25%
) else echo 25x25: Test yapilmadi

if %count_50% gtr 0 (
    set /a avg_50=total_50/count_50
    echo 50x50: %avg_50%
) else echo 50x50: Test yapilmadi

echo.
pause
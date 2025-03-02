@echo off

REM --------------------------------------------------
REM Monster Trading Cards Game
REM --------------------------------------------------
title Monster Trading Cards Game
echo CURL Testing for Monster Trading Cards Game
echo Syntax: $1 [pause]
echo - pause: optional, if set, the script will pause after each block
echo.

set "pauseFlag=0"
for %%a in (%*) do (
    if /I "%%a"=="pause" (
        set "pauseFlag=1"
    )
)


if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 15) stats
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and user stats"
echo.
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and user stats"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 16) scoreboard
curl -i -X GET http://localhost:10001/scoreboard --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and the scoreboard"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 17) battle
start /b "kienboec battle" curl -i -X POST http://localhost:10001/battles --header "Authorization: Bearer kienboec-mtcgToken"
start /b "altenhof battle" curl -i -X POST http://localhost:10001/battles --header "Authorization: Bearer altenhof-mtcgToken"
ping localhost -n 10 >NUL 2>NUL

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 18) Stats 
echo kienboec
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and changed user stats"
echo.
echo altenhof
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and changed user stats"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 19) scoreboard
curl -i -X GET http://localhost:10001/scoreboard --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and the changed scoreboard"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo end...

REM this is approx a sleep 
ping localhost -n 100 >NUL 2>NUL
@echo on

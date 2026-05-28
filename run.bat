@echo off
title Сапёр - запуск
chcp 65001 >nul
cd /d "%~dp0"


:: Запуск приложения через Gradle
call gradlew run
pause
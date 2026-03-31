@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo 课灵 APK 发布脚本 (Windows)
echo ========================================

cd /d "%~dp0..\apps\android"

REM 获取版本信息
for /f "tokens=2 delims==" %%a in ('findstr "versionCode" app\build.gradle.kts') do (
    set VERSION_CODE=%%a
    set VERSION_CODE=!VERSION_CODE: =!
)

for /f "tokens=2 delims==" %%a in ('findstr "versionName" app\build.gradle.kts') do (
    set VERSION_NAME=%%a
    set VERSION_NAME=!VERSION_NAME:"=!
    set VERSION_NAME=!VERSION_NAME: =!
)

set TAG_NAME=v!VERSION_NAME!
set APK_NAME=KeLing-!TAG_NAME!.apk

echo.
echo 版本号: !VERSION_NAME! (code: !VERSION_CODE!)
echo 标签名: !TAG_NAME!
echo APK名: !APK_NAME!
echo ========================================

echo.
echo 步骤1: 构建 release APK...
call gradlew.bat assembleRelease

if exist "app\build\outputs\apk\release\app-release.apk" (
    echo APK构建成功!

    REM 创建发布目录
    cd /d "%~dp0.."
    if not exist "releases" mkdir releases
    copy "apps\android\app\build\outputs\apk\release\app-release.apk" "releases\!APK_NAME!"

    echo.
    echo 步骤2: Git提交...
    git add -A
    git commit -m "chore: release !TAG_NAME!" 2>nul || echo 没有需要提交的更改
    git push origin main

    echo.
    echo 步骤3: 创建Git标签...
    git tag -a !TAG_NAME! -m "Release !TAG_NAME!" 2>nul || echo 标签已存在
    git push origin !TAG_NAME! 2>nul || echo 标签已推送

    echo.
    echo ========================================
    echo 发布准备完成!
    echo ========================================
    echo.
    echo APK文件: releases\!APK_NAME!
    echo.
    echo GitHub Release 链接:
    echo https://github.com/Hyhyhyyy/KeLingAssistant/releases/tag/!TAG_NAME!
    echo.
    echo 下一步操作:
    echo 1. 访问 https://github.com/Hyhyhyyy/KeLingAssistant/releases/new
    echo 2. 选择标签: !TAG_NAME!
    echo 3. 填写标题: 课灵 !TAG_NAME!
    echo 4. 上传APK文件: releases\!APK_NAME!
    echo 5. 点击发布
    echo.
    echo 或者等待GitHub Actions自动构建...
    echo ========================================
) else (
    echo APK构建失败!
    pause
    exit /b 1
)

pause
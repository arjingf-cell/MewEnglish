@echo off
set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
set ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" wait-for-device
call gradlew.bat :app:installDebug

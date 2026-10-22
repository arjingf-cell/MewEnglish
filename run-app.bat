@echo off
set ANDROID_HOME=%USERPROFILE%\Android\sdk
set ANDROID_SDK_ROOT=%USERPROFILE%\Android\sdk
"%USERPROFILE%\Android\sdk\platform-tools\adb.exe" wait-for-device
call gradlew.bat :app:installDebug

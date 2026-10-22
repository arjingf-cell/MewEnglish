@echo off
set ANDROID_HOME=%USERPROFILE%\Android\sdk
set ANDROID_SDK_ROOT=%USERPROFILE%\Android\sdk
start "" "%USERPROFILE%\Android\sdk\emulator\emulator.exe" -avd Pixel_8 -no-snapshot-load

@echo off
setlocal

set "ANDROID_HOME=%USERPROFILE%\Android\sdk"
set "ANDROID_SDK_ROOT=%USERPROFILE%\Android\sdk"
set "EMULATOR=%USERPROFILE%\Android\sdk\emulator\emulator.exe"

start "MewEnglish Lite Emulator" "%EMULATOR%" ^
  -avd Pixel_8 ^
  -memory 2048 ^
  -cores 2 ^
  -skin 720x1280 ^
  -dpi-device 320 ^
  -gpu auto ^
  -no-boot-anim ^
  -no-audio ^
  -camera-back none ^
  -camera-front none ^
  -no-snapshot-load ^
  -no-metrics ^
  -no-sim

endlocal

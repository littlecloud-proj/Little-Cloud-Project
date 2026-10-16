#!/usr/bin/env bash
# Installs the debug APK on the emulator and photographs each screen, light and dark.
set -x
mkdir -p shots
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.littlecloud.app android.permission.POST_NOTIFICATIONS || true
shoot() { # name, open-request, seconds to wait
  adb shell am force-stop com.littlecloud.app
  adb shell am start -n com.littlecloud.app/.MainActivity --es open "$2"
  sleep "${3:-8}"
  adb exec-out screencap -p > "shots/android-$1.png"
}
for mode in no yes; do
  adb shell cmd uimode night $mode
  suffix=$([ $mode = yes ] && echo "-dark" || echo "")
  shoot "today$suffix" "tab:today"
  shoot "hours$suffix" "tab:hours" 25
  shoot "calendar$suffix" "tab:calendar"
  shoot "prayers$suffix" "tab:prayers"
  shoot "bells$suffix" "tab:bells"
  shoot "settings$suffix" "settings"
  shoot "angelus$suffix" "prayer:angelus"
done
adb logcat -d -s AndroidRuntime:E > shots/android-crashes.txt
ls -la shots

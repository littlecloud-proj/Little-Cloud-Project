#!/usr/bin/env bash
# Installs the simulator build and photographs each screen, light and dark.
set -x
mkdir -p shots
APP=$(find build/dd/Build/Products -name 'LittleCloud.app' -maxdepth 3 | head -1)
DEV=$(xcrun simctl list devices available -j | python3 -c "import json,sys; d=json.load(sys.stdin)['devices']; print(next(x['udid'] for k,v in d.items() if 'iOS' in k for x in v if x['name'].startswith('iPhone')))")
xcrun simctl boot "$DEV"; xcrun simctl bootstatus "$DEV" -b
xcrun simctl install "$DEV" "$APP"
shoot() { # name, open-request
  xcrun simctl terminate "$DEV" com.littlecloud.app 2>/dev/null
  SIMCTL_CHILD_LC_OPEN="$2" xcrun simctl launch "$DEV" com.littlecloud.app
  sleep 8
  xcrun simctl io "$DEV" screenshot "shots/ios-$1.png"
}
for mode in light dark; do
  xcrun simctl ui "$DEV" appearance $mode
  suffix=$([ $mode = dark ] && echo "-dark" || echo "")
  shoot "today$suffix" "tab:today"
  shoot "hours$suffix" "tab:hours"
  shoot "calendar$suffix" "tab:calendar"
  shoot "prayers$suffix" "tab:prayers"
  shoot "bells$suffix" "tab:bells"
  shoot "settings$suffix" "settings"
  shoot "angelus$suffix" "prayer:angelus"
done
xcrun simctl spawn "$DEV" log show --last 3m --predicate 'process == "LittleCloud"' --style compact 2>/dev/null | grep -iE 'fault|crash|exception|error' | tail -50 > shots/ios-errors.txt
ls -la shots

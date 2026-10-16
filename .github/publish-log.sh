#!/usr/bin/env bash
# Pushes this job's build log to the branch ci-logs/<job>, so it can be read without the Actions UI.
set -u
job="$1"; status="$2"
dir="$RUNNER_TEMP/log-$job"
mkdir -p "$dir" && cd "$dir" || exit 0
git init -q -b main
cp "$GITHUB_WORKSPACE/build.log" . 2>/dev/null || echo "no build.log" > build.log
grep -nE '^e: |error:|FAILURE|What went wrong|BUILD (SUCCESSFUL|FAILED)|\*\* BUILD' build.log | head -300 > errors.txt || true
# Hand back the generated Xcode project, so it can be committed for people without XcodeGen.
if [ -d "$GITHUB_WORKSPACE/iosApp/iosApp.xcodeproj" ]; then
  cp -R "$GITHUB_WORKSPACE/iosApp/iosApp.xcodeproj" . && cp "$GITHUB_WORKSPACE/iosApp/iosApp/Info.plist" . 2>/dev/null
  rm -rf iosApp.xcodeproj/xcuserdata iosApp.xcodeproj/project.xcworkspace/xcuserdata
fi
cp "$GITHUB_WORKSPACE"/app/build/outputs/apk/debug/app-debug.apk ./LittleCloud-debug.apk 2>/dev/null
[ -d "$GITHUB_WORKSPACE/shots" ] && cp -R "$GITHUB_WORKSPACE/shots" .
printf '%s %s %s\n' "$GITHUB_SHA" "$status" "$(date -u +%FT%TZ)" > status.txt
git add . && git -c user.name=ci -c user.email=ci@localhost commit -qm "$job $status $GITHUB_SHA"
git push -qf "https://x-access-token:${TOKEN}@github.com/${GITHUB_REPOSITORY}.git" "HEAD:refs/heads/ci-logs/$job"

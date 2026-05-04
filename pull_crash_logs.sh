#!/bin/bash
# Pull crash logs from AMM device via adb
# Usage: ./pull_crash_logs.sh

PKG="io.shubham0204.smollmandroid"

echo "=== AMM Crash Log Puller ==="
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "Error: adb not found in PATH"
    echo "Install Android SDK platform-tools or use the in-app viewer:"
    echo "  Chat screen -> ⋮ Menu -> View crash logs"
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "Error: No Android device connected via adb"
    exit 1
fi

echo "Pulling global crash logs..."
adb shell run-as $PKG cat files/global_crashes.txt > global_crashes.txt 2>/dev/null
if [ -s global_crashes.txt ]; then
    echo "  -> Saved to ./global_crashes.txt ($(wc -c < global_crashes.txt) bytes)"
else
    echo "  -> No global crashes found"
    rm -f global_crashes.txt
fi

echo "Pulling browser crash logs..."
adb shell run-as $PKG cat files/browser_crash_logs.txt > browser_crash_logs.txt 2>/dev/null
if [ -s browser_crash_logs.txt ]; then
    echo "  -> Saved to ./browser_crash_logs.txt ($(wc -c < browser_crash_logs.txt) bytes)"
else
    echo "  -> No browser crashes found"
    rm -f browser_crash_logs.txt
fi

echo ""
echo "Recent logcat for $PKG (last 100 lines):"
echo "---"
adb logcat -d -s AndroidRuntime:E BrowserActivity:E GeckoView:E GeckoConsole:E | tail -100

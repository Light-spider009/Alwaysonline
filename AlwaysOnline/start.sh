#!/bin/bash
# =============================================
#   AlwaysOnline - Server Auto-Restart Script
#   Place this in your Minecraft server folder
#   Run with: chmod +x start.sh && ./start.sh
# =============================================

# --- CONFIGURATION ---
SERVER_JAR="server.jar"         # Your server jar filename
MIN_RAM="1G"                    # Minimum RAM allocation
MAX_RAM="4G"                    # Maximum RAM allocation (adjust to your server)
RESTART_DELAY=5                 # Seconds to wait before restarting after crash
LOG_FILE="server_uptime.log"    # Log file for uptime tracking

# --- DO NOT EDIT BELOW THIS LINE ---
CRASH_COUNT=0
MAX_CRASHES=10                  # Stop restarting after this many crashes in a row

echo "========================================"
echo "  AlwaysOnline Server Manager Started"
echo "========================================"

while true; do
    STARTED_AT=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$STARTED_AT] Starting Minecraft server..." | tee -a "$LOG_FILE"

    # Start the server
    java -Xms$MIN_RAM -Xmx$MAX_RAM \
         -XX:+UseG1GC \
         -XX:+ParallelRefProcEnabled \
         -XX:MaxGCPauseMillis=200 \
         -XX:+UnlockExperimentalVMOptions \
         -XX:+DisableExplicitGC \
         -XX:+AlwaysPreTouch \
         -XX:G1NewSizePercent=30 \
         -XX:G1MaxNewSizePercent=40 \
         -XX:G1HeapRegionSize=8M \
         -XX:G1ReservePercent=20 \
         -XX:G1HeapWastePercent=5 \
         -XX:G1MixedGCCountTarget=4 \
         -XX:InitiatingHeapOccupancyPercent=15 \
         -XX:G1MixedGCLiveThresholdPercent=90 \
         -XX:G1RSetUpdatingPauseTimePercent=5 \
         -XX:SurvivorRatio=32 \
         -XX:+PerfDisableSharedMem \
         -XX:MaxTenuringThreshold=1 \
         -Dusing.aikars.flags=https://mcflags.emc.gs \
         -Daikars.new.flags=true \
         -jar "$SERVER_JAR" nogui

    EXIT_CODE=$?
    STOPPED_AT=$(date '+%Y-%m-%d %H:%M:%S')

    # Check if this was a clean stop (exit code 0 = intentional /stop)
    if [ $EXIT_CODE -eq 0 ]; then
        echo "[$STOPPED_AT] Server stopped cleanly (exit code 0). Not restarting." | tee -a "$LOG_FILE"
        echo "To restart the server, run this script again."
        break
    fi

    CRASH_COUNT=$((CRASH_COUNT + 1))
    echo "[$STOPPED_AT] Server crashed! (exit code: $EXIT_CODE, crash #$CRASH_COUNT)" | tee -a "$LOG_FILE"

    if [ $CRASH_COUNT -ge $MAX_CRASHES ]; then
        echo "[$STOPPED_AT] Too many crashes ($CRASH_COUNT). Stopping auto-restart." | tee -a "$LOG_FILE"
        echo "Please check your server logs and fix the issue."
        break
    fi

    echo "Restarting in $RESTART_DELAY seconds... (Press Ctrl+C to cancel)" | tee -a "$LOG_FILE"
    sleep $RESTART_DELAY

    # Reset crash count if server ran for more than 5 minutes before crashing
    # (indicates it was a one-off crash, not a boot loop)
    CRASH_COUNT=0
done

echo "========================================"
echo "  Server Manager Exited"
echo "========================================"

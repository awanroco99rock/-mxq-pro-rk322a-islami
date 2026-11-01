#!/system/bin/sh
# MXQ PRO RK322A 1GB/8GB OS7 - Debloat Anti Reset
# Fix OpenCode reset terus - By BYAN Bekasi
# Cara jalanin: di Terminal Emulator ketik: su -c sh /sdcard/debloat_rk322a_os7.sh

echo "=== MXQ PRO RK322A Debloat Mulai ==="
mount -o rw,remount /system
mount -o rw,remount /

# Buat backup biar aman
mkdir -p /sdcard/debloat_backup/mxq

# 1. MATIKAN BIANG KEROK RESET - MBOX LAUNCHER
echo "Hapus MBOX..."
pm disable com.mbox.launcher 2>/dev/null
pm disable com.mbox.settings 2>/dev/null
mv /system/priv-app/MboxLauncher /sdcard/debloat_backup/mxq/ 2>/dev/null
rm -rf /system/priv-app/MboxLauncher
rm -rf /system/priv-app/MboxSettings
rm -rf /system/app/MboxSettings
rm -f /system/media/bootanimation.zip
rm -f /system/media/bootanimation_mbox.zip

# 2. HAPUS BLOAT BERAT BIAR LEGA 800MB
echo "Bersihkan bloat..."
for apk in Chrome YoutubeGo AkariStore Browser QuickSearchBox StbLog PrintSpooler; do
  mv /system/app/$apk /sdcard/debloat_backup/mxq/ 2>/dev/null
  rm -rf /system/app/$apk
  rm -rf /system/priv-app/$apk
done

# 3. INSTALL LAUNCHER ISLAMI + ADZAN
echo "Install Launcher Islami..."
if [ -f /sdcard/IslamiLauncher.apk ]; then
  mkdir -p /system/priv-app/IslamiLauncher
  cp /sdcard/IslamiLauncher.apk /system/priv-app/IslamiLauncher/IslamiLauncher.apk
  chmod 644 /system/priv-app/IslamiLauncher/IslamiLauncher.apk
fi

# Copy suara adzan
if [ -d /sdcard/adzan ]; then
  mkdir -p /system/media/adzan
  cp /sdcard/adzan/*.mp3 /system/media/adzan/ 2>/dev/null
  chmod 644 /system/media/adzan/*
fi

# 4. KUNCI BIAR GAK RESET LAGI (INI KUNCINYA)
echo "Fix permission anti reset..."
chmod 755 /system/priv-app
chmod 755 /system/app
chown -R 0:0 /system/priv-app
chown -R 0:0 /system/app
chmod -R 644 /system/priv-app/*.apk 2>/dev/null
chmod -R 644 /system/app/*.apk 2>/dev/null

# Set default launcher ke Islami
settings put secure launcher_default com.mxq.islami.launcher 2>/dev/null
pm set-home-activity com.mxq.islami.launcher/.MainActivity 2>/dev/null

# Matikan OTA biar gak balik lagi
pm disable com.google.android.gms/.update.SystemUpdateService 2>/dev/null
settings put global auto_update 0 2>/dev/null

echo "=== SELESAI - REBOOT 3 DETIK ==="
sleep 3
reboot

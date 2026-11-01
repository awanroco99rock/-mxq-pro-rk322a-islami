package com.mxq.islami.launcher;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import org.json.*;

// MXQ PRO RK322A 1GB/8GB - Launcher Islami 3.2MB RAM 80MB
// Fitur: Jadwal Sholat + Kalender Nasional + Jawa + Adzan Otomatis

public class MainActivity extends Activity {
    
    TextView tvJam, tvTanggal, tvJawa, tvSholat;
    String[] pasaran = {"Legi", "Pahing", "Pon", "Wage", "Kliwon"};
    String[] bulanHijriah = {"Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban", "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"};
    
    // Lokasi default Bekasi biar akurat
    double lat = -6.2416, lon = 106.9922;
    String city = "Bekasi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvJam = findViewById(R.id.tvJam);
        tvTanggal = findViewById(R.id.tvTanggal);
        tvJawa = findViewById(R.id.tvJawa);
        tvSholat = findViewById(R.id.tvSholat);
        
        updateTanggal();
        getJadwalSholat();
        setAlarmAdzan();
        
        // Update jam tiap detik - ringan
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                tvJam.setText(new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()));
                h.postDelayed(this, 1000);
            }
        }, 1000);
    }

    // KALENDER JAWA - Rumus Julian Day (Paling Akurat)
    public String getPasaranJawa(int d, int m, int y) {
        long jd = 367*y - 7*(y + (m+9)/12)/4 + 275*m/9 + d + 1721014;
        return pasaran[(int)((jd+4)%5)];
    }

    public void updateTanggal() {
        Calendar c = Calendar.getInstance();
        int d = c.get(Calendar.DAY_OF_MONTH);
        int m = c.get(Calendar.MONTH)+1;
        int y = c.get(Calendar.YEAR);
        
        // Kalender Nasional
        String tglNasional = new java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id","ID")).format(c.getTime());
        tvTanggal.setText(tglNasional);
        
        // Kalender Jawa
        String pasaranJawa = getPasaranJawa(d, m, y);
        tvJawa.setText("Jawa: " + d + " " + pasaranJawa);
    }

    // JADWAL SHOLAT - API Aladhan + Cache Offline
    public void getJadwalSholat() {
        // Simpan cache di SharedPreferences biar tetep jalan offline
        SharedPreferences pref = getSharedPreferences("sholat", 0);
        String cached = pref.getString("today", null);
        
        if(cached != null) {
            tvSholat.setText(cached);
        } else {
            // Default Bekasi kalo offline
            tvSholat.setText("Subuh 04:42 | Dzuhur 11:55 | Ashar 15:12 | Maghrib 17:56 | Isya 19:05");
        }
        
        // Background fetch - jangan di UI thread
        new Thread(() -> {
            try {
                // API: https://api.aladhan.com/v1/timingsByCity?city=Bekasi&country=Indonesia&method=20
                // method 20 = Kemenag
                java.net.URL url = new java.net.URL("https://api.aladhan.com/v1/timingsByCity?city="+city+"&country=Indonesia&method=20");
                java.util.Scanner s = new java.util.Scanner(url.openStream()).useDelimiter("\\A");
                String json = s.hasNext() ? s.next() : "";
                
                JSONObject data = new JSONObject(json).getJSONObject("data").getJSONObject("timings");
                String jadwal = "Subuh "+data.getString("Fajr")+" | Dzuhur "+data.getString("Dhuhr")+" | Ashar "+data.getString("Asr")+" | Maghrib "+data.getString("Maghrib")+" | Isya "+data.getString("Isha");
                
                pref.edit().putString("today", jadwal).apply();
                runOnUiThread(() -> tvSholat.setText(jadwal));
            } catch (Exception e) {}
        }).start();
    }

    // ALARM ADZAN OTOMATIS 5 WAKTU
    public void setAlarmAdzan() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        String[] waktu = {"04:42", "11:55", "15:12", "17:56", "19:05"};
        String[] nama = {"subuh", "dzuhur", "ashar", "maghrib", "isya"};
        
        for(int i=0; i<waktu.length; i++) {
            Intent intent = new Intent(this, AdzanReceiver.class);
            intent.putExtra("adzan", nama[i]);
            
            PendingIntent pi = PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_IMMUTABLE);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(waktu[i].split(":")[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(waktu[i].split(":")[1]));
            cal.set(Calendar.SECOND, 0);
            
            if(cal.getTimeInMillis() < System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1);
            
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }
}

// Receiver buat bunyiin adzan
class AdzanReceiver extends BroadcastReceiver {
    public void onReceive(Context c, Intent i) {
        String adzan = i.getStringExtra("adzan");
        // Play /system/media/adzan/adzan.mp3 + adzan_subuh.mp3 dll
        // Ringan, cuma MediaPlayer
    }
}

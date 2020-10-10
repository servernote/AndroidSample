// アプリケーション
// グローバルに共有する変数関数を持つ = 別名 G
package net.servernote.pulldownbutton;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class cApplication extends Application {
    public boolean FIRST;
    public boolean DEBUG;
    public String VERSION;
    public HashMap<String, Integer> N;
    public HashMap<String, String> S;
    public Calendar CAL_FR; //サポートする日付範囲(from) 判明している祝日分まで
    public Calendar CAL_TO; //サポートする日付範囲(to)
    public HashMap<String, String> HOLIDAY; //祝日Hash

    private void log(String s) {
        log("cApplication", s);
    }

    @Override
    public void onCreate() {
        Log.d("cApplication.onCreate", "start application");
        super.onCreate();

        DEBUG = false;
        VERSION = "";

        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
            if (pi.versionName != null) VERSION = pi.versionName;
            DEBUG = (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
        } catch (Exception e) {
            Log.e("cApplication.onCreate", e.toString());
        }

        N = new HashMap<String, Integer>();
        S = new HashMap<String, String>();

        N.put("SAVE_EXIST", 0);
        S.put("CLIENT_LATITUDE", "");
        S.put("CLIENT_LONGITUDE", "");

        SharedPreferences pref = getSharedPreferences("Preferences", MODE_PRIVATE);

        for (Map.Entry<String, Integer> entry : N.entrySet()) {
            entry.setValue(Integer.parseInt(pref.getString(entry.getKey(), entry.getValue() + "")));
        }

        for (Map.Entry<String, String> entry : S.entrySet()) {
            entry.setValue(pref.getString(entry.getKey(), entry.getValue()));
        }

        //祝日データの読み込み=サポートするカレンダー範囲

        CAL_FR = Calendar.getInstance();
        CAL_TO = Calendar.getInstance();
        HOLIDAY = new HashMap<String, String>();

        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open("Holiday.csv");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            int n = 0;
            String[] ss = null;
            String[] st = null;

            while ((line = br.readLine()) != null) {
                n++;
                if (n == 1) continue; //1行目は飛ばす

                ss = line.split(","); //2019/1/1,火,元日,,,

                if (n == 2) { //サポート開始日付を取得
                    st = ss[0].split("/");
                    //CAL_FR.set(Integer.parseInt(st[0]),Integer.parseInt(st[1]) - 1,1); //当該年月の1日
                    CAL_FR.set(Integer.parseInt(st[0]), 0, 1, 0, 0, 0); //当該年の元日 00:00:00
                    log("CAL_FR.y=" + CAL_FR.get(Calendar.YEAR) + ",m=" + (CAL_FR.get(Calendar.MONTH) + 1) + ",d=" + CAL_FR.get(Calendar.DATE));
                }

                HOLIDAY.put(ss[0], ss[2]); //日付と祝日名をセット
            }
            br.close();

            if (ss != null) { //サポート終了日付を取得
                st = ss[0].split("/");
                //CAL_TO.set(Integer.parseInt(st[0]),Integer.parseInt(st[1]),0); //次の月の0日＝当該年月の最終日(31日など)
                CAL_TO.set(Integer.parseInt(st[0]), 11, 31, 23, 59, 59); //当該年の大晦日 23:59:59
                log("CAL_TO.y=" + CAL_TO.get(Calendar.YEAR) + ",m=" + (CAL_TO.get(Calendar.MONTH) + 1) + ",d=" + CAL_TO.get(Calendar.DATE));
            }

        } catch (Exception e) {
            Log.e("cApplication.onCreate", e.toString());
        }

    }

    public void hookTouchScreen(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void save() {
        SharedPreferences pref = getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        N.put("SAVE_EXIST", 1);

        for (Map.Entry<String, Integer> entry : N.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue() + "");
        }

        for (Map.Entry<String, String> entry : S.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }

        editor.commit();
    }

    //ログ出力ラッパー
    public void log(String head, String body) {
        if (DEBUG) Log.d(head, body);
    }


    public String displayDate(Calendar cal, int type) {
        if (cal == null) {
            if (type == 1) return "----/--/--";
            return "----年--月--日";
        }
        String[] weeknames = {"日", "月", "火", "水", "木", "金", "土"};
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        String str = cal.get(Calendar.YEAR) + "年" + (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DATE) + "日 ";
        String ymd = cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE);
        if (type == 1) {
            //ymd += "（" + weeknames[w] + "）";
            str = ymd;
        } else {
            if (w == 0 || HOLIDAY.containsKey(ymd)) {
                str += "<font color='red'>(" + weeknames[w] + ")</font>";
            } else if (w == 6) {
                str += "<font color='blue'>(" + weeknames[w] + ")</font>";
            } else {
                str += "(" + weeknames[w] + ")";
            }
        }
        return str;
    }

    public int diffCalendar(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) return 0;

        //==== ミリ秒単位での差分算出 ====//
        long diffTime = cal1.getTimeInMillis() - cal2.getTimeInMillis();

        //==== 日単位に変換 ====//
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        int diffDays = (int) (diffTime / MILLIS_OF_DAY);

        return diffDays;
    }

    public boolean equalsDate(Calendar cal_a, Calendar cal_b) {
        return cal_a.get(Calendar.YEAR) == cal_b.get(Calendar.YEAR) &&
                cal_a.get(Calendar.MONTH) == cal_b.get(Calendar.MONTH) &&
                cal_a.get(Calendar.DATE) == cal_b.get(Calendar.DATE);
    }

    public Calendar dateStringToCalendar(String str) {
        Calendar cal = Calendar.getInstance();
        int y = 1980, m = 0, d = 1, h = 0, mm = 0;
        String[] ss, st, su;
        if (str == null || str == "" || str.contains("-")) {
            return null; //empty
        } else if (str.length() == 12) { //YYYYMMDDHHMM
            y = Integer.parseInt(str.substring(0, 4));
            m = Integer.parseInt(str.substring(4, 6));
            d = Integer.parseInt(str.substring(6, 8));
            h = Integer.parseInt(str.substring(8, 10));
            mm = Integer.parseInt(str.substring(10, 12));
        } else if (str.contains(" ")) { //YYYY/MM/DD HH:MM
            ss = str.split(" ");
            if (!ss[0].contains("-")) {
                st = ss[0].split("/");
                y = Integer.parseInt(st[0]);
                m = Integer.parseInt(st[1]);
                d = Integer.parseInt(st[2]);
            }
            if (!ss[1].contains("-")) {
                su = ss[1].split(":");
                h = Integer.parseInt(su[0]);
                mm = Integer.parseInt(su[1]);
            }
        } else if (str.contains("/")) { //YYYY/MM/DD
            if (!str.contains("-")) {
                st = str.split("/");
                y = Integer.parseInt(st[0]);
                m = Integer.parseInt(st[1]);
                d = Integer.parseInt(st[2]);
            }
        } else if (str.contains(":")) { //HH:MM
            if (!str.contains("-")) {
                su = str.split(":");
                h = Integer.parseInt(su[0]);
                mm = Integer.parseInt(su[1]);
            }
        }
        cal.set(y, m - 1, d, h, mm, 0);
        return cal;
    }

} //end of class

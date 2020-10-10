// アクティビティ
package net.servernote.pulldownbutton;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

public class cActivity extends AppCompatActivity implements LocationListener, cDialogListener {
    public static final short V_HOME = 0;
    public static final short V_SUGGEST_DIALOG = 1;
    public static final short V_TIME_DIALOG = 2;
    public static final short V_CALENDAR_DIALOG = 3;
    public static final short V_CHOICE_DIALOG = 4;
    public static final short V_YESNO_DIALOG = 5;
    public static final short V_LOADINGEX_DIALOG = 6;
    public static final short V_LOADING_DIALOG = 7;
    public static final short V_ALERT_DIALOG = 8;
    public static final int A_GPSLOCATION_ON = 1;
    public static final int A_FOCUS_WINDOW = 2;
    public static final int A_END_ACTIVITY = 3;
    public static final int A_GOT_LOCATION = 4;
    public static final short D_DEFAULT = 0;
    public static final short D_GPSLOCATION_ON = 1;
    public cApplication G;
    public boolean FIRST_FOCUS_WINDOW;
    public ArrayList<View> ROOTVIEWS;
    public ArrayList<cBaseView> FRAMEVIEWS;
    public short FRAMEVIEW_NOW;
    public cHomeView HOME_VIEW;
    public cDialog SUGGEST_DIALOG;
    public cDialog TIME_DIALOG;
    public cDialog CALENDAR_DIALOG;
    public cDialog CHOICE_DIALOG;
    public cDialog YESNO_DIALOG;
    public cDialog LOADINGEX_DIALOG;
    public cDialog LOADING_DIALOG;
    public cDialog ALERT_DIALOG;
    public LocationManager LOCATIONMANAGER;

    private void log(String s) {
        G.log("cActivity", s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        G = (cApplication) getApplication();
        FIRST_FOCUS_WINDOW = true;
        ROOTVIEWS = new ArrayList<View>();
        FRAMEVIEWS = new ArrayList<cBaseView>();
        FRAMEVIEW_NOW = (-1);
        LOCATIONMANAGER = null;

        if (G.N.get("SAVE_EXIST") == 0) { //Create Desktop Shortcut
            String shortcutTitle = getResources().getString(R.string.app_name);
            Parcelable icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.app_icon);
            Intent shortcut_intent = new Intent(Intent.ACTION_MAIN);
            shortcut_intent.setClassName(this, this.getClass().getName());
            shortcut_intent.addCategory(Intent.CATEGORY_LAUNCHER);
            Intent installIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            installIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut_intent);
            installIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            installIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);
            installIntent.putExtra("duplicate", false);
            sendBroadcast(installIntent);
            G.save();
        }

        setContentView(R.layout.root);
    }

    @Override
    public void setContentView(int layoutResID) {
        log("setContentView");
        super.setContentView(layoutResID);

        ROOTVIEWS.add(findViewById(R.id.home));
        ROOTVIEWS.add(findViewById(R.id.suggest_dialog));
        ROOTVIEWS.add(findViewById(R.id.time_dialog));
        ROOTVIEWS.add(findViewById(R.id.calendar_dialog));
        ROOTVIEWS.add(findViewById(R.id.choice_dialog));
        ROOTVIEWS.add(findViewById(R.id.yesno_dialog));
        ROOTVIEWS.add(findViewById(R.id.loadingex_dialog));
        ROOTVIEWS.add(findViewById(R.id.loading_dialog));
        ROOTVIEWS.add(findViewById(R.id.alert_dialog));
        for (int i = 0; i < ROOTVIEWS.size(); i++) {
            G.hookTouchScreen(ROOTVIEWS.get(i));
        }
        HOME_VIEW = new cHomeView(this, ROOTVIEWS.get(V_HOME));
        FRAMEVIEWS.add(HOME_VIEW);
        SUGGEST_DIALOG = new cDialog(this, ROOTVIEWS.get(V_SUGGEST_DIALOG));
        TIME_DIALOG = new cDialog(this, ROOTVIEWS.get(V_TIME_DIALOG));
        CALENDAR_DIALOG = new cDialog(this, ROOTVIEWS.get(V_CALENDAR_DIALOG));
        CHOICE_DIALOG = new cDialog(this, ROOTVIEWS.get(V_CHOICE_DIALOG));
        YESNO_DIALOG = new cDialog(this, ROOTVIEWS.get(V_YESNO_DIALOG));
        LOADINGEX_DIALOG = new cDialog(this, ROOTVIEWS.get(V_LOADINGEX_DIALOG));
        LOADING_DIALOG = new cDialog(this, ROOTVIEWS.get(V_LOADING_DIALOG));
        ALERT_DIALOG = new cDialog(this, ROOTVIEWS.get(V_ALERT_DIALOG));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        log("onWindowFocusChanged hasFocus=" + hasFocus);
        if (!hasFocus) return;
        if (FIRST_FOCUS_WINDOW) {
            FIRST_FOCUS_WINDOW = false;
            switchFrameView(V_HOME);
            //checkGPSPermission( true );
            //YESNO_DIALOG.show("この内容で登録します。\nよろしいですか？", this, D_DEFAULT);
            //LOADING_DIALOG.show( "処理中です...",this,D_DEFAULT,Gravity.LEFT );
            //LOADINGEX_DIALOG.show( "処理中です...",this,D_DEFAULT,Gravity.LEFT );
            //CHOICE_DIALOG.show( "居住地域",this,D_DEFAULT,Gravity.CENTER,R.array.prefectures,20 );
        }
        if (dialogIsShow()) return;

        for (int i = 0; i < FRAMEVIEWS.size(); i++) {
            FRAMEVIEWS.get(i).notifyFromActivity(A_FOCUS_WINDOW, Activity.RESULT_OK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult requestCode=" + requestCode + ",resultCode=" + resultCode);

        for (int i = 0; i < FRAMEVIEWS.size(); i++) {
            FRAMEVIEWS.get(i).notifyFromActivity(requestCode, resultCode);
        }
    }

    @Override
    protected void onPause() {
        log("onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        log("onResume");
        super.onResume();
    }


    public void switchFrameView(short frameview_index) {
        log("switchFrameView index=" + frameview_index);

        if (FRAMEVIEW_NOW == frameview_index) return;

        if (FRAMEVIEW_NOW >= 0) {
            FRAMEVIEWS.get(FRAMEVIEW_NOW).hide();
        }

        FRAMEVIEWS.get(frameview_index).show();

        FRAMEVIEW_NOW = frameview_index;
    }

    public boolean dialogIsShow() {
        for (short i = V_SUGGEST_DIALOG; i <= V_ALERT_DIALOG; i++) {
            if (ROOTVIEWS.get(i).getVisibility() == View.VISIBLE) return true; //ダイアログ表示中
        }
        return false;
    }

    @Override
    public void finish() {
        log("finish");
        stopWatchLocation();

        for (int i = 0; i < FRAMEVIEWS.size(); i++) {
            FRAMEVIEWS.get(i).notifyFromActivity(A_END_ACTIVITY, Activity.RESULT_OK);
        }

        super.finish();
    }

    @Override
    protected void onDestroy() {
        log("onDestroy");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        log("onBackPressed");

        if (dialogIsShow()) return;

        if (FRAMEVIEW_NOW == V_HOME) {
            super.onBackPressed();
        } else {
            FRAMEVIEWS.get(FRAMEVIEW_NOW).back();
        }
    }

// implements cDialogListener

    public void onDialogYesClick(cDialog dialog) {
        log("onDialogYesClick");
        dialog.hide();
    }

    public void onDialogCloseClick(cDialog dialog) {
        log("onDialogCloseClick");

        dialog.hide();
        if (dialog.ACTION_ID == D_GPSLOCATION_ON) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, A_GPSLOCATION_ON);
        }
    }

    public void onDialogChoice(cDialog dialog, int index, String text) {
        log("onDialogChoice,index=" + index + ",text=" + text);

        dialog.hide();

    }

    public boolean checkGPSPermission(boolean showDialog) {
        // Android 6, API 23以上は Runtime Permission必須
        log("API_LEVEL=" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT < 23 || //またはすでに許可されている
                PermissionChecker.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            log("check_fine_location granted");
            return true;
        }
        log("check_fine_location not granted");
        if (showDialog) {
            if (!dialogIsShow()) {
                ALERT_DIALOG.show("現在地から検索するには位置情報の許可が必要です。", this, D_GPSLOCATION_ON);
            }
        }
        return false;
    }

    public boolean startWatchLocation(boolean gps_provider) {
        log("startWatchLocation");

        stopWatchLocation();

        if (LOCATIONMANAGER == null) {
            LOCATIONMANAGER = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        if (LOCATIONMANAGER == null) {
            log("LocationManager get failed null");
            return false;
        }
        LOCATIONMANAGER.requestLocationUpdates(
                gps_provider ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER, 0, 0, this);

        return true;
    }

    public void stopWatchLocation() {
        log("stopWatchLocation");
        if (LOCATIONMANAGER != null) {
            LOCATIONMANAGER.removeUpdates(this);
            LOCATIONMANAGER = null;
        }
    }

//implements LocationListener

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        log("onRequestPermissionsResult,requestCode=" + requestCode);

        switch (requestCode) {
            case A_GPSLOCATION_ON:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("result_fine_location granted");
                } else {
                    log("result_fine_location not granted");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        log("onLocationChanged,lat=" + location.getLatitude() + ",lon=" + location.getLongitude() + ",alt=" + location.getAltitude());

        G.S.put("CLIENT_LATITUDE", "" + location.getLatitude());
        G.S.put("CLIENT_LONGITUDE", "" + location.getLongitude());
        G.save();

        stopWatchLocation();

        for (int i = 0; i < FRAMEVIEWS.size(); i++) {
            FRAMEVIEWS.get(i).notifyFromActivity(A_GOT_LOCATION, Activity.RESULT_OK);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        log("onProviderDisabled," + provider);
        if (provider.toLowerCase().contains("network")) {
            startWatchLocation(true);
        } else {
            stopWatchLocation();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        log("onProviderEnabled," + provider);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("onStatusChanged," + provider + ",status=" + status);

    }

} //end of class

// ベースビュー
package net.servernote.pulldownbutton;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class cBaseView implements View.OnClickListener {
    public cApplication G;
    public cActivity ACTIVITY;
    public Resources RESOURCES;
    public View VIEW;
    public short ACTION_ID;
    public String ACTION_STRING;
    public float DISP_PXW;
    public float DISP_PXH;
    public float DISP_DPW;
    public float DISP_DPH;

    public cBaseView(cActivity activity, View view) {
        ACTIVITY = activity;
        G = (cApplication) activity.getApplication();
        RESOURCES = activity.getResources();
        VIEW = view;
        ACTION_ID = 0;
        ACTION_STRING = null;
        DISP_PXW = 0;
        DISP_PXH = 0;
        DISP_DPW = 0;
        DISP_DPH = 0;
    }

    private void log(String s) {
        G.log("cBaseView", s);
    }

    public void show() {
        log("show");

        VIEW.setVisibility(View.VISIBLE);
    }

    public void hide() {
        log("hide");

        VIEW.setVisibility(View.INVISIBLE);
    }

    public void draw() {
        log("draw");
        //drawのタイミングで画面サイズ取得すればOrientationに対応が多少楽
        WindowManager wm = (WindowManager) ACTIVITY.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        DISP_PXW = disp.getWidth();
        DISP_PXH = disp.getHeight();
        DISP_DPW = pxToDp(DISP_PXW);
        DISP_DPH = pxToDp(DISP_PXH);
        log("pxw=" + DISP_PXW + ",pxh=" + DISP_PXH + ",dpw=" + DISP_DPW + ",dph=" + DISP_DPH);
    }

    public void back() {
    }

    public void notifyFromActivity(int requestCode, int resultCode) {
    }

    public void onClick(View view) {
    }

    public float dpToPx(float dp) {
        DisplayMetrics metrics = RESOURCES.getDisplayMetrics();
        return dp * metrics.density;
    }

    public float pxToDp(float px) {
        DisplayMetrics metrics = RESOURCES.getDisplayMetrics();
        return px / metrics.density;
    }


} //end of class

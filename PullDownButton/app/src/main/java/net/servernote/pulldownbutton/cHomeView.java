// ホームビュー
package net.servernote.pulldownbutton;

import android.app.Activity;
import android.view.View;

import java.util.ArrayList;

public class cHomeView extends cBaseView implements cDialogListener {
    public static final short V_ARROW_BUTTON_TEST = 0;
    public static final short D_DEFAULT = 0;
    public ArrayList<View> FRAMEVIEWS;
    public short FRAMEVIEW_NOW;

    public cHomeView(cActivity activity, View view) {
        super(activity, view);

        FRAMEVIEWS = new ArrayList<View>();
        FRAMEVIEWS.add(VIEW.findViewById(R.id.home_arrow_button_test));

        FRAMEVIEW_NOW = (-1);
    }

    private void log(String s) {
        G.log("cHomeView", s);
    }

    @Override
    public void show() {
        log("show");

        prepareArrowButtonTest();

        switchFrameView(V_ARROW_BUTTON_TEST);
        super.show();
    }

    public void prepareArrowButtonTest() {
        log("prepareArrowButtonTest");
    }

    @Override
    public void hide() {
        log("hide");
        //super.hide(); //ホームは最下層なので隠れない
    }

    @Override
    public void back() {
        log("back");
        switch (FRAMEVIEW_NOW) {
            default:
                break;
        }
    }

    public void switchFrameView(short frameview_index) {
        log("switchFrameView index=" + frameview_index);
        if (FRAMEVIEW_NOW != frameview_index) {
            if (FRAMEVIEW_NOW >= 0) {
                FRAMEVIEWS.get(FRAMEVIEW_NOW).setVisibility(View.INVISIBLE);
            }
            FRAMEVIEW_NOW = frameview_index;
        }
        switch (FRAMEVIEW_NOW) {
            case V_ARROW_BUTTON_TEST:
                previewArrowButtonTest();
                break;
            default:
                break;
        }
        draw();
        FRAMEVIEWS.get(FRAMEVIEW_NOW).setVisibility(View.VISIBLE);
    }

    public void previewArrowButtonTest() {
        log("previewArrowButtonTest");
    }

    @Override
    public void draw() {
        log("draw");
        super.draw();

        switch (FRAMEVIEW_NOW) {
            case V_ARROW_BUTTON_TEST:
                drawArrowButtonTest();
                break;
            default:
                break;
        }
    }

    public void drawArrowButtonTest() {
        log("drawArrowButtonTest");
    }

    @Override
    public void notifyFromActivity(int requestCode, int resultCode) {
        log("notifyFromActivity requestCode=" + requestCode + ",resultCode=" + resultCode);
        switch (requestCode) {
            case cActivity.A_FOCUS_WINDOW:
                if (resultCode == Activity.RESULT_OK) {
                    draw();
                }
                break;
            case cActivity.A_END_ACTIVITY:

                break;
            case cActivity.A_GOT_LOCATION:
                draw();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int i, x, tag, id = view.getId();
        log("onClick id = " + id);

        switch (id) {
            default:
                break;

        }
    }

    public void onDialogYesClick(cDialog dialog) {
        log("onDialogYesClick");
        dialog.hide();
    }

    public void onDialogCloseClick(cDialog dialog) {
        log("onDialogCloseClick");
        dialog.hide();
    }

    public void onDialogChoice(cDialog dialog, int index, String text) {
        log("onDialogChoice,index=" + index + ",text=" + text);
        dialog.hide();
    }

} //end of class

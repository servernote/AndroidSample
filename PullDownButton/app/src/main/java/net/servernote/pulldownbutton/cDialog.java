// ダイアログビュー
package net.servernote.pulldownbutton;

import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class cDialog extends cBaseView implements TextWatcher {
    public cDialogListener LISTENER;
    public String[] CHOICES;
    public String VAL_STRINGS;
    public Calendar CAL_NOW;
    public Calendar CAL_PIN;
    public TextView TV_PIN;
    public NumberPicker H_PICKER;
    public NumberPicker M_PICKER;

    public cDialog(cActivity activity, View view) {
        super(activity, view);

        int i, j;

        View btn = VIEW.findViewById(R.id.dialog_yes);
        if (btn != null) {
            btn.setOnClickListener(this);
        }

        btn = VIEW.findViewById(R.id.dialog_close);
        if (btn != null) {
            btn.setOnClickListener(this);
        }

        btn = VIEW.findViewById(R.id.dialog_prev_month);
        if (btn != null) {
            btn.setOnClickListener(this);
        }

        btn = VIEW.findViewById(R.id.dialog_next_month);
        if (btn != null) {
            btn.setOnClickListener(this);
        }

        //btn = VIEW.findViewById(R.id.dialog_time_text);
        //if (btn != null) {
        //    btn.setOnClickListener(this);
        // }

        btn = VIEW.findViewById(R.id.dialog_search);
        if (btn != null) {
            btn.setOnClickListener(this);
        }

        btn = VIEW.findViewById(R.id.dialog_gps);
        if (btn != null) {
            btn.setOnClickListener(this);
        }


        H_PICKER = null;
        M_PICKER = null;
        NumberPicker np = VIEW.findViewById(R.id.dialog_h_picker);
        if (np != null) {
            H_PICKER = np;
            np.setDisplayedValues(null);
            List<String> np_l = new ArrayList<>();
            for (i = 0; i < 24; i++) {
                if (i < 10) np_l.add("0" + i);
                else np_l.add("" + i);
            }
            String[] np_a = np_l.toArray(new String[np_l.size()]);
            np.setDisplayedValues(np_a);
            np.setMinValue(0);
            np.setMaxValue(23);
            np = VIEW.findViewById(R.id.dialog_m_picker);
            M_PICKER = np;
            np.setDisplayedValues(null);
            np_l.clear();
            for (i = 0; i < 60; i++) {
                if (i < 10) np_l.add("0" + i);
                else np_l.add("" + i);
            }
            np_a = np_l.toArray(new String[np_l.size()]);
            np.setDisplayedValues(np_a);
            np.setMinValue(0);
            np.setMaxValue(59);
        }

        LISTENER = null;
        CHOICES = null;
        VAL_STRINGS = null;

        CAL_NOW = null;
        CAL_PIN = null;
        TV_PIN = null;

        if (VIEW.findViewById(R.id.calendar_dialog) == null) return;

        //カレンダーのハコをinflateしておく
        LinearLayout lla = VIEW.findViewById(R.id.dialog_list);
        LinearLayout llb, llc;
        for (i = 0; i < 7; i++) {
            llb = (LinearLayout) ACTIVITY.getLayoutInflater().inflate(R.layout.calendar_dialog_row, null);
            llb.setTag("ROW-" + i);
            for (j = 0; j < 7; j++) {
                llc = (LinearLayout) ACTIVITY.getLayoutInflater().inflate(R.layout.calendar_dialog_col, null);
                llc.setTag("COL-" + j);
                llb.addView(llc);
            }
            lla.addView(llb);
        }
    }

    private void log(String s) {
        G.log("cDialog", s);
    }

    public void show(String title, cDialogListener listener, short actionid) {
        log("show-default");

        show(title, listener, actionid, Gravity.CENTER);
    }

    public void show(String title, cDialogListener listener, short actionid, int gravity) {
        log("show-gravity");

        ((TextView) VIEW.findViewById(R.id.dialog_text)).setGravity(gravity);
        ((TextView) VIEW.findViewById(R.id.dialog_text)).setText(title);
        LISTENER = listener;
        ACTION_ID = actionid;
        VIEW.setVisibility(View.VISIBLE);
    }

    //val_strings != null = マルチセレクトモード,null=シングルセレクトモード
    public void show(String title, cDialogListener listener, short actionid, int gravity, String val_strings, int rid_strings, final int now_index) {
        log("show-choice");
        CHOICES = RESOURCES.getStringArray(rid_strings);
        if (val_strings != null) VAL_STRINGS = "/" + val_strings + "/";
        else VAL_STRINGS = null;
        int i;
        final int n = CHOICES.length;
        final ScrollView scroll = VIEW.findViewById(R.id.dialog_scroll);
        final LinearLayout lla = VIEW.findViewById(R.id.dialog_list);
        lla.removeAllViews();
        LinearLayout llb;
        RelativeLayout rla;
        TextView tv;
        for (i = 0; i < n; i++) {
            llb = (LinearLayout) ACTIVITY.getLayoutInflater().inflate(R.layout.choice_dialog_row, null);
            rla = llb.findViewById(R.id.choice_dialog_row_content);
            rla.setOnClickListener(this);
            rla.setTag(i);
            if (VAL_STRINGS != null) {
                if (VAL_STRINGS.contains("/" + CHOICES[i] + "/")) {
                    rla.setBackgroundColor(RESOURCES.getColor(R.color.lightyellow));
                    rla.findViewById(R.id.choice_dialog_row_content_check).setVisibility(View.VISIBLE);
                } else {
                    rla.setBackgroundColor(RESOURCES.getColor(R.color.white));
                }
            } else {
                rla.setBackgroundResource(R.drawable.selector_solid_lightyellow);
            }
            tv = rla.findViewById(R.id.choice_dialog_row_content_text);
            tv.setText(CHOICES[i]);
            lla.addView(llb);
            if (i == now_index) {
                rla.setBackgroundColor(Color.rgb(0xf7, 0xef, 0xdb));
            }
        }
        final ViewTreeObserver observer = lla.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int h = lla.getHeight() / n;
                        if (now_index >= 0) scroll.scrollTo(0, now_index * h);
                        else scroll.scrollTo(0, 0);
                        observer.removeOnGlobalLayoutListener(this);
                    }
                });
        show(title, listener, actionid, gravity);
    }

    public void show(String title, cDialogListener listener, short actionid, int gravity, int year, int month, int day) {
        log("show-calendar");

        CAL_NOW = Calendar.getInstance();
        if (year < G.CAL_FR.get(Calendar.YEAR) || year > G.CAL_TO.get(Calendar.YEAR)) { //サポート範囲外の場合現在日付
            year = CAL_NOW.get(Calendar.YEAR);
            month = CAL_NOW.get(Calendar.MONTH) + 1;
            day = CAL_NOW.get(Calendar.DATE);
        }
        CAL_NOW.set(year, month - 1, 1);
        CAL_PIN = Calendar.getInstance();
        CAL_PIN.set(year, month - 1, day);

        drawCalendar();

        show(title, listener, actionid, gravity);
    }

    public void drawCalendar() {

        TV_PIN = null;
        Calendar c = Calendar.getInstance();
        c.set(CAL_NOW.get(Calendar.YEAR), CAL_NOW.get(Calendar.MONTH) + 1, 0);
        int i, j, x = 0, x_end = c.get(Calendar.DATE);

        String str = CAL_NOW.get(Calendar.YEAR) + "<small><small>年</small></small>" + (CAL_NOW.get(Calendar.MONTH) + 1) + "<small><small>月</small></small>";
        //str += 12 + "<small><small>日</small></small>";

        ((TextView) VIEW.findViewById(R.id.dialog_date_text)).setText(Html.fromHtml(str));

        //str = "<u>10:00</u>";
        //((TextView)VIEW.findViewById( R.id.dialog_time_text )).setText( Html.fromHtml(str) );

        //VIEW.findViewById( R.id.dialog_prev_month ).setVisibility( CAL_NOW.get(Calendar.YEAR) <= 2000 && CAL_NOW.get(Calendar.MONTH) <= 0 ? View.INVISIBLE:View.VISIBLE );
        //VIEW.findViewById( R.id.dialog_next_month ).setVisibility( CAL_NOW.get(Calendar.YEAR) >= 2050 && CAL_NOW.get(Calendar.MONTH) >= 11 ? View.INVISIBLE:View.VISIBLE );

        if (CAL_NOW.get(Calendar.YEAR) == G.CAL_FR.get(Calendar.YEAR) && CAL_NOW.get(Calendar.MONTH) == G.CAL_FR.get(Calendar.MONTH)) {
            VIEW.findViewById(R.id.dialog_prev_month).setVisibility(View.INVISIBLE);
        } else {
            VIEW.findViewById(R.id.dialog_prev_month).setVisibility(View.VISIBLE);
        }

        if (CAL_NOW.get(Calendar.YEAR) == G.CAL_TO.get(Calendar.YEAR) && CAL_NOW.get(Calendar.MONTH) == G.CAL_TO.get(Calendar.MONTH)) {
            VIEW.findViewById(R.id.dialog_next_month).setVisibility(View.INVISIBLE);
        } else {
            VIEW.findViewById(R.id.dialog_next_month).setVisibility(View.VISIBLE);
        }

        LinearLayout lla = VIEW.findViewById(R.id.dialog_list);
        LinearLayout llb, llc;
        TextView tv;
        String[] weeknames = {"日", "月", "火", "水", "木", "金", "土"};
        for (i = 0; i < 7; i++) {
            llb = lla.findViewWithTag("ROW-" + i);
            for (j = 0; j < 7; j++) {
                llc = llb.findViewWithTag("COL-" + j);
                tv = llc.findViewById(R.id.calendar_dialog_col_text);
                tv.setBackgroundColor(Color.WHITE);
                tv.setText("");
                tv.setOnClickListener(null);
                if (j == 0) tv.setTextColor(Color.RED);
                else if (j == 6) tv.setTextColor(Color.BLUE);
                else tv.setTextColor(Color.BLACK);
                if (i == 0) {
                    tv.setText(weeknames[j]);
                    tv.setBackgroundColor(RESOURCES.getColor(R.color.lightyellow));
                } else if (i == 1) {
                    if ((j + 1) == CAL_NOW.get(Calendar.DAY_OF_WEEK)) x = 1;
                }
                if (x > 0 && x <= x_end) {
                    tv.setText("" + x);
                    tv.setOnClickListener(this);
                    tv.setTag(x);
                    String ymd = CAL_NOW.get(Calendar.YEAR) + "/" + (CAL_NOW.get(Calendar.MONTH) + 1) + "/" + x;
                    if (G.HOLIDAY.containsKey(ymd)) {
                        tv.setTextColor(Color.RED);
                    }
                    if (CAL_NOW.get(Calendar.YEAR) == CAL_PIN.get(Calendar.YEAR) &&
                            CAL_NOW.get(Calendar.MONTH) == CAL_PIN.get(Calendar.MONTH) &&
                            x == CAL_PIN.get(Calendar.DATE)) {
                        tv.setBackgroundColor(RESOURCES.getColor(R.color.palegreen));
                        TV_PIN = tv;
                    }
                    x++;
                }
            }
        }
    }

    public void show(String title, cDialogListener listener, short actionid, int gravity, int year, int month, int day, int hour, int minute) {
        log("show-time");

        TextView tv = VIEW.findViewById(R.id.dialog_date_text);
        if (year < 0) {
            tv.setVisibility(View.GONE);
        } else {
            String str = year + "<small><small>年</small></small>" + month + "<small><small>月</small></small>" + day + "<small><small>日</small></small>";
            tv.setText(Html.fromHtml(str));
            tv.setVisibility(View.VISIBLE);
        }

        ((NumberPicker) VIEW.findViewById(R.id.dialog_h_picker)).setValue(hour);
        ((NumberPicker) VIEW.findViewById(R.id.dialog_m_picker)).setValue(minute);

        show(title, listener, actionid, gravity);
    }

    public void show(String title, cDialogListener listener, short actionid, int gravity, String defkey) {
        log("show-suggest");

        EditText ed = VIEW.findViewById(R.id.dialog_edit);
        ed.removeTextChangedListener(this);

        LinearLayout lla = VIEW.findViewById(R.id.dialog_list);
        lla.removeAllViews();

        if (defkey != null && !defkey.equals("現在地")) {
            ed.setText(defkey);
        } else {
            ed.setText("");
        }

        ed.addTextChangedListener(this);

        show(title, listener, actionid, gravity);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        log("onClick id = " + id);
        int i;
        LinearLayout lla, llb, llc;
        RelativeLayout rla;
        String str;
        TextView tv;

        if (LISTENER == null) return;

        switch (id) {
            case R.id.dialog_yes:
                LISTENER.onDialogYesClick(this);
                break;
            case R.id.dialog_close:
                if (VAL_STRINGS != null) {
                    str = "";
                    lla = VIEW.findViewById(R.id.dialog_list);
                    for (i = 0; i < lla.getChildCount(); i++) {
                        llb = (LinearLayout) lla.getChildAt(i);
                        rla = llb.findViewById(R.id.choice_dialog_row_content);
                        if (rla.findViewById(R.id.choice_dialog_row_content_check).getVisibility() == View.VISIBLE) {
                            if (str != "") str = str + "/";
                            str = str + CHOICES[(int) rla.getTag()];
                        }
                    }
                    log("str=" + str);
                    LISTENER.onDialogChoice(this, (-1), str);
                } else {
                    LISTENER.onDialogCloseClick(this);
                }
                break;
            case R.id.choice_dialog_row_content:
                i = (int) view.getTag();
                if (VAL_STRINGS == null) {
                    LISTENER.onDialogChoice(this, i, CHOICES[i]);
                } else {
                    rla = (RelativeLayout) view;
                    View check = rla.findViewById(R.id.choice_dialog_row_content_check);
                    if (check.getVisibility() == View.INVISIBLE) {
                        rla.setBackgroundColor(RESOURCES.getColor(R.color.lightyellow));
                        check.setVisibility(View.VISIBLE);
                    } else {
                        rla.setBackgroundColor(RESOURCES.getColor(R.color.white));
                        check.setVisibility(View.INVISIBLE);
                    }
                }
                break;
            //case R.id.dialog_time_text:
            //	VIEW.findViewById(R.id.dialog_list).setVisibility(View.GONE);
            //	VIEW.findViewById(R.id.dialog_timepicker).setVisibility(View.VISIBLE);
            //break;
            case R.id.dialog_prev_month:
                CAL_NOW.set(Calendar.MONTH, CAL_NOW.get(Calendar.MONTH) - 1);
                drawCalendar();
                break;
            case R.id.dialog_next_month:
                CAL_NOW.set(Calendar.MONTH, CAL_NOW.get(Calendar.MONTH) + 1);
                drawCalendar();
                break;
            case R.id.calendar_dialog_col_text:
                if (view != TV_PIN) {
                    i = (int) view.getTag();
                    if (TV_PIN != null) {
                        TV_PIN.setBackgroundColor(Color.WHITE);
                    }
                    TV_PIN = (TextView) view;
                    TV_PIN.setBackgroundColor(RESOURCES.getColor(R.color.palegreen));
                    CAL_PIN.set(CAL_NOW.get(Calendar.YEAR), CAL_NOW.get(Calendar.MONTH), i);
                }
                break;
            case R.id.dialog_search:

                break;
            case R.id.dialog_gps:
                LISTENER.onDialogChoice(this, (-1), "GPS,現在地");
                break;
            case R.id.suggest_dialog_row_content:
                llc = (LinearLayout) view;
                tv = llc.findViewById(R.id.suggest_dialog_row_content_text);
                LISTENER.onDialogChoice(this, (-1), llc.getTag() + "," + tv.getText().toString());
                break;
            default:
                break;
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        // テキスト変更後に変更されたテキストを取り出す
        String inputStr = s.toString();

        log("inputStr=" + inputStr);

        if (inputStr.equals("会津")) {
            log("go infrate wakamatu");
            LinearLayout lla = VIEW.findViewById(R.id.dialog_list);
            lla.removeAllViews();
            LinearLayout llb, llc;
            TextView tv;

            String[] ss = {"会津若松", "会津武家屋敷"};
            String[] si = {"AI000011112222", "AI333344445555"};

            int i;

            for (i = 0; i < ss.length; i++) {
                llb = (LinearLayout) ACTIVITY.getLayoutInflater().inflate(R.layout.suggest_dialog_row, null);
                llc = llb.findViewById(R.id.suggest_dialog_row_content);
                llc.setOnClickListener(this);
                llc.setTag(si[i]);
                tv = llc.findViewById(R.id.suggest_dialog_row_content_text);
                tv.setText(ss[i]);
                lla.addView(llb);
            }


        }


    }


} //end of class

// アクティビティ
package net.servernote.getwebviewhtml;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener,
        TextView.OnEditorActionListener, View.OnFocusChangeListener {

    public HashMap<String, Integer> N;
    public HashMap<String, String> S;
    private WebView WEBVIEW;
    private EditText KEYWORD;
    private Handler HANDLER = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //プリファレンスのロード

        N = new HashMap<String, Integer>();
        S = new HashMap<String, String>();

        N.put("FILE_COUNTER", 0);
        S.put("KEYWORD", "");
        S.put("LAST_WEB_URL", "https://www.google.co.jp/");

        SharedPreferences pref = getSharedPreferences("Preferences", MODE_PRIVATE);

        for (Map.Entry<String, Integer> entry : N.entrySet()) {
            entry.setValue(Integer.parseInt(pref.getString(entry.getKey(), entry.getValue() + "")));
        }

        for (Map.Entry<String, String> entry : S.entrySet()) {
            entry.setValue(pref.getString(entry.getKey(), entry.getValue()));
        }

        //レイアウト

        setContentView(R.layout.main);

        KEYWORD = findViewById(R.id.keyword);
        KEYWORD.setText(S.get("KEYWORD"));
        KEYWORD.setOnEditorActionListener(this);
        KEYWORD.setOnFocusChangeListener(this);

        findViewById(R.id.search).setOnClickListener(this);

        WEBVIEW = findViewById(R.id.webview);
        WEBVIEW.getSettings().setJavaScriptEnabled(true);
        WEBVIEW.setWebViewClient(new ViewSourceClient());
        WEBVIEW.addJavascriptInterface(this, "activity");
        WEBVIEW.loadUrl(S.get("LAST_WEB_URL"));
    }

    @Override
    protected void onDestroy() {

        //プリファレンスのセーブ

        SharedPreferences pref = getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        for (Map.Entry<String, Integer> entry : N.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue() + "");
        }

        for (Map.Entry<String, String> entry : S.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }

        editor.commit();

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.search:
                doSearch();
                break;
            default:
                break;
        }
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            doSearch();
        }
        return true; // falseを返すと, IMEがSearch→Doneへと切り替わる
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            hideKeyboard();
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(KEYWORD.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void doSearch() {
        hideKeyboard();

        String str = KEYWORD.getText().toString();
        S.put("KEYWORD", str);

        if (str.length() <= 0) return;

        try {
            String enc = URLEncoder.encode(str, "UTF-8");
            String url = "https://www.google.co.jp/search?q=" + enc;
            S.put("LAST_WEB_URL", url);
            WEBVIEW.loadUrl(url);
        } catch (Exception e) {
            Log.e("doSearch", e.toString());
        }
    }

    @JavascriptInterface
    public void viewSource(final String src) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                int counter = N.get("FILE_COUNTER");
                counter++;
                String file = "output." + counter + ".html";
                N.put("FILE_COUNTER", counter);
                try {
                    writeTextFile(file, src);
                    Log.d("viewSource", "saved to " + file);
                } catch (Exception e) {
                    Log.e("viewSource", e.toString());
                }
            }
        });
    }

    public String readTextFile(String file) throws Exception {
        String ret = "";
        FileInputStream in = openFileInput(file);
        byte[] buf = new byte[in.available()];
        in.read(buf);
        ret = new String(buf).trim();
        in.close();
        return ret;
    }

    public void writeTextFile(String file, String text) throws Exception {
        FileOutputStream out = openFileOutput(file, Context.MODE_PRIVATE);
        out.write(text.getBytes());
        out.close();
    }

    private static class ViewSourceClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.activity.viewSource(document.documentElement.outerHTML);");
        }
    }

} //end of class

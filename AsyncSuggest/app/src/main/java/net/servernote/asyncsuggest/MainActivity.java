package net.servernote.asyncsuggest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnKeyListener {

    private EditText mEditText;
    private TextView mTextView;
    private String mLastInput;  // 直前の入力保存用
    private MyAsyncTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText = (EditText)findViewById(R.id.input_text);
        mEditText.addTextChangedListener(this);
        mEditText.setOnKeyListener(this);
        mTextView = (TextView)findViewById(R.id.output_text);
        mLastInput = "";
        mTask = null;
    }

    // implements TextWatcher
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    // implements TextWatcher
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    // implements TextWatcher
    @Override
    public void afterTextChanged(Editable s) {
        String inputStr= s.toString();
        Log.d("MainActivity", "input="+inputStr);
        if(!inputStr.equals(mLastInput)){ // 直前入力と異なっていたら処理
            mLastInput = inputStr; // 直前入力保存
            if(mTask != null){ // サジェスト通信処理中ならキャンセル指令を出す
                mTask.cancel(true);
                mTask = null;
            }
            if(inputStr.length() > 0) { // 入力ありならサジェスト通信処理開始
                mTask = new MyAsyncTask(mTextView);
                mTask.execute(mLastInput);
            }
            else{ // 空なら結果画面をクリアする
                mTextView.setText("");
            }
        }
    }

    // implements View.OnKeyListener
    // ENTERキー入力で、キーボードを閉じる。
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_ENTER) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    // サジェスト通信処理を行うクラス
    private class MyAsyncTask extends AsyncTask<String, Integer, Long> {

        private TextView mTextView;
        private String mResponse;
        private String mDispText;

        public MyAsyncTask(TextView textView) {
            super();
            mTextView = textView; //ここに結果を表示する
            mResponse = "";
            mDispText = "";
        }

        // doInBackground開始前に呼ばれる（UI操作可能)
        @Override
        protected void onPreExecute() {
        }

        // バックグラウンド処理 (UI操作禁止)
        // return code: 0:正常,1:キャンセル,2:エラー
        @Override
        protected Long doInBackground(String... params) {
            Log.d("MyAsyncTask", "doInBackground "+params[0]);

            // finallyで後始末するものはここで宣言する
            HttpsURLConnection connection = null;
            Map<String, List<String>> headers = null;
            InputStream inputStream = null;
            BufferedReader reader = null;

            try {
                // 入力文字列を Bing API に渡してサジェスト候補検索
                String uri = "https://api.bing.com/qsonhs.aspx?mkt=ja-JP&q=" +
                        URLEncoder.encode(params[0], "UTF-8");
                Log.d("MyAsyncTask","URI="+uri);

                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 1");
                    return 1L;
                }

                URL url = new URL(uri);
                connection = (HttpsURLConnection)url.openConnection();

                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 2");
                    return 1L;
                }

                connection.setRequestProperty("User-Agent","Android " + Build.MODEL);
                connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                connection.setConnectTimeout(30000); // Timeout 30秒
                connection.setReadTimeout(30000); // Timeout 30秒
                connection.setDoInput(true);
                connection.setRequestMethod("GET");

                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 3");
                    return 1L;
                }

                connection.connect(); // 接続・検索

                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 4");
                    return 1L;
                }

                int responseCode = connection.getResponseCode(); // HTTPステータス取得

                Log.d("MyAsyncTask", "got response "+responseCode);

                if(responseCode != HttpsURLConnection.HTTP_OK) { // 200 OK以外はエラー
                    throw new IOException("HTTP responseCode: " + responseCode);
                }

                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 5");
                    return 1L;
                }

                //headers = connection.getHeaderFields();

                // サジェスト候補JSONデータストリームOpen gzip圧縮に対応
                String contentEncoding = connection.getContentEncoding();
                if(contentEncoding!=null && contentEncoding.contains("gzip")){
                    inputStream = new GZIPInputStream(connection.getInputStream());
                }else{
                    inputStream = connection.getInputStream();
                }

                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 6");
                    return 1L;
                }

                // データ読み込み
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?  StandardCharsets.UTF_8 : Charset.forName("UTF-8")));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isCancelled()) {
                        Log.d("MyAsyncTask", "cancel return 7");
                        return 1L;
                    }

                    sb.append(line);
                }

                mResponse = sb.toString(); // 結果JSONの生データ
                if(mResponse == null || mResponse.length() <= 0){
                    return 3L;
                }

                Log.d("MyAsyncTask", "finished stream read");

                // JSON解析開始
                JSONObject rootObject = new JSONObject(mResponse);
                JSONObject as = null;
                JSONArray results = null;
                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 8");
                    return 1L;
                }
                if(rootObject != null){
                    as = rootObject.optJSONObject("AS");
                    if(as != null){
                        results = as.optJSONArray("Results");
                    }
                }
                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 9");
                    return 1L;
                }
                if (results != null) { // Suggests要素配列を分解して出力
                    expandJSONArray(results.optJSONObject(0).optJSONArray("Suggests"));
                    if (isCancelled()) {
                        Log.d("MyAsyncTask", "cancel return 10");
                        return 1L;
                    }
                    Log.d("MyAsyncTask", "finished expand JSON");
                }

            } catch (Exception e) {
                Log.e("AsyncTask", e.toString());
                return 2L; // エラー終了
            }
            finally { // 後片付け
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("AsyncTask", e.toString());
                    }
                }
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e("AsyncTask", e.toString());
                    }
                }
                if(connection != null) {
                    // A connection to https://api.bing.com/ was leaked. Did you forget to close a response body?
                    // と言われるのを防ぐクローズ処理
                    if (connection.getErrorStream() != null) {
                        try {
                            connection.getErrorStream().close();
                        } catch (IOException e) {
                            Log.e("AsyncTask", e.toString());
                        }
                    }
                    connection.disconnect();
                }
            }
            return 0L; // 正常終了（検索候補無しも含む）
        }

        // doInBackgroundでpublishProgressを呼ぶと呼ばれる
        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        // doInBackground完了後に呼ばれる（UI操作可能)
        @Override
        protected void onPostExecute(Long result) {
            Log.d("MyAsyncTask", "onPostExecute result="+result);
            if(result == 0L){ //正常終了なら、サジェスト結果文字列を表示する
                mTextView.setText(mDispText);
                Log.d("MyAsyncTask", "finished display text");
            }
        }

        // doInBackground中にcancelされたら呼ばれる（UI操作可能)
        @Override
        protected void onCancelled() {
            Log.d("MyAsyncTask", "onCancelled");
        }

        // BingサジェストAPIの結果配列を文字列に分解する
        // https://api.bing.com/qsonhs.aspx?mkt=ja-JP&q=Amazon
        void expandJSONArray(JSONArray array){
            if(array == null){
                return;
            }
            int i, n = array.length();
            for (i = 0; i < n; i++) { //Txt要素が候補文字列なのでDispTextへ追加していく
                if (isCancelled()) {
                    Log.d("MyAsyncTask", "cancel return 11");
                    return;
                }
                JSONObject object = array.optJSONObject(i);
                if(object == null){
                    break;
                }
                mDispText += object.optString("Txt") + "\n";
            }
        }
    }
}
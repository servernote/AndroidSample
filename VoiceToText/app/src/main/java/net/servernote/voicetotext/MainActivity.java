package net.servernote.voicetotext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, RecognitionListener {

    private static final int PERMISSION_RECORD_AUDIO = 1;

    private Button mButton;
    private TextView mText;
    private SpeechRecognizer mRecorder;
    private AlertDialog.Builder mAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button)findViewById(R.id.speech_button);
        mText = (TextView)findViewById(R.id.output_text);

        mButton.setOnClickListener(this);

        mRecorder = null;
        
        mAlert = new AlertDialog.Builder(this);
        mAlert.setTitle(getString(R.string.error));
        mAlert.setPositiveButton(getString(R.string.ok), null);

        checkRecordable();
    }

    public Boolean checkRecordable(){
        if(!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
            //mAlert.setMessage(getString(R.string.speech_not_available));
            //mAlert.show();
            mText.setText(getString(R.string.speech_not_available));
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED)
            {
                mText.setText(getString(R.string.speech_not_granted));
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        PERMISSION_RECORD_AUDIO);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permission, int[] grantResults
    ){
        Log.d("MainActivity","onRequestPermissionsResult");

        if (grantResults.length <= 0) { return; }
        switch(requestCode){
            case PERMISSION_RECORD_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mText.setText("");
                } else {

                }
                break;
        }
    }

    public void stopRecording(){
        if(mRecorder != null && checkRecordable()) {
            mRecorder.stopListening();
            mRecorder.cancel();
            mRecorder.destroy();
            mRecorder = null;
            mButton.setText(getString(R.string.start_speech));
        }
    }

    public void startRecording(){
        if(mRecorder == null && checkRecordable()) {
            mText.setText(getString(R.string.prepare_speech));
            mRecorder = SpeechRecognizer.createSpeechRecognizer(this);
            mRecorder.setRecognitionListener(this);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    getPackageName());
            //以下指定で途中の認識を拾う
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            mRecorder.startListening(intent);
            mButton.setText(getString(R.string.stop_speech));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() != R.id.speech_button){
            return;
        }
        if(mRecorder != null){
            stopRecording();
        }
        else{
            startRecording();
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d("MainActivity","onReadyForSpeech");
        mText.setText(getString(R.string.ready_speech));
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("MainActivity","onBeginningOfSpeech");
        mText.setText("");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("MainActivity","onBufferReceived");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.d("MainActivity","onRmsChanged");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("MainActivity","onEndOfSpeech");
        stopRecording();
    }

    @Override
    public void onError(int error) {
        Log.d("MainActivity","onError.error="+error);
        //mAlert.setMessage(getString(R.string.speech_error) + "\nエラーコード：" + error);
        //mAlert.show();
        mText.setText(getString(R.string.speech_error) + "\nエラーコード：" + error);
        stopRecording();
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d("MainActivity","onEvent.eventType="+eventType);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d("MainActivity","onPartialResults");
        String str = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        if(str.length() > 0) {
            mText.setText(str);
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.d("MainActivity","onResults");
    }
}

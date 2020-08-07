package org.dicio.dicio_android.input;

import android.app.Activity;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.kaldi.Assets;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechRecognizer;
import org.kaldi.Vosk;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

import static android.Manifest.permission.RECORD_AUDIO;

public class VoskInputDevice extends SpeechInputDevice {

    static {
        System.loadLibrary("kaldi_jni");
    }

    private final Activity activity;
    private Disposable buildDisposable = null;
    private SpeechRecognizer recognizer = null;

    public VoskInputDevice(final Activity activity) {
        this.activity = activity;

        // TODO fix request code
        ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO}, 5);
    }

    @Override
    public void startListening() {
        if (recognizer == null) {
            if (buildDisposable != null) {
                return;
            }

            buildDisposable = Completable.fromAction(() -> {
                final Assets assets = new Assets(activity);
                final File assetDir = assets.syncAssets();
                Log.d("KaldiDemo", "Sync files in the folder " + assetDir.toString());

                Vosk.SetLogLevel(0);
                final Model model = new Model(assetDir.toString() + "/model-android");
                recognizer = new SpeechRecognizer(model);
                startListening();
            }).subscribe();
            return;
        }

        recognizer.addListener(new RecognitionListener() {

            private boolean receivedSomething = false;

            @Override
            public void onPartialResult(final String s) {}

            @Override
            public void onResult(final String s) {
                if (receivedSomething) {
                    return;
                }
                receivedSomething = true;
                recognizer.stop();
                onFinishedListening();

                try {
                    final String input = new JSONObject(s).getString("text");

                    if (!input.isEmpty()) {
                        notifyInputReceived(input);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(final Exception e) {
                recognizer.stop();
                onFinishedListening();
                notifyError(e);
            }

            @Override
            public void onTimeout() {
                recognizer.stop();
                onFinishedListening();
            }
        });
        recognizer.startListening();
    }
}

package org.dicio.dicio_android.input;

import android.app.Activity;

import androidx.core.app.ActivityCompat;

import org.dicio.dicio_android.ApiKeys;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Future;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class AzureSpeechInputDevice extends SpeechInputDevice {
    private static final String azureApiKey = ApiKeys.azure;
    private static final String azureServiceRegion = "westus";

    private Disposable speechRecognitionDisposable;

    public AzureSpeechInputDevice(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{RECORD_AUDIO, INTERNET}, 5);
    }

    @Override
    public void startListening() {
        if (speechRecognitionDisposable != null) speechRecognitionDisposable.dispose();

        speechRecognitionDisposable = Completable.fromCallable(() -> {
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureApiKey, azureServiceRegion);
            SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig);
            Future<SpeechRecognitionResult> task = speechRecognizer.recognizeOnceAsync();

            SpeechRecognitionResult speechRecognitionResult = task.get();
            switch (speechRecognitionResult.getReason()) {
                case RecognizedSpeech: case NoMatch:
                    notifyInputReceived(speechRecognitionResult.getText());
                    break;
                case Canceled:
                    throw new SocketException();
                default:
                    throw new IOException("Unable to recognize speech.");
            }

            return null;
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onFinishedListening,
                throwable -> {
                    onFinishedListening();
                    notifyError(throwable);
                });
    }
}

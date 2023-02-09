package org.stypox.dicio.input;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import org.stypox.dicio.R;

import java.util.ArrayList;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import static org.stypox.dicio.util.StringUtils.isNullOrEmpty;

public class AndroidSttServiceInputDevice extends SpeechInputDevice
        implements android.speech.RecognitionListener {

    public static final String TAG = AndroidSttServiceInputDevice.class.getSimpleName();
    private Activity activity;

    private boolean startListeningOnLoaded = false;

    private SpeechRecognizer speechRecognizer;
    private boolean currentlyListening = false;


    /////////////////////
    // Exposed methods //
    /////////////////////

    public AndroidSttServiceInputDevice(final Activity activity) {
        this.activity = activity;
    }

    @Override
    public void load() {
        load(false); // the user did not press on a button, so manual=false
    }

    /**
     * @param manual if this is true and the model is not already downloaded, do not start
     *               downloading it. See {@link #tryToGetInput(boolean)}.
     */
    protected void load(final boolean manual) {
        if (speechRecognizer == null) {
            onLoading();
            speechRecognizer = getRecognizer();
            speechRecognizer.setRecognitionListener(this);

            if (startListeningOnLoaded) {
                startListeningOnLoaded = false;
                tryToGetInput(manual);
            } else {
                onInactive();
            }
        }
    }

    /**
     * initializes the recognizers by calling the appropritate
     * {@link SpeechRecognizer}.createSpeechRecognizer() . Default is system provided recognizer.
     * Overwrite this in case you want to specify.
     * @return the {@link SpeechRecognizer}
     */
    protected SpeechRecognizer getRecognizer() {
        return SpeechRecognizer.createSpeechRecognizer(activity);
    }

    /**
     * Override this to specify which Intent shall be used in
     * {@link SpeechRecognizer}.startListening()
     * @return the {@link Intent} according to {@link RecognizerIntent}
     */
    protected Intent getRecognizerIntent() {
        final Intent i = new Intent();
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getString(activity.getString(R.string.pref_key_language), "en"));
        return i;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        cancelGettingInput();

        activity = null;
    }

    @Override
    public synchronized void tryToGetInput(final boolean manual) {
        if (speechRecognizer == null) {
            startListeningOnLoaded = true;
            load(manual); // not loaded before, retry
            return; // recognizer not ready
        }

        super.tryToGetInput(manual);

        Log.d(TAG, "starting recognizer");

        onLoading();
        speechRecognizer.startListening(getRecognizerIntent());
        currentlyListening = true;
    }

    @Override
    public void cancelGettingInput() {
        if (speechRecognizer != null && currentlyListening) {
            //call stoplistening only if it is running! Otherwise ERROR_CLIENT will be reported
            speechRecognizer.cancel();
        }
        startListeningOnLoaded = false;
    }

    /////////////////////
    // Other utilities //
    /////////////////////

    protected void asyncMakeToast(@StringRes final int message) {
        activity.runOnUiThread(() ->
                Toast.makeText(activity, activity.getString(message), Toast.LENGTH_SHORT).show());
    }


    ///////////////////////////
    // Recognition Callbacks //
    ///////////////////////////

    @Override
    public void onReadyForSpeech(final Bundle bundle) {
        Log.d(TAG, "onReadyForSpeech");
        onListening();
        currentlyListening = true;
    }

    @Override
    public void onBeginningOfSpeech() {
        //no usecase for dicio
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(final float v) {
        //no usecase for dicio
        Log.d(TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(final byte[] bytes) {
        //no usecase for dicio
        Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        currentlyListening = false;
        onInactive();
    }

    @Override
    public void onError(final int i) {
        Log.d(TAG, "onError called with error code = " + i);
        switch (i) {
            case SpeechRecognizer.ERROR_AUDIO:
                notifyError(new Throwable("ERROR_AUDIO"));
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                notifyError(new Throwable("ERROR_CLIENT"));
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                notifyError(new Throwable("ERROR_INSUFFICIENT_PERMISSIONS"));
                break;
            case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
                notifyError(new Throwable("ERROR_LANGUAGE_NOT_SUPPORTED"));
                break;
            case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:
                notifyError(new Throwable("ERROR_LANGUAGE_UNAVAILABLE"));
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                notifyError(new Throwable("ERROR_NETWORK"));
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                notifyError(new Throwable("ERROR_NETWORK_TIMEOUT"));
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                Log.d(TAG, "ERROR_NO_MATCH");
                notifyNoInputReceived();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                notifyError(new Throwable("ERROR_RECOGNIZER_BUSY"));
                break;
            case SpeechRecognizer.ERROR_SERVER:
                notifyError(new Throwable("ERROR_SERVER"));
                break;
            case SpeechRecognizer.ERROR_SERVER_DISCONNECTED:
                notifyError(new Throwable("ERROR_SERVER_DISCONNECTED"));
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                notifyError(new Throwable("ERROR_SPEECH_TIMEOUT"));
                break;
            case SpeechRecognizer.ERROR_TOO_MANY_REQUESTS:
                notifyError(new Throwable("ERROR_TOO_MANY_REQUESTS"));
                break;
            default:
                Log.w(TAG, "onError called with unexpected error code = " + i);
                notifyError(new Throwable("Unexpected error code = " + i));
        }
        //reset views
        onEndOfSpeech(); // e.g. Google does not send this after error like No_Match


    }

    @Override
    public void onResults(final Bundle bundle) {
        final ArrayList<String> results = bundle.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG, "onResult called with s = " + results.toString());
        notifyInputReceived(results);
    }

    @Override
    public void onPartialResults(final Bundle bundle) {
        final ArrayList<String> results = bundle.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG, "onPartialResult called with s = " + results.toString());
        final String partialInput = results.get(0);
        if (!isNullOrEmpty(partialInput)) {
            notifyPartialInputReceived(partialInput);
        }
    }

    @Override
    public void onEvent(final int i, final Bundle bundle) {
        //android docs: "Reserved for adding future events"
        Log.d(TAG, "onEvent");
    }



}

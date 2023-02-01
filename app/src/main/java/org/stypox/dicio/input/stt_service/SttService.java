package org.stypox.dicio.input.stt_service;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stypox.dicio.BuildConfig;
import org.stypox.dicio.R;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.Nullable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static org.stypox.dicio.util.StringUtils.isNullOrEmpty;

public class SttService extends RecognitionService {
    protected class RecognitionListener implements org.vosk.android.RecognitionListener {
        private boolean firstPartialResultReceived = false;

        @Override
        public void onPartialResult(final String s) {
            Log.d(TAG, "onPartialResult called with s = " + s);

            String partialInput = null;
            try {
                partialInput = new JSONObject(s).getString("partial");
            } catch (final JSONException e) {
                e.printStackTrace();
            }

            if (!isNullOrEmpty(partialInput)) {
                if (!firstPartialResultReceived) {
                    firstPartialResultReceived = true;
                    try {
                        callback.beginningOfSpeech();
                    } catch (final RemoteException e) {
                        logRemoteException(e);
                    }
                }
                final String[] partialInputArray = {partialInput};
                final Bundle partResult = new Bundle();
                partResult.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION,
                        new ArrayList<>(Arrays.asList(partialInputArray)));
                try {
                    callback.partialResults(partResult);
                } catch (final RemoteException e) {
                    logRemoteException(e);
                }
            }
        }

        @Override
        public void onResult(final String s) {
            Log.d(TAG, "onResult called with s = " + s);

            stopRecognizer();

            final ArrayList<String> inputs = new ArrayList<>();
            float[] confidences = null;
            try {
                final JSONObject jsonResult = new JSONObject(s);
                final JSONArray alternatives = jsonResult.getJSONArray("alternatives");
                int size = alternatives.length();
                for (int i = 0; i < size; i++) {
                    final String text = alternatives.getJSONObject(i).getString("text");
                    if (!isNullOrEmpty(text)) {
                        inputs.add(text);
                    }
                }
                //final size may change if empty entries exist
                size = inputs.size();
                confidences = new float[size];
                for (int i = 0; i < size; i++) {
                    confidences[i] = (float) alternatives.getJSONObject(i)
                            .getDouble("confidence");
                }

            } catch (final JSONException e) {
                e.printStackTrace();
            }

            if (inputs.isEmpty()) {
                try {
                    callback.error(SpeechRecognizer.ERROR_NO_MATCH);
                } catch (final RemoteException e) {
                    logRemoteException(e);
                }
            } else {
                final Bundle results = new Bundle();
                results.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, inputs);
                results.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, confidences);
                try {
                    callback.results(results);
                } catch (final RemoteException e) {
                    logRemoteException(e);
                }
            }
        }

        @Override
        public void onFinalResult(final String s) {
            Log.d(TAG, "onFinalResult called with s = " + s);
            firstPartialResultReceived = false; //reset for next input
            try {
                //only notify endOfSpeech because s is currently always empty - even if onResult
                // was not empty before
                callback.endOfSpeech();
            } catch (final RemoteException e) {
                logRemoteException(e);
            }
        }

        @Override
        public void onError(final Exception e) {
            Log.e(TAG, "onError", e);
            stopRecognizer();
            try {
                //The Error message is quite general because there is no "generic error code"
                callback.error(SpeechRecognizer.ERROR_SERVER);
            } catch (final RemoteException ex) {
                Log.e(TAG, "onError", e);
            }
        }

        @Override
        public void onTimeout() {
            Log.d(TAG, "onTimeout called");
            stopRecognizer();
            try {
                callback.error(SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
            } catch (final RemoteException e) {
                logRemoteException(e);
            }
        }
    }

    /**
     docs of SpeechService
     <a href="https://github.com/alphacep/vosk-api/blob/master/android/lib/src/main/java/org/vosk/
     android/SpeechService.java">...</a>
     */
    @Nullable
    private SpeechService speechService = null;
    private boolean currentlyInitializingRecognizer = false;
    public static final String MODEL_PATH = "/vosk-model";
    public static final String TAG = SttService.class.getSimpleName();
    private final CompositeDisposable disposables = new CompositeDisposable();
    public static final float SAMPLE_RATE = 44100.0f;
    private boolean currentlyListening = false;
    private boolean startListeningOnLoaded = false;
    private boolean onStartCommandCalled = false;
    private Intent lastRequestedIntent = null;
    Callback callback;

//TODO support onCheckRecognitionSupport
//TODO support onTriggerModelDownload

    @Override
    public void onCreate() {
        super.onCreate();
        load();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(TAG, "onStartCommand");
        onStartCommandCalled = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }


    @Override
    public void onRebind(final Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        disposables.clear();
        if (speechService != null) {
            stopRecognizer();
            speechService.shutdown();
            speechService = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStartListening(final Intent intent, final Callback newCallback) {
        Log.d(TAG, "onStartListening");
        Log.d(TAG, "onStartCommand called is " + onStartCommandCalled);
        //TODO remove toast or make different type of speech recognition hint or a preference option
        // to disable
        Toast.makeText(this, this.getString(R.string.pref_input_method_vosk),
                Toast.LENGTH_SHORT).show();
        //TODO maybe check here for audio permission of the caller (but already in manifest of this
        // service declared => should not happen?): Need a test app without permission
        // https://developer.android.com/reference/android/speech/RecognitionService#
        // onStartListening(android.content.Intent,%20android.speech.RecognitionService.Callback)
        this.callback = newCallback;
        lastRequestedIntent = intent;
        tryToGetInput();

        //TODO support Intent Extras if possible with vosk
        // EXTRA_LANGUAGE / EXTRA_LANGUAGE_PREFERENCE / EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE
        // Further Extras which may be interesting
        // EXTRA_LANGUAGE_MODEL / LANGUAGE_MODEL_FREE_FORM /   LANGUAGE_MODEL_WEB_SEARCH
        // EXTRA_SEGMENTED_SESSION
        // EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS /
        // EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS
        // EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS
        // EXTRA_AUDIO_SOURCE / EXTRA_AUDIO_SOURCE_CHANNEL_COUNT /
        // EXTRA_AUDIO_SOURCE_ENCODING / EXTRA_AUDIO_SOURCE_SAMPLING_RATE
        // EXTRA_BIASING_STRINGS
        // EXTRA_ENABLE_BIASING_DEVICE_CONTEXT

    }

    @Override
    protected void onCancel(final Callback newCallback) {
        Log.d(TAG, "onCancel");
        stopRecognizer();
    }

    @Override
    protected void onStopListening(final Callback newCallback) {
        Log.d(TAG, "onStopListening");
        if (currentlyListening) {
            stopRecognizer();
        }
    }





    private void load() {
        if (speechService == null && !currentlyInitializingRecognizer) {
            if (new File(getModelDirectory(), "ivector").exists()) {
                // one directory is in the correct place, so everything should be ok
                Log.d(TAG, "Vosk model in place");

                currentlyInitializingRecognizer = true;

                disposables.add(Completable.fromAction(this::initializeRecognizer)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            currentlyInitializingRecognizer = false;
                            if (startListeningOnLoaded) {
                                startListeningOnLoaded = false;
                                tryToGetInput();
                            }
                        }, throwable -> {
                            currentlyInitializingRecognizer = false;
                            if ("Failed to initialize recorder. Microphone might be already in use."
                                    .equals(throwable.getMessage())) {
                                callback.error(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS);
                            } else {
                                Log.e(TAG, "load()->initializeRecognizer", throwable);
                                callback.error(SpeechRecognizer.ERROR_SERVER);
                            }
                        }));

            } else {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        callback.error(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE);
                    } else {
                        callback.error(SpeechRecognizer.ERROR_SERVER);
                    }
                } catch (final RemoteException e) {
                    logRemoteException(e);
                }
            }
        }
    }
    public synchronized void tryToGetInput() {
        if (currentlyInitializingRecognizer) {
            startListeningOnLoaded = true;
            return;
        } else if (speechService == null) {
            try {
                callback.error(SpeechRecognizer.ERROR_SERVER);
            } catch (final RemoteException e) {
                logRemoteException(e);
            }
            return; // recognizer not ready
        }
        //(only one client can be connected via system to speech recognizer (otherwise
        // ERROR_BUSY seems to be reported) - check whether currently listening checks are
        // necessary at all) - on the other hand they do not harm
        if (currentlyListening) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    callback.error(SpeechRecognizer.ERROR_TOO_MANY_REQUESTS);
                } else {
                    //more generic
                    callback.error(SpeechRecognizer.ERROR_SERVER);
                }
            } catch (final RemoteException e) {
                logRemoteException(e);
            }
            return;
        }

        currentlyListening = true;
        Log.d(TAG, "starting recognizer");

        speechService.startListening(new RecognitionListener());

        try {
            callback.readyForSpeech(null);
        } catch (final RemoteException e) {
            logRemoteException(e);
        }
    }

    private void logRemoteException(final RemoteException e) {
        Log.e(TAG, "Remote exception on callback information", e);
    }

    private File getModelDirectory() {
        return new File(this.getFilesDir(), MODEL_PATH);
    }


    ////////////////////
    // Vosk Initialization //
    ////////////////////

    private synchronized void initializeRecognizer() throws IOException {
        Log.d(TAG, "initializing recognizer");

        LibVosk.setLogLevel(BuildConfig.DEBUG ? LogLevel.DEBUG : LogLevel.WARNINGS);
        final Model model = new Model(getModelDirectory().getAbsolutePath());
        final Recognizer recognizer = new Recognizer(model, SAMPLE_RATE);
        recognizer.setMaxAlternatives(
                lastRequestedIntent.getIntExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5));
        this.speechService = new SpeechService(recognizer, SAMPLE_RATE);
    }

    /**
     * save to call if
     */
    private void stopRecognizer() {
        if (speechService != null) {
            speechService.stop(); //does nothing if recognition is not active.
        } else if (currentlyListening) {
            //(actually currentlyListening should never be true at this point-however does not harm)
            //means SpeechRecognizer.startListening was called, but endOfSpeech not yet
            // make sure to free resources so that speech recognizer is not supposed to be busy
            try {
                callback.endOfSpeech();
            } catch (final RemoteException e) {
                logRemoteException(e);
            }
        }
        currentlyListening = false;


    }
}

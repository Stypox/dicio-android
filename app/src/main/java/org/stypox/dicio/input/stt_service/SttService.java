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
import org.stypox.dicio.error.ErrorInfo;
import org.stypox.dicio.error.ErrorUtils;
import org.stypox.dicio.error.UserAction;
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
                callbackErrorReport(SpeechRecognizer.ERROR_NO_MATCH);
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
            showErrorNotification(e);
            stopRecognizer();
            callbackErrorReport(SpeechRecognizer.ERROR_SERVER);
        }

        @Override
        public void onTimeout() {
            Log.d(TAG, "onTimeout called");
            stopRecognizer();
            callbackErrorReport(SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
        }
    }

    /**
     docs of SpeechService
     <a href="https://github.com/alphacep/vosk-api/blob/master/android/lib/src/main/java/org/vosk/
     android/SpeechService.java">...</a>
     */
    @Nullable
    private SpeechService speechService = null;
    private Model model;
    private long modelDownloadDate;
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

    @Override
    public void onCreate() {
        super.onCreate();
        LibVosk.setLogLevel(BuildConfig.DEBUG ? LogLevel.DEBUG : LogLevel.WARNINGS);
        initialize();
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
        shutdownSpeechService();
        super.onDestroy();
    }

    @Override
    protected void onStartListening(final Intent intent, final Callback newCallback) {
        Log.d(TAG, "onStartListening");
        Log.d(TAG, "onStartCommand called is " + onStartCommandCalled);
        this.callback = newCallback;
        //TODO check permission. Actually it seems this is already done by the system interface
        // (reports SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) , but it is
        // explicitly recommended in the SpeechRecognizer documentation. However the way it is in
        // the docs does not work here due to API Level for requested calls (and since Audio
        // Recorder is not directly implemented here but by vosk library)
        // https://developer.android.com/reference/android/speech/RecognitionService
        // However even if there is a way for app without permission, not a security issue since
        // stt service notifies user when speech input is started
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final String callingPackageName = getPackageManager().getPackagesForUid(
                    newCallback.getCallingUid())[0];
//Not working this way - check fails even for dicio
//            int permissionState = PermissionChecker.checkCallingPermission(this,
//                    "android.permission.RECORD_AUDIO", callingPackageName);
//            if (permissionState != PermissionChecker.PERMISSION_GRANTED){
//                callbackErrorReport(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS);
//                return;
//            }
        }
        if (speechService != null && !recogIntentExtrasEquals(lastRequestedIntent, intent)) {
            shutdownSpeechService();
            if (intent.hasExtra(RecognizerIntent.EXTRA_LANGUAGE)) {
                //check if language change is the reason
                Log.d(TAG, "requested language = "
                        + intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE));
                if (!lastRequestedIntent.hasExtra(RecognizerIntent.EXTRA_LANGUAGE)
                        || !lastRequestedIntent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)
                        .equals(intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE))) {
                    //Since at the moment only one language at the time is supported, just check
                    // whether the downloaded model has changed. Otherwise use the language which
                    // is installed anyway
                    if (getModelDirectory().lastModified() != modelDownloadDate) {
                        Log.d(TAG, "model last modified " + getModelDirectory().lastModified());
                        Log.d(TAG, "model_download_date " + modelDownloadDate);
                        model = null; //forces reloading
                        shutdownSpeechService(); //forces reloading
                    }
                }
            }
        }
        lastRequestedIntent = intent;

        //TODO remove toast or make different type of speech recognition hint or a preference option
        // to disable
        Toast.makeText(this, this.getString(R.string.pref_input_method_vosk),
                Toast.LENGTH_SHORT).show();
        tryToGetInput();

    }

    /**
     * in order to identify whether a new recognizer has to be loaded or not
     * @return true if all Extras, which are supported by this STT service, are equal
     */
    protected boolean recogIntentExtrasEquals(final Intent i1, final Intent i2) {
        final Bundle ie1 = i1.getExtras();
        final Bundle ie2 = i2.getExtras();
        final String[] supportedExtras = {RecognizerIntent.EXTRA_LANGUAGE,
                RecognizerIntent.EXTRA_MAX_RESULTS};
        for (final String key: supportedExtras) {
            final Object extra1 = ie1.get(key);
            final Object extra2 = ie2.get(key);
            //return false if they are not equal or one (but noth both) is null
            if (extra1 != null) {
                if (!extra1.equals(extra2)) {
                    return false;
                }
            } else if (extra2 != null) {
                return false;
            }
        }
        return true;

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





    private void initialize() {
        if (speechService == null && !currentlyInitializingRecognizer) {
            if (new File(getModelDirectory(), "ivector").exists()) {
                // one directory is in the correct place, so everything should be ok
                Log.d(TAG, "Vosk model in place");

                currentlyInitializingRecognizer = true;

                disposables.add(Completable.fromAction(this::loadModel)
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
                            showErrorNotification(throwable);
                        }));

            } else {
                if (callback != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        callbackErrorReport(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE);
                    } else {
                        callbackErrorReport(SpeechRecognizer.ERROR_SERVER);
                    }
                }
                showErrorNotification(
                        new Throwable(getString(R.string.vosk_model_unsupported_language)));
            }
        }
    }
    public synchronized void tryToGetInput() {
        if (currentlyInitializingRecognizer) {
            startListeningOnLoaded = true;
            return;
        } else if (model == null) {
            Log.w(TAG, "tryToGetInput model==null");
            initialize(); //try to load anew
            startListeningOnLoaded = true;
            return; // recognizer not ready
        } else if (getModelDirectory().lastModified() != modelDownloadDate) {
            //if model has changed / updated / etc...
            Log.i(TAG, "model directory modified date changed - load it anew");
            Log.d(TAG, "model last modified " + getModelDirectory().lastModified());
            Log.d(TAG, "model_download_date " + modelDownloadDate);
            model = null; //reset
            shutdownSpeechService();
            initialize(); //load new one
            startListeningOnLoaded = true;
            return; // recognizer not ready
        } else if (speechService == null) {
            try {
                loadSpeechService();
            } catch (final IOException e) {
                if ("Failed to initialize recorder. Microphone might be already in use."
                        .equals(e.getMessage())) {
                    callbackErrorReport(SpeechRecognizer.ERROR_AUDIO);
                } else {
                    Log.e(TAG, "load()->initializeRecognizer", e);
                    showErrorNotification(e);
                    callbackErrorReport(SpeechRecognizer.ERROR_SERVER);
                }
                return;
            }
        }
        //(only one client can be connected via system to speech recognizer (otherwise
        // ERROR_BUSY seems to be reported) - check whether currently listening checks are
        // necessary at all) - on the other hand they do not harm
        if (currentlyListening) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                callbackErrorReport(SpeechRecognizer.ERROR_TOO_MANY_REQUESTS);
            } else {
                //more generic
                callbackErrorReport(SpeechRecognizer.ERROR_SERVER);
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
        showErrorNotification(e);
    }

    /**
     * wrapper for
     * calling {@link RecognitionService.Callback#error(int)} and catches the remote exception
     * @param errorType see {@link RecognitionService.Callback#error(int)}
     */
    protected void callbackErrorReport(final int errorType) {
        try {
            callback.error(errorType);
        } catch (final RemoteException e) {
            logRemoteException(e);
        } catch (final NullPointerException e) {
            showErrorNotification(e);
        }
    }

    private File getModelDirectory() {
        return new File(this.getFilesDir(), MODEL_PATH);
    }

    protected void showErrorNotification(final Throwable t) {
        final ErrorInfo ei = new ErrorInfo(t, UserAction.STT_SERVICE_SPEECH_TO_TEXT);
        ErrorUtils.createNotification(this, ei);
    }


    ////////////////////
    // Vosk Initialization //
    ////////////////////

    /**
     * load the vosk model. Most time consuming procedure of recognizer intitializiation
     */
    private synchronized void loadModel() {
        Log.d(TAG, "load Model");
        final long t0 = System.currentTimeMillis();
        model = new Model(getModelDirectory().getAbsolutePath());
        modelDownloadDate = getModelDirectory().lastModified();
        final long t1 = (System.currentTimeMillis() - t0);
        Log.i(TAG, "Loading Model takes " + t1 + " ms");
    }

    /**
     * load the recognizer. call this if a intent with new parameters (compared to last one) is
     * received
     */
    private void loadSpeechService() throws IOException {
        if (speechService != null) {
            //first shutdown the old one, if a new one is requested
            shutdownSpeechService();
        }

        final long t0 = System.currentTimeMillis();
        final Recognizer recognizer = new Recognizer(model, SAMPLE_RATE);
        if (lastRequestedIntent != null) {
            recognizer.setMaxAlternatives(
                    lastRequestedIntent.getIntExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5));

        }
        this.speechService = new SpeechService(recognizer, SAMPLE_RATE);
        Log.i(TAG, "Loading SpeechService takes " + (System.currentTimeMillis() - t0)  + " ms");
    }

    /**
     * only shut down speech service
     * this still keeps the language model in cache for faster start of speech service
     */
    protected void shutdownSpeechService() {
        if (speechService != null) {
            stopRecognizer();
            speechService.shutdown();
            speechService = null;
        }
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

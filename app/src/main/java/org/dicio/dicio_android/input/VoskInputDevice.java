package org.dicio.dicio_android.input;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.kaldi.KaldiRecognizer;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechService;
import org.kaldi.Vosk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.Manifest.permission.RECORD_AUDIO;
import static org.dicio.dicio_android.util.LocaleUtils.LocaleResolutionResult;
import static org.dicio.dicio_android.util.LocaleUtils.UnsupportedLocaleException;
import static org.dicio.dicio_android.util.LocaleUtils.getAvailableLocalesFromPreferences;
import static org.dicio.dicio_android.util.LocaleUtils.resolveSupportedLocale;

public class VoskInputDevice extends SpeechInputDevice {

    public static final String TAG = VoskInputDevice.class.getSimpleName();
    public static final String MODEL_PATH = "/vosk-model";
    public static final String MODEL_ZIP_FILENAME = "model.zip";
    public static final float SAMPLE_RATE = 16000.0f;

    /**
     * All small models from <a href="https://alphacephei.com/vosk/models">Vosk</a>
     */
    public static final Map<String, String> MODEL_URLS = new HashMap<String, String>() {{
        put("en",    "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip");
        put("en-in", "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip");
        put("cn",    "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.3.zip");
        put("ru",    "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.4.zip");
        put("fr",    "https://alphacephei.com/vosk/models/vosk-model-small-fr-pguyot-0.3.zip");
        put("de",    "https://alphacephei.com/vosk/models/vosk-model-small-de-0.15.zip");
        put("es",    "https://alphacephei.com/vosk/models/vosk-model-small-es-0.3.zip");
        put("pt",    "https://alphacephei.com/vosk/models/vosk-model-small-pt-0.3.zip");
        put("tr",    "https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip");
        put("vn",    "https://alphacephei.com/vosk/models/vosk-model-small-vn-0.3.zip");
        put("it",    "https://alphacephei.com/vosk/models/vosk-model-small-it-0.4.zip");
        put("nl",    "https://alphacephei.com/vosk/models/vosk-model-nl-spraakherkenning-0.6-lgraph.zip");
        put("ca",    "https://alphacephei.com/vosk/models/vosk-model-small-ca-0.4.zip");
        put("fa",    "https://alphacephei.com/vosk/models/vosk-model-small-fa-0.4.zip");
        put("ph",    "https://alphacephei.com/vosk/models/vosk-model-tl-ph-generic-0.6.zip");
    }};


    private final Activity activity;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private SpeechService recognizer = null;
    private boolean currentlyInitializingRecognizer = false;
    private boolean currentlyListening = false;


    ////////////////////////
    // Overriding methods //
    ////////////////////////

    public VoskInputDevice(final Activity activity) {
        this.activity = activity;

        // TODO fix request code
        ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO}, 5);
    }

    @Override
    public synchronized void tryToGetInput() {

        if (recognizer == null) {
            if (!currentlyInitializingRecognizer) {
                currentlyInitializingRecognizer = true;
                disposables.add(Single.fromCallable(this::initializeRecognizer)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(recognizerIsReady -> {
                            currentlyInitializingRecognizer = false;
                            if (recognizerIsReady) {
                                tryToGetInput();
                            }
                        }, throwable -> {
                            currentlyInitializingRecognizer = false;
                            throwable.printStackTrace();
                        }));
            }
            return;
        }

        if (currentlyListening) {
            return;
        }
        currentlyListening = true;

        Log.d(TAG, "starting recognizer");
        recognizer.startListening();
        onStartedListening();
    }


    ////////////////////
    // Initialization //
    ////////////////////

    synchronized boolean initializeRecognizer() throws IOException {
        if (!prepareModel()) {
            return false;
        }
        Log.d(TAG, "initializing recognizer");

        Vosk.SetLogLevel(0);
        final Model model = new Model(getModelDirectory().getAbsolutePath());
        recognizer = new SpeechService(new KaldiRecognizer(model, SAMPLE_RATE), SAMPLE_RATE);

        recognizer.addListener(new RecognitionListener() {

            @Override
            public void onPartialResult(final String s) {
                Log.d(TAG, "onPartialResult called");
            }

            @Override
            public void onResult(final String s) {
                Log.d(TAG, "onResult called");
                if (!currentlyListening) {
                    return;
                }
                stopListening();

                String input = null;
                try {
                    input = new JSONObject(s).getString("text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (input != null && !input.isEmpty()) {
                    notifyInputReceived(input);
                }
            }

            @Override
            public void onError(final Exception e) {
                Log.d(TAG, "onError called");
                stopListening();
                notifyError(e);
            }

            @Override
            public void onTimeout() {
                Log.d(TAG, "onTimeout called");
                stopListening();
            }

            private void stopListening() {
                recognizer.stop();
                currentlyListening = false;
                onFinishedListening();
            }
        });
        return true;
    }


    ////////////////////
    // File utilities //
    ////////////////////

    private boolean prepareModel() {
        if (new File(getModelDirectory(), "README").exists()) {
            // one file is in the correct place, so everything should be ok
            return true;
        } else {
            final DownloadManager downloadManager =
                    (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

            if (getDownloadIdFromPreferences(activity, downloadManager) == null) {
                // download zip if not already downloading
                try {
                    final LocaleResolutionResult result = resolveSupportedLocale(
                            getAvailableLocalesFromPreferences(activity), MODEL_URLS.keySet());
                    startDownloadingModel(downloadManager, result.supportedLocaleString);
                } catch (UnsupportedLocaleException e) {
                    asyncMakeToast(R.string.vosk_model_unsupported_language);
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    private void startDownloadingModel(final DownloadManager downloadManager,
                                       final String language) {
        asyncMakeToast(R.string.vosk_model_downloading);
        final File modelZipFile = getModelZipFile();
        //noinspection ResultOfMethodCallIgnored
        modelZipFile.delete(); // if existing, delete the model zip file

        // build download manager request
        final String modelUrl = MODEL_URLS.get(language);
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelUrl))
                .setTitle(activity.getString(R.string.vosk_model_notification_title))
                .setDescription(activity.getString(
                        R.string.vosk_model_notification_description, language))
                .setDestinationUri(Uri.fromFile(modelZipFile));

        // launch download
        final long modelDownloadId = downloadManager.enqueue(request);
        putDownloadIdInPreferences(activity, modelDownloadId);

        // setup download completion listener
        final IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    if (id == modelDownloadId) {
                        disposables.add(Completable
                                .fromAction(VoskInputDevice.this::extractModelZip)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                            asyncMakeToast(R.string.vosk_model_ready);
                                            downloadManager.remove(modelDownloadId);
                                            putDownloadIdInPreferences(activity, null);
                                        },
                                        throwable -> {
                                            asyncMakeToast(R.string.vosk_model_extraction_error);
                                            throwable.printStackTrace();
                                            downloadManager.remove(modelDownloadId);
                                            putDownloadIdInPreferences(activity, null);
                                        }));
                    }
                }
            }
        };
        activity.registerReceiver(broadcastReceiver, filter);
    }

    private void extractModelZip() throws IOException {
        asyncMakeToast(R.string.vosk_model_extracting);

        try (final ZipInputStream zipInputStream =
                     new ZipInputStream(new FileInputStream(getModelZipFile()))) {
            ZipEntry entry; // cycles through all entries
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final File destinationFile = getDestinationFile(entry.getName());

                if (entry.isDirectory()) {
                    // create directory
                    if (!destinationFile.mkdirs()) {
                        throw new IOException("mkdirs failed: " + destinationFile);
                    }

                } else {
                    // copy file
                    try (final BufferedOutputStream outputStream = new BufferedOutputStream(
                            new FileOutputStream(destinationFile))) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        outputStream.flush();
                    }
                }

                zipInputStream.closeEntry();
            }
        }
    }


    ////////////////////
    // File utilities //
    ////////////////////

    private File getDestinationFile(final String entryName) throws IOException {
        // model files are under a subdirectory, so get the path after the first /
        final String filePath = entryName.substring(entryName.indexOf('/') + 1);
        final File destinationDirectory = getModelDirectory();

        // protect from Zip Slip vulnerability (!)
        final File destinationFile = new File(destinationDirectory, filePath);
        if (!destinationDirectory.getCanonicalPath().equals(destinationFile.getCanonicalPath()) &&
                !destinationFile.getCanonicalPath().startsWith(
                        destinationDirectory.getCanonicalPath() + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + entryName);
        }

        return destinationFile;
    }

    private File getModelDirectory() {
        return new File(activity.getFilesDir(), MODEL_PATH);
    }

    private File getModelZipFile() {
        return new File(activity.getExternalFilesDir(null), MODEL_ZIP_FILENAME);
    }


    //////////////////////////
    // Preference utilities //
    //////////////////////////

    private static Long getDownloadIdFromPreferences(final Context context,
                                                     final DownloadManager manager) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String downloadIdKey = context.getString(R.string.settings_key_vosk_download_id);
        if (prefs.contains(downloadIdKey)) {
            final long id = prefs.getLong(downloadIdKey, 0);
            if (manager.query(new DownloadManager.Query().setFilterById(id)).getCount() == 0) {
                // no download in progress or being extracted with this id, reset setting
                return null;
            } else {
                return id;
            }

        } else {
            return null;
        }
    }

    private void putDownloadIdInPreferences(final Context context, final Long id) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String downloadIdKey = context.getString(R.string.settings_key_vosk_download_id);
        if (id == null) {
            // remove completely, used to notify of null values, check getDownloadIdFromPreferences
            prefs.edit().remove(downloadIdKey).apply();
        } else {
            prefs.edit().putLong(downloadIdKey, id).apply();
        }
    }


    /////////////////////
    // Other utilities //
    /////////////////////

    private void asyncMakeToast(@StringRes final int message) {
        activity.runOnUiThread(() ->
                Toast.makeText(activity, activity.getString(message), Toast.LENGTH_SHORT).show());
    }


    ///////////////////////////////
    // Cleanup of models on disk //
    ///////////////////////////////

    public static void deleteCurrentModel(final Context context) {
        final DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final Long modelDownloadId = getDownloadIdFromPreferences(context, downloadManager);
        if (modelDownloadId != null) {
            downloadManager.remove(modelDownloadId);
        }

        deleteFolder(new File(context.getFilesDir(), MODEL_PATH));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteFolder(final File file) {
        final File[] subFiles = file.listFiles();
        if (subFiles != null) {
            for (final File subFile : subFiles) {
                if(subFile.isDirectory()) {
                    deleteFolder(subFile);
                } else {
                    subFile.delete();
                }
            }
        }
        file.delete();
    }
}

package org.stypox.dicio.input;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.speech.SpeechRecognizer;
import android.util.Log;

import org.stypox.dicio.R;
import org.stypox.dicio.Sections;
import org.stypox.dicio.input.stt_service.SttService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.Nullable;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static org.stypox.dicio.util.LocaleUtils.LocaleResolutionResult;
import static org.stypox.dicio.util.LocaleUtils.UnsupportedLocaleException;
import static org.stypox.dicio.util.LocaleUtils.resolveSupportedLocale;

public class VoskInputDevice extends AndroidSttServiceInputDevice {

    public static final String TAG = VoskInputDevice.class.getSimpleName();
    public static final String MODEL_PATH = "/vosk-model";
    public static final String MODEL_ZIP_FILENAME = "model.zip";

    /**
     * All small models from <a href="https://alphacephei.com/vosk/models">Vosk</a>
     */
    @SuppressWarnings("LineLength")
    public static final Map<String, String> MODEL_URLS = new HashMap<>() {{
        put("en",    "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip");
        put("en-in", "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip");
        put("cn",    "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip");
        put("ru",    "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip");
        put("fr",    "https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip");
        put("de",    "https://alphacephei.com/vosk/models/vosk-model-small-de-0.15.zip");
        put("es",    "https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip");
        put("pt",    "https://alphacephei.com/vosk/models/vosk-model-small-pt-0.3.zip");
        put("tr",    "https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip");
        put("vn",    "https://alphacephei.com/vosk/models/vosk-model-small-vn-0.3.zip");
        put("it",    "https://alphacephei.com/vosk/models/vosk-model-small-it-0.22.zip");
        put("nl",    "https://alphacephei.com/vosk/models/vosk-model-small-nl-0.22.zip");
        put("ca",    "https://alphacephei.com/vosk/models/vosk-model-small-ca-0.4.zip");
        put("fa",    "https://alphacephei.com/vosk/models/vosk-model-small-fa-0.4.zip");
        put("ph",    "https://alphacephei.com/vosk/models/vosk-model-tl-ph-generic-0.6.zip");
        put("uk",    "https://alphacephei.com/vosk/models/vosk-model-small-uk-v3-nano.zip");
        put("kz",    "https://alphacephei.com/vosk/models/vosk-model-small-kz-0.15.zip");
        put("ja",    "https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip");
        put("eo",    "https://alphacephei.com/vosk/models/vosk-model-small-eo-0.42.zip");
        put("hi",    "https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip");
        put("cs",    "https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip");
        put("pl",    "https://alphacephei.com/vosk/models/vosk-model-small-pl-0.22.zip");
        put("uz",    "https://alphacephei.com/vosk/models/vosk-model-small-uz-0.22.zip");
        put("ko",    "https://alphacephei.com/vosk/models/vosk-model-small-ko-0.22.zip");
    }};


    private Activity activity;
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable private BroadcastReceiver downloadingBroadcastReceiver = null;
    private Long currentModelDownloadId = null;

    /////////////////////
    // Exposed methods //
    /////////////////////

    public VoskInputDevice(final Activity activity) {
        super(activity);
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
        if (new File(getModelDirectory(), "ivector").exists()) {
            // one directory is in the correct place, so everything should be ok
            Log.d(TAG, "Vosk model in place");
            super.load(manual);
        } else {
            Log.d(TAG, "Vosk model not in place");
            final DownloadManager downloadManager =
                    (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

            if (currentModelDownloadId == null) {
                Log.d(TAG, "Vosk model is not already being downloaded");

                if (manual) {
                    // the model needs to be downloaded and no download has already started;
                    // the user manually triggered the input device, so he surely wants the
                    // model to be downloaded, so we can proceed
                    onLoading();
                    try {
                        final LocaleResolutionResult result = resolveSupportedLocale(
                                LocaleListCompat.create(Sections.getCurrentLocale()),
                                MODEL_URLS.keySet());
                        startDownloadingModel(downloadManager, result.supportedLocaleString);
                    } catch (final UnsupportedLocaleException e) {
                        asyncMakeToast(R.string.vosk_model_unsupported_language);
                        e.printStackTrace();
                        onRequiresDownload();
                    }

                } else {
                    // loading the model would require downloading it, but the user didn't
                    // explicitly tell the voice recognizer to download files, so notify them
                    // that a download is required
                    onRequiresDownload();
                }

            } else {
                Log.d(TAG, "Vosk model already being downloaded: " + currentModelDownloadId);
            }
        }
    }

    @Override
    protected SpeechRecognizer getRecognizer() {
        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(activity,
                new ComponentName(activity, SttService.class));
        //additionally call startService so that service is not directly destroyed after
        //speech recognizer is unbound (especially important if SttServiceActivity is
        // only called from other apps. If dicio app is closed, service is destroyed anyway,
        // too. Avoid destroyin in order to avoid re-initialization of SpeechService
        //(observed when manually closed - check if this happens too when closed by system
        // due to inactivity)
        //works also when battery optimization is enabled
        //TODO check long term behaviour with and without battery optimization
        //TODO check how to call startService if neither Dicio Main app nor
        // Dicios SttServiceActivity is called but directly
        // SpeechRecognizer.createSpeechRecognizer by a 3rd party app
        activity.startService(new Intent(activity, SttService.class));
        return sr;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        disposables.clear();

        if (currentModelDownloadId != null) {
            final DownloadManager downloadManager =
                    (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.remove(currentModelDownloadId);
            updateCurrentDownloadId(activity, null);
        }

        if (downloadingBroadcastReceiver != null) {
            activity.unregisterReceiver(downloadingBroadcastReceiver);
            downloadingBroadcastReceiver = null;
        }
        activity = null;
    }

    /**
     * Deletes the Vosk model downloaded in the {@link Context#getFilesDir()} if it exists. It also
     * stops any Vosk model download currently in progress based on the id stored in settings.
     * @param context the Android context used to get the download manager and the files dir
     */
    public static void deleteCurrentModel(final Context context) {
        final DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final Long modelDownloadId = getDownloadIdFromPreferences(context, downloadManager);
        if (modelDownloadId != null) {
            downloadManager.remove(modelDownloadId);
        }

        deleteFolder(new File(context.getFilesDir(), MODEL_PATH));
    }


    ////////////////////
    // Model download //
    ////////////////////

    private void startDownloadingModel(final DownloadManager downloadManager,
                                       final String language) {
        asyncMakeToast(R.string.vosk_model_downloading);
        final File modelZipFile = getModelZipFile();
        //noinspection ResultOfMethodCallIgnored
        modelZipFile.delete(); // if existing, delete the model zip file (should never happen)

        // build download manager request
        final String modelUrl = MODEL_URLS.get(language);
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelUrl))
                .setTitle(activity.getString(R.string.vosk_model_notification_title))
                .setDescription(activity.getString(
                        R.string.vosk_model_notification_description, language))
                .setDestinationUri(Uri.fromFile(modelZipFile));

        // setup download completion listener
        final IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadingBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                Log.d(TAG, "Got intent for downloading broadcast receiver: " + intent);
                if (downloadingBroadcastReceiver == null) {
                    return; // just to be sure there are no issues with threads
                }

                if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                    if (currentModelDownloadId == null || id != currentModelDownloadId) {
                        Log.w(TAG, "Download complete listener notified with unknown id: " + id);
                        return; // do not unregister broadcast receiver
                    }

                    if (downloadingBroadcastReceiver != null) {
                        Log.d(TAG, "Unregistering downloading broadcast receiver");
                        activity.unregisterReceiver(downloadingBroadcastReceiver);
                        downloadingBroadcastReceiver = null;
                    }

                    if (downloadManager.getMimeTypeForDownloadedFile(currentModelDownloadId)
                            == null) {
                        Log.e(TAG, "Failed to download vosk model");
                        asyncMakeToast(R.string.vosk_model_download_error);
                        downloadManager.remove(currentModelDownloadId);
                        updateCurrentDownloadId(activity, null);
                        onInactive();
                        return;
                    }

                    Log.d(TAG, "Vosk model download complete, extracting from zip");
                    disposables.add(Completable
                            .fromAction(VoskInputDevice.this::extractModelZip)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                        asyncMakeToast(R.string.vosk_model_ready);
                                        downloadManager.remove(currentModelDownloadId);
                                        updateCurrentDownloadId(activity, null);

                                        // surely the user pressed a button a while ago that
                                        // triggered the download process, so manual=true
                                        load(true);
                                    },
                                    throwable -> {
                                        asyncMakeToast(R.string.vosk_model_extraction_error);
                                        throwable.printStackTrace();
                                        downloadManager.remove(currentModelDownloadId);
                                        updateCurrentDownloadId(activity, null);
                                        onInactive();
                                    }));
                }
            }
        };
        activity.registerReceiver(downloadingBroadcastReceiver, filter);

        // launch download
        Log.d(TAG, "Starting vosk model download: " + request);
        updateCurrentDownloadId(activity, downloadManager.enqueue(request));
    }

    private void extractModelZip() throws IOException {
        asyncMakeToast(R.string.vosk_model_extracting);

        try (ZipInputStream zipInputStream =
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
                    try (BufferedOutputStream outputStream = new BufferedOutputStream(
                            new FileOutputStream(destinationFile))) {
                        final byte[] buffer = new byte[1024];
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
        if (!destinationDirectory.getCanonicalPath().equals(destinationFile.getCanonicalPath())
                && !destinationFile.getCanonicalPath().startsWith(
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteFolder(final File file) {
        final File[] subFiles = file.listFiles();
        if (subFiles != null) {
            for (final File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    deleteFolder(subFile);
                } else {
                    subFile.delete();
                }
            }
        }
        file.delete();
    }


    ///////////////////////////
    // Download id utilities //
    ///////////////////////////

    private static Long getDownloadIdFromPreferences(final Context context,
                                                     final DownloadManager manager) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String downloadIdKey = context.getString(R.string.pref_key_vosk_download_id);
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

    private void updateCurrentDownloadId(final Context context, final Long id) {
        // this field is used anywhere except in static contexts, where the preference is used
        currentModelDownloadId = id;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String downloadIdKey = context.getString(R.string.pref_key_vosk_download_id);
        if (id == null) {
            // remove completely, used to notify of null values, check getDownloadIdFromPreferences
            prefs.edit().remove(downloadIdKey).apply();
        } else {
            prefs.edit().putLong(downloadIdKey, id).apply();
        }
    }

}

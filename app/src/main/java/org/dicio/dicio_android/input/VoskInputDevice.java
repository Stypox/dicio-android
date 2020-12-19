package org.dicio.dicio_android.input;

import android.app.Activity;
import android.content.res.AssetManager;

import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.kaldi.KaldiRecognizer;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechService;
import org.kaldi.Vosk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;

import static android.Manifest.permission.RECORD_AUDIO;

public class VoskInputDevice extends SpeechInputDevice {

    public static final String MODEL_PATH = "/vosk-model";
    public static final String ASSETS_SUBPATH = "sync/model-android";
    public static final float SAMPLE_RATE = 16000.0f;

    private final Activity activity;
    private Disposable buildDisposable = null;
    private SpeechService recognizer = null;
    private boolean currentlyListening = false;

    public VoskInputDevice(final Activity activity) {
        this.activity = activity;

        // TODO fix request code
        ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO}, 5);
    }

    private static void copyFromSyncAssetsToModelFolder(final AssetManager assetManager,
                                                        final String assetsPath,
                                                        final File modelPath) throws IOException {
        final File outFile = new File(modelPath, assetsPath.substring(ASSETS_SUBPATH.length()));
        outFile.getParentFile().mkdirs();

        try (final InputStream in = assetManager.open(assetsPath);
             final OutputStream out = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
        }
    }

    private static void copyModelIfNeeded(final File modelPath,
                                          final AssetManager assetManager) throws IOException {
        if (!new File(modelPath, "README").exists()) { // check if one file in correct place
            final Queue<String> paths = new ArrayDeque<>(Collections.singletonList(ASSETS_SUBPATH));

            while (true) {
                final String path = paths.poll();
                if (path == null) {
                    break;
                }

                final String[] subPaths = assetManager.list(path);
                if (subPaths.length == 0) {
                    // this is a file, copy it
                    if (!path.endsWith(".md5")) { // ignore checksum files
                        copyFromSyncAssetsToModelFolder(assetManager, path, modelPath);
                    }
                } else {
                    // this is a directory, navigate further
                    for (final String subPath : subPaths) {
                        paths.add(path + "/" + subPath);
                    }
                }
            }
        }
    }

    synchronized void initializeRecognizer() throws IOException {
        final File modelPath = new File(activity.getFilesDir(), MODEL_PATH);
        copyModelIfNeeded(modelPath, activity.getAssets());

        Vosk.SetLogLevel(0);
        final Model model = new Model(modelPath.getAbsolutePath());
        recognizer = new SpeechService(new KaldiRecognizer(model, SAMPLE_RATE), SAMPLE_RATE);

        recognizer.addListener(new RecognitionListener() {

            @Override
            public void onPartialResult(final String s) {}

            @Override
            public void onResult(final String s) {
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
                stopListening();
                notifyError(e);
            }

            @Override
            public void onTimeout() {
                stopListening();
            }

            private void stopListening() {
                recognizer.stop();
                currentlyListening = false;
                onFinishedListening();
            }
        });
    }

    @Override
    public void startListening() {
        if (recognizer == null) {
            if (buildDisposable == null) {
                // not already being initialized
                buildDisposable = Completable.fromAction(this::initializeRecognizer)
                        .subscribe(this::startListening, Throwable::printStackTrace);
            }
            return;
        }

        if (currentlyListening) {
            return;
        }
        currentlyListening = true;
        recognizer.startListening();
    }
}

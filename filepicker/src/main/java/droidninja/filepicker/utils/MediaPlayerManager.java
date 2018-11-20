package droidninja.filepicker.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class MediaPlayerManager {

    private static MediaPlayerManager manager;

    public static MediaPlayerManager getInstance() {
        if (manager == null) {
            manager = new MediaPlayerManager();
        }
        return manager;
    }

    private MediaPlayer mediaPlayer;

    public void play(Context context, Uri uriSound) {
        play(context, uriSound, null, null);
    }

    public void play(Context context, Uri uriSound, MediaPlayer.OnCompletionListener completionListener) {
        play(context, uriSound, completionListener, null);
    }

    public void play(Context context, Uri uriSound, MediaPlayer.OnErrorListener errorListener) {
        play(context, uriSound, null, errorListener);
    }

    public void play(Context context, Uri uriSound, MediaPlayer.OnCompletionListener completionListener, MediaPlayer.OnErrorListener errorListener) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(context.getApplicationContext(), uriSound);
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setOnErrorListener(errorListener);
        mediaPlayer.start();

    }

    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public Boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

}

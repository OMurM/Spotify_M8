package com.example.spotify;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Base64;
import com.bumptech.glide.Glide;
import java.io.IOException;

public class VideoPlaybackActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlaybackActivity";
    private MediaPlayer mediaPlayer;
    private Button playPauseButton;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playback);

        ImageView imageView = findViewById(R.id.image_view);
        TextView songTitle = findViewById(R.id.song_title);
        TextView songAlbum = findViewById(R.id.song_album);
        TextView songArtist = findViewById(R.id.song_artist);
        playPauseButton = findViewById(R.id.play_pause_button);

        String title = getIntent().getStringExtra("title");
        String album = getIntent().getStringExtra("album");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("image_url");
        String uri = getIntent().getStringExtra("uri");

        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Album: " + album);
        Log.d(TAG, "Artist: " + artist);
        Log.d(TAG, "Image URL: " + imageUrl);
        Log.d(TAG, "URI: " + uri);

        if (title != null) {
            songTitle.setText(title);
        }
        if (album != null) {
            songAlbum.setText(album);
        }
        if (artist != null) {
            songArtist.setText(artist);
        }

        if (imageUrl != null) {
            try {
                byte[] imageBytes = Base64.decode(imageUrl, Base64.DEFAULT);
                Glide.with(this)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(imageView);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, "Invalid Base64 string");
            }
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                mediaPlayer.pause();
                playPauseButton.setText("Play");
            } else {
                mediaPlayer.start();
                playPauseButton.setText("Pause");
            }
            isPlaying = !isPlaying;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
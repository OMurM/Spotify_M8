package com.example.spotify;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Base64;
import com.bumptech.glide.Glide;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
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

        String uri = getIntent().getStringExtra("uri");
        Log.d(TAG, "URI: " + uri);

        if (uri != null) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(uri);

                String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                byte[] art = retriever.getEmbeddedPicture();
                retriever.release();

                if (title != null) {
                    songTitle.setText(title);
                }
                if (album != null) {
                    songAlbum.setText(album);
                }
                if (artist != null) {
                    songArtist.setText(artist);
                }

                if (art != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] byteArray = baos.toByteArray();
                    String imageUrl = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    byte[] imageBytes = Base64.decode(imageUrl, Base64.DEFAULT);
                    Glide.with(this)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(imageView);
                } else {
                    Glide.with(this)
                            .load(R.drawable.ic_launcher_foreground)
                            .into(imageView);
                }

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(uri);
                mediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }
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
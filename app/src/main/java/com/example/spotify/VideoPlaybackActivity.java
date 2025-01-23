package com.example.spotify;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Base64;
import com.bumptech.glide.Glide;

public class VideoPlaybackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playback);

        ImageView imageView = findViewById(R.id.image_view);
        TextView songTitle = findViewById(R.id.song_title);
        TextView songAlbum = findViewById(R.id.song_album);
        TextView songArtist = findViewById(R.id.song_artist);

        String title = getIntent().getStringExtra("title");
        String album = getIntent().getStringExtra("album");
        String artist = getIntent().getStringExtra("artist");
        String imageUrl = getIntent().getStringExtra("image_url");

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
            byte[] imageBytes = Base64.decode(imageUrl, Base64.DEFAULT);
            Glide.with(this)
                    .load(imageBytes)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        }
    }
}
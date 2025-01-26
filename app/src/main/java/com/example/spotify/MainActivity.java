package com.example.spotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import android.net.Uri;
import android.media.MediaMetadataRetriever;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 23;
    private ArrayList<HashMap<String, String>> videoList;
    private VideoAdapter videoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView videoListView = findViewById(R.id.video_list_view);

        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(this, videoList, uri -> {
            Intent intent = new Intent(this, VideoPlaybackActivity.class);
            intent.putExtra("uri", uri);
            startActivity(intent);
        });
        videoListView.setAdapter(videoAdapter);

        if (!checkStoragePermissions()) {
            requestForStoragePermissions();
        } else {
            loadMediaFiles();
        }

        videoListView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> selectedMedia = videoList.get(position);
            Log.d("MainActivity", "Selected Media: " + selectedMedia.toString());
            Log.d("MainActivity", "Image URL: " + selectedMedia.get("image_url"));
            Intent intent = new Intent(this, VideoPlaybackActivity.class);
            intent.putExtra("title", selectedMedia.get("name"));
            intent.putExtra("album", selectedMedia.get("album"));
            intent.putExtra("artist", selectedMedia.get("artist"));
            intent.putExtra("image_url", selectedMedia.get("image_url"));
            intent.putExtra("uri", selectedMedia.get("uri"));
            startActivity(intent);
        });
    }


    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestForStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (read && write) {
                    Toast.makeText(MainActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
                    loadMediaFiles();
                } else {
                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Manage External Storage Permissions Granted", Toast.LENGTH_SHORT).show();
                    loadMediaFiles();
                } else {
                    Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadMediaFiles() {
        videoList.clear();
        File downloadDir = new File(Environment.getExternalStorageDirectory(), "Download");

        if (downloadDir.exists() && downloadDir.isDirectory()) {
            File[] files = downloadDir.listFiles((dir, name) -> name.endsWith(".mp3"));
            if (files != null) {
                for (File file : files) {
                    String path = file.getAbsolutePath();
                    String name = file.getName();
                    String title = null;
                    String imageUrl = null;
                    String album = null;
                    String artist = null;
                    try {
                        title = getTitle(path);
                        imageUrl = getEmbeddedPicture(path);
                        album = getAlbum(path);
                        artist = getArtist(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.d("MainActivity", "Found video file: " + name + " at " + path);

                    HashMap<String, String> media = new HashMap<>();
                    media.put("name", title);
                    media.put("uri", path);
                    media.put("image_url", imageUrl);
                    media.put("album", album);
                    media.put("artist", artist);
                    videoList.add(media);
                }
                videoAdapter.notifyDataSetChanged();
            } else {
                Log.d("MainActivity", "No video files found in Download directory");
            }
        } else {
            Log.d("MainActivity", "Download directory does not exist");
        }

        if (videoList.isEmpty()) {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTitle(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        retriever.release();
        return title;
    }

    private String getEmbeddedPicture(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        if (art != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        return null;
    }

    private String getAlbum(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        retriever.release();
        return album;
    }

    private String getArtist(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        retriever.release();
        return artist;
    }
}
package com.example.spotify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Base64;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<HashMap<String, String>> videoList;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String uri);
    }

    public VideoAdapter(Context context, ArrayList<HashMap<String, String>> videoList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.videoList = videoList;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int position) {
        return videoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.video_list_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.video_image);
        TextView nameTextView = convertView.findViewById(R.id.video_name);
        TextView albumTextView = convertView.findViewById(R.id.video_album);
        TextView artistTextView = convertView.findViewById(R.id.video_artist);

        HashMap<String, String> video = videoList.get(position);
        String uri = video.get("uri");
        String name = video.get("name");
        String imageUrl = video.get("image_url");
        String album = video.get("album");
        String artist = video.get("artist");

        nameTextView.setText(name);
        albumTextView.setText(album);
        artistTextView.setText(artist);

        if (imageUrl != null) {
            byte[] imageBytes = Base64.decode(imageUrl, Base64.DEFAULT);
            Glide.with(context).load(imageBytes).into(imageView);
        } else {
            Glide.with(context).load("https://via.placeholder.com/250").into(imageView);
        }

        convertView.setOnClickListener(v -> onItemClickListener.onItemClick(uri));

        return convertView;
    }
}
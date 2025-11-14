package com.example.mp3player.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mp3player.R;
import com.example.mp3player.models.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private List<Track> tracks;
    private OnTrackClickListener listener;
    
    public interface OnTrackClickListener {
        void onTrackClick(Track track);
        void onMoreClick(Track track);
    }
    
    public TrackAdapter(OnTrackClickListener listener) {
        this.tracks = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.bind(track, listener);
    }
    
    @Override
    public int getItemCount() {
        return tracks.size();
    }
    
    public void updateTracks(List<Track> newTracks) {
        this.tracks.clear();
        if (newTracks != null) {
            this.tracks.addAll(newTracks);
        }
        notifyDataSetChanged();
    }
    
    static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArtwork;
        TextView tvTitle;
        TextView tvArtist;
        ImageButton btnMore;
        
        TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArtwork = itemView.findViewById(R.id.ivArtwork);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
        
        void bind(Track track, OnTrackClickListener listener) {
            tvTitle.setText(track.getTitle());
            tvArtist.setText(track.getArtist());
            
            // Load artwork with Glide
            Glide.with(itemView.getContext())
                    .load(track.getArtworkUrl())
                    .placeholder(R.color.surface)
                    .error(R.color.surface)
                    .into(ivArtwork);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackClick(track);
                }
            });
            
            btnMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMoreClick(track);
                }
            });
        }
    }
}

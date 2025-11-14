package com.example.mp3player.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Track implements Parcelable {
    private long id;
    private String title;
    private String artist;
    private String album;
    private String artworkUrl;
    private String previewUrl;
    private int duration;
    private boolean isInLibrary;

    public Track() {
    }

    public Track(long id, String title, String artist, String album, String artworkUrl, 
                 String previewUrl, int duration, boolean isInLibrary) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.artworkUrl = artworkUrl;
        this.previewUrl = previewUrl;
        this.duration = duration;
        this.isInLibrary = isInLibrary;
    }

    protected Track(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        artworkUrl = in.readString();
        previewUrl = in.readString();
        duration = in.readInt();
        isInLibrary = in.readByte() != 0;
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(artworkUrl);
        dest.writeString(previewUrl);
        dest.writeInt(duration);
        dest.writeByte((byte) (isInLibrary ? 1 : 0));
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isInLibrary() {
        return isInLibrary;
    }

    public void setInLibrary(boolean inLibrary) {
        isInLibrary = inLibrary;
    }
}

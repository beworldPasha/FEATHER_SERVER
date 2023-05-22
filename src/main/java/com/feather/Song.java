package com.feather;

import java.io.Serializable;

public class Song implements Serializable {
    public String name;
    public String artist;
    public String[] playlists;
    public String image;
    public int length;
    public int trackIndex; // (index - 1)
    public String url;
    public boolean liked;
}
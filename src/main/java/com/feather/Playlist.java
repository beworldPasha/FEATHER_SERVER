package com.feather;

import java.awt.*;
import java.io.Serializable;

public class Playlist implements Serializable {
    public PlaylistSong[] songs;
    public int length;
    public String image;
    public String name;
    public String[] artists;
}

package com.feather;

import java.awt.*;
import java.io.Serializable;

public class Artist implements Serializable {
    public String name;
    public Playlist[] albums;
    public Image image;
}
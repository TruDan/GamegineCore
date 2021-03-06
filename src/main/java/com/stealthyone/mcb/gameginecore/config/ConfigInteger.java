package com.stealthyone.mcb.gameginecore.config;

import com.stealthyone.mcb.gameginecore.Gamegine;

public class ConfigInteger {

    private String path;
    private int defValue;

    ConfigInteger(String path) {
        this(path, 0);
    }

    public ConfigInteger(String path, int defValue) {
        this.path = path;
        this.defValue = defValue;
    }

    public int get() {
        return Gamegine.getInstance().getConfig().getInt(path, defValue);
    }

}
package dev.jbaby.wicket.oat;

import java.io.Serializable;

public enum OatTheme implements Serializable {
    DARK("dark"),
    LIGHT(null),
    MIDNIGHT("midnight"),
    NORD("nord"),
    EVERFOREST("everforest"),
    TOKYO_NIGHT("tokyo-night"),
    ROSE_PINE_DAWN("rose-pine-dawn"),
    ROYAL("royal"),
    CLAY("clay"),
    CATPPUCCIN_MOCHA("catppuccin-mocha"),
    CATPPUCCIN_LATTE("catppuccin-latte"),
    MATERIAL("material"),
    DAISY("daisy"),
    ULTRAVIOLET("ultraviolet"),
    HALLOWEEN("halloween"),
    XMAS("xmas"),
    WIREFRAME("wireframe");

    private final String value;

    OatTheme(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

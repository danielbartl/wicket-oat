package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Page;

import java.io.Serializable;

public record MenuItem(
        String label,
        Class<? extends Page> pageClass
) implements Serializable {

    public static MenuItem of(String label, Class<? extends Page> pageClass) {
        return new MenuItem(label, pageClass);
    }
}
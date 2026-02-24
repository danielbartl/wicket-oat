package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

public class OatToastBehavior extends Behavior {

    public enum Variant {
        DEFAULT("default"),
        SUCCESS("success"),
        ERROR("error"),
        WARNING("warning"),
        INFO("info");

        private final String value;
        Variant(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    public static void toast(IPartialPageRequestHandler handler, String message) {
        toast(handler, message, Variant.DEFAULT);
    }

    public static void toast(IPartialPageRequestHandler handler, String message, Variant variant) {
        toast(handler, message, variant, null);
    }

    public static void toast(IPartialPageRequestHandler handler, String message, Variant variant, String title) {
        String script;
        if (title != null) {
            script = String.format("ot.toast('%s', {variant: '%s', title: '%s'})", 
                escapeJs(message), variant.getValue(), escapeJs(title));
        } else {
            script = String.format("ot.toast('%s', {variant: '%s'})", 
                escapeJs(message), variant.getValue());
        }
        handler.appendJavaScript(script);
    }

    private static String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("'", "'");
    }
}

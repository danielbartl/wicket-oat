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
        Variant v = variant != null ? variant : Variant.DEFAULT;
        String script;
        if (title != null) {
            script = String.format("ot.toast('%s', '%s', {variant: '%s'})", 
                escapeJs(message), escapeJs(title), v.getValue());
        } else {
            script = String.format("ot.toast('%s', null, {variant: '%s'})", 
                escapeJs(message), v.getValue());
        }
        handler.appendJavaScript(script);
    }

    private static String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("'", "\\'");
    }
}

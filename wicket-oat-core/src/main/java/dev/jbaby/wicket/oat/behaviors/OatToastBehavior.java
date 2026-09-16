package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

/**
 * Not a component-attaching behavior in the usual sense - a holder for the
 * static {@code toast(...)} helpers that trigger Oat's {@code ot.toast()} JS
 * notification via an Ajax response, plus the {@link Variant} enum they share
 * with other behaviors. See {@link dev.jbaby.wicket.oat.Oat} for the public
 * entry point.
 */
public class OatToastBehavior extends Behavior {

    public enum Variant {
        DEFAULT(null),
        SUCCESS("success"),
        WARNING("warning"),
        DANGER("danger");

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
        String titleArg = title != null ? "'" + escapeJs(title) + "'" : "null";
        // Oat's ot.toast() only recognizes variant: 'success'|'warning'|'danger'; omit the
        // option entirely for the default/unstyled case instead of sending an unknown value.
        String optionsArg = v.getValue() != null ? String.format("{variant: '%s'}", v.getValue()) : "{}";
        String script = String.format("ot.toast('%s', %s, %s)", escapeJs(message), titleArg, optionsArg);
        handler.appendJavaScript(script);
    }

    private static String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("'", "\\'");
    }
}

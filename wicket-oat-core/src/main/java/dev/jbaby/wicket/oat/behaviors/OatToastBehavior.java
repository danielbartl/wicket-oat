package dev.jbaby.wicket.oat.behaviors;

import com.github.openjson.JSONObject;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FeedbackMessage;

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

        /** The toast variant for a feedback message's level: errors are danger, info and debug the default. */
        public static Variant forFeedback(FeedbackMessage message) {
            if (message.isError()) return DANGER;
            if (message.isWarning()) return WARNING;
            if (message.isSuccess()) return SUCCESS;
            return DEFAULT;
        }
    }

    public static void toast(IPartialPageRequestHandler handler, String message) {
        toast(handler, message, Variant.DEFAULT);
    }

    public static void toast(IPartialPageRequestHandler handler, String message, Variant variant) {
        toast(handler, message, variant, null);
    }

    public static void toast(IPartialPageRequestHandler handler, String message, Variant variant, String title) {
        handler.appendJavaScript(script(message, variant, title));
    }

    /**
     * The JavaScript that shows a toast, for use outside an Ajax request (e.g. in an
     * {@code OnDomReadyHeaderItem}).
     */
    public static String script(String message, Variant variant, String title) {
        Variant v = variant != null ? variant : Variant.DEFAULT;
        // JSON string literals are valid JS string literals, and quote() escapes quotes,
        // backslashes, control characters and "</", so user-supplied text can't break out.
        String titleArg = title != null ? JSONObject.quote(title) : "null";
        // Oat's ot.toast() only recognizes variant: 'success'|'warning'|'danger'; omit the
        // option entirely for the default/unstyled case instead of sending an unknown value.
        String optionsArg = v.getValue() != null ? "{variant: " + JSONObject.quote(v.getValue()) + "}" : "{}";
        return "ot.toast(" + JSONObject.quote(message) + ", " + titleArg + ", " + optionsArg + ")";
    }
}

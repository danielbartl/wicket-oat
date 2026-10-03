package dev.jbaby.wicket.oat.behaviors;

import com.github.openjson.JSONObject;
import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FeedbackMessage;

/**
 * Not a component-attaching behavior in the usual sense - a holder for the
 * static {@code toast(...)} helpers that trigger Oat's {@code ot.toast()} JS
 * notification via an Ajax response, or build its script for use outside one.
 * See {@link dev.jbaby.wicket.oat.Oat} for the public
 * entry point.
 */
public class OatToastBehavior extends Behavior {

    public static void toast(IPartialPageRequestHandler handler, String message) {
        toast(handler, message, OatVariant.DEFAULT);
    }

    public static void toast(IPartialPageRequestHandler handler, String message, OatVariant variant) {
        toast(handler, message, variant, null);
    }

    public static void toast(IPartialPageRequestHandler handler, String message, OatVariant variant, String title) {
        handler.appendJavaScript(script(message, variant, title));
    }

    /**
     * The JavaScript that shows a toast, for use outside an Ajax request (e.g. in an
     * {@code OnDomReadyHeaderItem}).
     */
    public static String script(String message, OatVariant variant, String title) {
        OatVariant v = variant != null ? variant : OatVariant.DEFAULT;
        // JSON string literals are valid JS string literals, and quote() escapes quotes,
        // backslashes, control characters and "</", so user-supplied text can't break out.
        String titleArg = title != null ? JSONObject.quote(title) : "null";
        // Oat's ot.toast() only recognizes variant: 'success'|'warning'|'danger'; omit the
        // option entirely for the default/unstyled case instead of sending an unknown value.
        String optionsArg = v.getValue() != null ? "{variant: " + JSONObject.quote(v.getValue()) + "}" : "{}";
        return "ot.toast(" + JSONObject.quote(message) + ", " + titleArg + ", " + optionsArg + ")";
    }
}

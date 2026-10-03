package dev.jbaby.wicket.oat;

import org.apache.wicket.feedback.FeedbackMessage;

/**
 * The color variants of Oat components, rendered as their {@code data-variant}
 * attribute. Not every component styles every variant; per Oat's CSS:
 * <ul>
 * <li>buttons: {@link #DEFAULT}, {@link #SECONDARY}, {@link #DANGER}</li>
 * <li>badges: all of them</li>
 * <li>alerts and toasts: {@link #DEFAULT}, {@link #SUCCESS}, {@link #WARNING}, {@link #DANGER}</li>
 * </ul>
 * A variant a component doesn't style renders like {@link #DEFAULT}.
 */
public enum OatVariant {
    DEFAULT(null),
    SECONDARY("secondary"),
    SUCCESS("success"),
    WARNING("warning"),
    DANGER("danger");

    private final String value;

    OatVariant(String value) {
        this.value = value;
    }

    /** The {@code data-variant} value, or {@code null} for {@link #DEFAULT}. */
    public String getValue() {
        return value;
    }

    /** The variant for a feedback message's level: errors are danger, info and debug the default. */
    public static OatVariant forFeedback(FeedbackMessage message) {
        if (message.isError()) return DANGER;
        if (message.isWarning()) return WARNING;
        if (message.isSuccess()) return SUCCESS;
        return DEFAULT;
    }
}

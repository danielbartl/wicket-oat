package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;

/**
 * Accepts the feedback messages that Oat form fields don't already show inline. Each
 * {@link BaseOatField} shows the first error reported by its wrapped form component, so
 * a page-level feedback panel or toast would only repeat it; so does any container that
 * implements {@link ShowsErrorsInline}. Used by default by
 * {@code OatFeedbackPanel} and {@code FeedbackToastsBehavior}.
 */
public class NotShownInlineFilter implements IFeedbackMessageFilter {

    @Override
    public boolean accept(FeedbackMessage message) {
        Component reporter = message.getReporter();
        if (reporter == null || !message.isError()) {
            return true;
        }
        if (reporter.getFeedbackMessages().first(FeedbackMessage.ERROR) != message) {
            return true;
        }
        BaseOatField<?, ?, ?> field = reporter.findParent(BaseOatField.class);
        if (field != null && field.getField() == reporter) {
            return false;
        }
        for (Component parent = reporter.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof ShowsErrorsInline inline && inline.showsErrorsOf(reporter)) {
                return false;
            }
        }
        return true;
    }
}

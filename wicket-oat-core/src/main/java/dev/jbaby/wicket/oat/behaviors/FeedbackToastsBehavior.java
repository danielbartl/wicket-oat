package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.form.NotShownInlineFilter;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shows feedback messages ({@code info()}, {@code success()}, {@code warn()},
 * {@code error()}) as Oat toasts, the toast variant following the message level.
 * Add it to a page, or to a base page so every page gets it:
 * {@code add(Oat.Behaviors.feedbackToasts())}.
 * <p>
 * It shows the messages of the component it is added to and of its children, plus
 * session messages - so a {@code getSession().success(...)} before
 * {@code setResponsePage(...)} appears on the next page. Messages are shown on a full
 * page render and during Ajax requests, without adding anything to the
 * {@code AjaxRequestTarget}. By default, errors that Oat form fields already show
 * inline are left out ({@link NotShownInlineFilter}).
 * <p>
 * Oat shows several toasts as a stack, the newest in front, until the pointer or the
 * focus is on them. So the messages of a request are shown in order of their level,
 * the most severe last, and it is the one of them in front.
 * <p>
 * Each message is shown once: it is marked rendered, so a feedback panel showing all
 * messages would display it too, but a second toast behavior would not.
 */
public class FeedbackToastsBehavior extends Behavior {

    private final IFeedbackMessageFilter filter;

    public FeedbackToastsBehavior() {
        this(new NotShownInlineFilter());
    }

    /**
     * @param filter the messages to show, or {@code null} for all messages
     */
    public FeedbackToastsBehavior(IFeedbackMessageFilter filter) {
        this.filter = filter;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        String script = consumeMessages(component);
        if (!script.isEmpty()) {
            response.render(OnDomReadyHeaderItem.forScript(script));
        }
    }

    @Override
    public void onEvent(Component component, IEvent<?> event) {
        super.onEvent(component, event);
        // Wicket broadcasts the AjaxRequestTarget to every component before responding
        if (event.getPayload() instanceof AjaxRequestTarget target) {
            String script = consumeMessages(component);
            if (!script.isEmpty()) {
                target.appendJavaScript(script);
            }
        }
    }

    private String consumeMessages(Component component) {
        // Oat stacks toasts with the newest in front, so the most severe message comes last.
        // A copy: collect() doesn't promise a list that can be sorted.
        List<FeedbackMessage> messages = new ArrayList<>(new FeedbackCollector(component, true).collect(this::accept));
        messages.sort(Comparator.comparingInt(FeedbackMessage::getLevel));
        StringBuilder script = new StringBuilder();
        for (FeedbackMessage message : messages) {
            message.markRendered();
            script.append(OatToastBehavior.script(String.valueOf(message.getMessage()),
                    OatVariant.forFeedback(message), null)).append(';');
        }
        return script.toString();
    }

    private boolean accept(FeedbackMessage message) {
        return !message.isRendered() && (filter == null || filter.accept(message));
    }
}

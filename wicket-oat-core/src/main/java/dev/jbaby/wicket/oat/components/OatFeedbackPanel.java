package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import dev.jbaby.wicket.oat.components.form.NotShownInlineFilter;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;

/**
 * A {@link FeedbackPanel} that renders each message as an Oat alert, its variant
 * following the message level: error, warning, success, or the default style for
 * info and debug messages. Filters work as with any {@code FeedbackPanel}; by default
 * it leaves out the errors Oat form fields already show inline
 * ({@link NotShownInlineFilter}).
 * <p>
 * During an Ajax request it adds itself to the response whenever it has messages to
 * show, or still shows messages from an earlier request, so there's no need to call
 * {@code target.add(feedbackPanel)}.
 *
 * <pre>
 * &lt;div wicket:id="feedback"&gt;&lt;/div&gt;
 * </pre>
 */
public class OatFeedbackPanel extends FeedbackPanel {

    private boolean showingMessages;

    public OatFeedbackPanel(String id) {
        this(id, new NotShownInlineFilter());
    }

    /**
     * @param filter the messages to show, or {@code null} for all messages
     */
    public OatFeedbackPanel(String id, IFeedbackMessageFilter filter) {
        super(id, filter);
        setOutputMarkupId(true);
    }

    @Override
    protected ListItem<FeedbackMessage> newMessageItem(int index, IModel<FeedbackMessage> itemModel) {
        ListItem<FeedbackMessage> item = super.newMessageItem(index, itemModel);
        item.add(new AlertBehavior(AlertBehavior.Variant.forFeedback(itemModel.getObject())));
        return item;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        showingMessages = anyMessage();
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        // Wicket broadcasts the AjaxRequestTarget to every component before responding
        if (event.getPayload() instanceof AjaxRequestTarget target
                && isVisibleInHierarchy()
                && (showingMessages || anyMessage())) {
            target.add(this);
        }
    }
}

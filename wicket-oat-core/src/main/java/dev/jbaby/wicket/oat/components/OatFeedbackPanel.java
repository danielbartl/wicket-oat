package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import dev.jbaby.wicket.oat.components.form.NotShownInlineFilter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FencedFeedbackPanel;
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
 * Like Wicket's {@link FencedFeedbackPanel}, which it extends, it can show the messages
 * of one container only, its fence: {@code new OatFeedbackPanel("formFeedback", form)}
 * shows those reported by the form and the components in it, such as the form's own
 * errors from {@code form.error(...)}, next to the form. Then a panel without a fence and
 * {@code Oat.Behaviors.feedbackToasts()} leave them out, so each message shows once.
 * <p>
 * During an Ajax request it adds itself to the response whenever it has messages to
 * show, or still shows messages from an earlier request, so there's no need to call
 * {@code target.add(feedbackPanel)}.
 *
 * <pre>
 * &lt;div wicket:id="feedback"&gt;&lt;/div&gt;
 * </pre>
 */
public class OatFeedbackPanel extends FencedFeedbackPanel {

    // Wicket's own fence mark is private, so the toasts can't see it
    private static final MetaDataKey<Integer> FENCE = new MetaDataKey<>() {
    };

    private final Component fence;
    private boolean showingMessages;

    /** A panel for the messages of the whole page, other than those a panel with a fence shows. */
    public OatFeedbackPanel(String id) {
        this(id, (Component) null);
    }

    /**
     * @param filter the messages to show, or {@code null} for all messages
     */
    public OatFeedbackPanel(String id, IFeedbackMessageFilter filter) {
        this(id, null, filter);
    }

    /**
     * A panel for the messages of {@code fence} and the components in it.
     *
     * @param fence the container, or {@code null} for the whole page
     */
    public OatFeedbackPanel(String id, Component fence) {
        this(id, fence, new NotShownInlineFilter());
    }

    /**
     * @param fence the container whose messages to show, or {@code null} for the whole page
     * @param filter the messages to show, or {@code null} for all messages
     */
    public OatFeedbackPanel(String id, Component fence, IFeedbackMessageFilter filter) {
        super(id, fence, filter);
        this.fence = fence;
        if (fence != null) {
            countFence(1);
        }
        setOutputMarkupId(true);
        add(AttributeModifier.append("class", "oat-feedback"));
    }

    /**
     * Whether an {@code OatFeedbackPanel} shows the messages of this component and the
     * components in it, so other feedback leaves them out.
     */
    public static boolean isFence(Component component) {
        return component.getMetaData(FENCE) != null;
    }

    private void countFence(int change) {
        Integer count = fence.getMetaData(FENCE);
        int changed = (count == null ? 0 : count) + change;
        fence.setMetaData(FENCE, changed > 0 ? changed : null);
    }

    @Override
    protected void onRemove() {
        super.onRemove();
        if (fence != null) {
            countFence(-1);
        }
    }

    @Override
    protected void onReAdd() {
        if (fence != null) {
            countFence(1);
        }
        super.onReAdd();
    }

    @Override
    protected ListItem<FeedbackMessage> newMessageItem(int index, IModel<FeedbackMessage> itemModel) {
        ListItem<FeedbackMessage> item = super.newMessageItem(index, itemModel);
        item.add(new AlertBehavior(OatVariant.forFeedback(itemModel.getObject())));
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

package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.behaviors.AjaxBusyBehavior;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.Iterator;
import java.util.Objects;

/**
 * A long list - an activity feed, search results, a product catalogue - that shows
 * its first items and appends more as the user asks for them, instead of pages:
 * <pre>{@code
 * add(new OatLoadMoreList<>("orders", ordersProvider, 20, (id, order) -> new OrderCard(id, order)));
 * }</pre>
 * The items come from a Wicket {@link IDataProvider}, a batch at a time. "Load more"
 * appends the next batch over Ajax without re-rendering the items already shown, and
 * shows how many there are ({@code OatLoadMoreList.more}: "Load more (20 of 134)").
 * {@link #setLoadOnScroll(boolean) setLoadOnScroll(true)} also loads the next batch when
 * the button scrolls into view, using Wicket Oat's small script.
 * <p>
 * Loaded items stay in the page, so it suits hundreds of items; for more, or when users
 * jump around, page with {@code OatDataTable} or {@code OatPagingNavigator} instead.
 * Each item is rendered on a {@code <div role="listitem">}.
 *
 * @param <T> the item type
 */
public class OatLoadMoreList<T> extends Panel {

    private final IDataProvider<T> provider;
    private final long batchSize;
    private final SerializableBiFunction<String, IModel<T>, ? extends Component> item;
    private final WebMarkupContainer list;
    private final RepeatingView batches;
    private final AjaxLink<Void> more;
    private long shown;
    private boolean loadOnScroll;

    /**
     * @param provider the items
     * @param batchSize how many items each batch has
     * @param item creates an item's component with the id and model it is given
     */
    public OatLoadMoreList(String id, IDataProvider<T> provider, long batchSize,
                           SerializableBiFunction<String, IModel<T>, ? extends Component> item) {
        super(id);
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be at least 1: " + batchSize);
        }
        this.provider = Objects.requireNonNull(provider);
        this.batchSize = batchSize;
        this.item = Objects.requireNonNull(item);
        setOutputMarkupId(true);

        list = new WebMarkupContainer("list");
        list.setOutputMarkupId(true);
        add(list);
        batches = new RepeatingView("batch");
        list.add(batches);

        more = new AjaxLink<>("more") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                loadMore(target);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(shown < provider.size());
            }
        };
        more.setOutputMarkupPlaceholderTag(true);
        more.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE));
        more.add(new AjaxBusyBehavior());
        more.add(AttributeModifier.replace("data-oat-load-on-scroll", (IModel<String>) () -> loadOnScroll ? "" : null));
        more.add(new Label("label", new StringResourceModel("OatLoadMoreList.more", this)
                .setParameters((IModel<Long>) () -> shown, (IModel<Long>) provider::size)
                .setDefaultValue("Load more")));
        add(more);

        addBatch();
    }

    /** Also loads the next batch when the button scrolls into view, as in an endless feed. */
    public OatLoadMoreList<T> setLoadOnScroll(boolean loadOnScroll) {
        this.loadOnScroll = loadOnScroll;
        return this;
    }

    /** Shows the next batch; does nothing once all items are shown. */
    public OatLoadMoreList<T> loadMore(AjaxRequestTarget target) {
        if (shown >= provider.size()) {
            return this;
        }
        Component batch = addBatch();
        // Make room for the new batch at the end, then render only it
        target.prependJavaScript("(function(l){if(l){var b=document.createElement('div');b.id='" + batch.getMarkupId()
                + "';l.appendChild(b);}})(document.getElementById('" + list.getMarkupId() + "'))");
        target.add(batch, more);
        return this;
    }

    /** Starts again from the first batch, e.g. after the provider's filter changed. */
    public OatLoadMoreList<T> reset(AjaxRequestTarget target) {
        batches.removeAll();
        shown = 0;
        addBatch();
        target.add(this);
        return this;
    }

    private Component addBatch() {
        Batch batch = new Batch(batches.newChildId(), shown, Math.min(batchSize, provider.size() - shown));
        batches.add(batch);
        shown += batch.count;
        return batch;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (loadOnScroll) {
            response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS));
        }
    }

    @Override
    protected void onDetach() {
        provider.detach();
        super.onDetach();
    }

    /** One batch of items, rendered once when loaded. */
    private final class Batch extends WebMarkupContainer {

        private final long count;

        Batch(String id, long first, long count) {
            super(id);
            this.count = count;
            setOutputMarkupId(true);
            RepeatingView items = new RepeatingView("item");
            add(items);
            Iterator<? extends T> rows = provider.iterator(first, count);
            while (rows.hasNext()) {
                IModel<T> model = provider.model(rows.next());
                Component component = item.apply(items.newChildId(), model);
                items.add(component);
            }
        }
    }
}

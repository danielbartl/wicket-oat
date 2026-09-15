package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 * A container component that renders its items as an Oat tabbed interface using
 * the {@code ot-tabs} web component (JS vendored in oat.min.js). Tab buttons and
 * panels are two lists driven by the same model; {@code ot-tabs} wires
 * {@code id}/{@code aria-controls} between them automatically by DOM position, so
 * no manual id-matching is needed here. Unlike {@link OatDialog}/{@link OatDropdown},
 * this is a plain container (not a self-templated Panel) since each tab panel
 * typically needs its own arbitrary markup shape, which the consuming page supplies.
 *
 * <pre>
 * &lt;ot-tabs wicket:id="tabs"&gt;
 *     &lt;div wicket:id="tablist" role="tablist"&gt;
 *         &lt;button wicket:id="tabButtons" role="tab"&gt;&lt;span wicket:id="tabLabel"&gt;&lt;/span&gt;&lt;/button&gt;
 *     &lt;/div&gt;
 *     &lt;div wicket:id="tabPanels" role="tabpanel"&gt;...&lt;/div&gt;
 * &lt;/ot-tabs&gt;
 * </pre>
 *
 * @param <T> the type of the tab items
 */
public abstract class OatTabs<T> extends WebMarkupContainer {

    public OatTabs(String id, IModel<List<T>> model) {
        super(id);

        WebMarkupContainer tablist = new WebMarkupContainer("tablist");
        add(tablist);
        tablist.add(new ListView<>("tabButtons", model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OatTabs.this.populateTab(item);
            }
        });

        add(new ListView<>("tabPanels", model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OatTabs.this.populatePanel(item);
            }
        });
    }

    /** Populate a tab button; the item's root tag should carry {@code role="tab"}. */
    protected abstract void populateTab(ListItem<T> item);

    /** Populate a tab panel; the item's root tag should carry {@code role="tabpanel"}. */
    protected abstract void populatePanel(ListItem<T> item);
}

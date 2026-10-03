package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 * Wicket's {@link AjaxTabbedPanel} rendered as Oat tabs ({@code role="tablist"},
 * {@code role="tab"} with {@code aria-selected}, {@code role="tabpanel"}). Tabs are
 * {@link ITab}s, e.g. {@code Oat.tab("Profile", ProfilePanel::new)}, and only the
 * selected tab's panel is created: switching tabs replaces it over Ajax (or with a
 * full request when JavaScript is off). Pass an {@code IModel<Integer>} to keep the
 * selected tab index somewhere of your own.
 * <p>
 * Use it when panels are expensive or need server state; {@link OatTabs} renders every
 * panel up front and switches between them in the browser.
 *
 * <pre>
 * add(new OatTabbedPanel&lt;&gt;("tabs", List.of(
 *         Oat.tab("Profile", ProfilePanel::new),
 *         Oat.tab("Settings", SettingsPanel::new))));
 *
 * &lt;div wicket:id="tabs"&gt;&lt;/div&gt;
 * </pre>
 *
 * @param <T> the tab type
 */
public class OatTabbedPanel<T extends ITab> extends AjaxTabbedPanel<T> {

    public OatTabbedPanel(String id, List<T> tabs) {
        super(id, tabs);
        setOutputMarkupId(true);
    }

    public OatTabbedPanel(String id, List<T> tabs, IModel<Integer> selectedTab) {
        super(id, tabs, selectedTab);
        setOutputMarkupId(true);
    }

    @Override
    protected WebMarkupContainer newTabsContainer(String id) {
        // role="tablist" is in the markup; skip TabbedPanel's "tab-row" class
        return new WebMarkupContainer(id);
    }

    @Override
    protected LoopItem newTabContainer(int tabIndex) {
        // Keep TabbedPanel's per-tab visibility, but render the link alone as the tab
        LoopItem item = super.newTabContainer(tabIndex);
        item.setRenderBodyOnly(true);
        return item;
    }

    @Override
    protected WebMarkupContainer newLink(String linkId, int index) {
        WebMarkupContainer link = super.newLink(linkId, index);
        link.setOutputMarkupId(true);
        link.add(AttributeModifier.replace("aria-selected", (IModel<String>) () -> String.valueOf(index == getSelectedTab())));
        link.add(AttributeModifier.replace("aria-controls", (IModel<String>) () -> getPanel().getMarkupId()));
        return link;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // TabbedPanel has just put the selected tab's panel in place
        getPanel().setOutputMarkupId(true);
    }

    private Component getPanel() {
        return get(TAB_PANEL_ID);
    }
}

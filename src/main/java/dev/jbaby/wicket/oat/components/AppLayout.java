package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class AppLayout extends WebPage {

    protected WebMarkupContainer footer;
    private WebMarkupContainer sidebar;

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        // Shared assets for all pages using this layout
        response.render(CssHeaderItem.forUrl("https://unpkg.com/@knadh/oat/oat.min.css"));
        response.render(JavaScriptHeaderItem.forUrl("https://unpkg.com/@knadh/oat/oat.min.js"));
    }

    public AppLayout() {
        // Put shared components here later if needed (nav, user menu, feedback, etc.)
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        appTitle();
        appName();
        sidebar();
        sidebarMenu();
        footer();

    }

    protected void appTitle() {
        add(new Label("appTitle", appTitleModel()));
    }

    protected @NonNull IModel<?> appTitleModel() {
        return Model.of("Wicket Oat Application");
    }

    protected void appName() {
        add(new Label("appName", appNameModel()));
    }

    protected @NonNull IModel<?> appNameModel() {
        return Model.of("Wicket Oat Application");
    }

    protected void sidebar() {
        sidebar = new WebMarkupContainer("sidebar");
        add(sidebar);
    }

    /**
     * Child pages/apps customize the menu by overriding {@link #sidebarMenuItemsModel()}.
     * AppLayout owns the rendering (UL/LI) via a ListView.
     */
    protected void sidebarMenu() {
        sidebar.add(new ListView<>("menuItems", sidebarMenuItemsModel()) {
            @Override
            protected void populateItem(ListItem<MenuItem> item) {

                MenuItem mi = item.getModelObject();

                BookmarkablePageLink<?> link = new BookmarkablePageLink<>("link", mi.pageClass());
                link.add(new Label("label", mi.label()));
                item.add(link);

                boolean isCurrent = getPage().getClass().equals(mi.pageClass());

                if (isCurrent) {
                    // Accessibility-friendly "current page" marker
                    link.add(AttributeModifier.replace("aria-current", "page"));

                    // Optional: allow custom styling via CSS on the <li>
                    item.add(new AttributeAppender("class", Model.of("active"), " "));

                } else {

                    // Ensure non-current items don't keep aria-current from reuse
                    link.add(AttributeModifier.remove("aria-current"));

                }

            }
        });
    }

    /**
     * Override in child pages (or better: in an app-specific base page) to supply menu items.
     */
    protected @NonNull IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of());
    }


    protected void footer() {
        footer = new WebMarkupContainer("footer");
        sidebar.add(footer);
    }
}
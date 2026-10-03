package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.OatThemeBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Base page providing the Oat UI application layout (sidebar + topnav).
 * Override factory methods (e.g., {@link #createFooter(String)}) to customize the layout.
 */
public abstract class OatAppLayout extends WebPage {

    protected WebMarkupContainer sidebar;
    protected WebMarkupContainer topNav;

    public OatAppLayout() {
        this((PageParameters) null);
    }

    /** For bookmarkable pages: pass their {@code PageParameters} on. */
    public OatAppLayout(PageParameters parameters) {
        super(parameters);
        addThemedHtml();
    }

    public OatAppLayout(IModel<?> model) {
        super(model);
        addThemedHtml();
    }

    private void addThemedHtml() {
        TransparentWebMarkupContainer html = new TransparentWebMarkupContainer("html");
        html.add(new OatThemeBehavior());
        add(html);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new Label("appTitle", appTitleModel()));
        
        topNav();
        sidebar();
    }

    /** The page {@code <title>}; by default the {@code OatAppLayout.title} resource. */
    protected @NonNull IModel<String> appTitleModel() {
        return new StringResourceModel("OatAppLayout.title", this).setDefaultValue("Wicket Oat Application");
    }

    protected void topNav() {
        topNav = new WebMarkupContainer("topNav");
        add(topNav);

        topNav.add(new Label("appName", appNameModel()));
        topNav.add(createTopNavExtra("topNavExtra"));
    }

    /** The name in the top navigation; by default the {@code OatAppLayout.name} resource. */
    protected @NonNull IModel<String> appNameModel() {
        return new StringResourceModel("OatAppLayout.name", this).setDefaultValue("Wicket Oat Application");
    }

    /**
     * Hook to add extra components to the top navigation (e.g., user profile, search).
     */
    protected Component createTopNavExtra(String id) {
        return new WebMarkupContainer(id).setVisible(false);
    }

    protected void sidebar() {
        sidebar = new WebMarkupContainer("sidebar");
        add(sidebar);

        sidebarMenu();
        sidebar.add(createFooter("footer"));
    }

    /**
     * Child pages/apps customize the menu by overriding {@link #sidebarMenuItemsModel()}.
     */
    protected void sidebarMenu() {
        sidebar.add(new ListView<>("menuItems", sidebarMenuItemsModel()) {
            @Override
            protected void populateItem(ListItem<MenuItem> item) {
                MenuItem mi = item.getModelObject();

                BookmarkablePageLink<?> link = new BookmarkablePageLink<>("link", mi.pageClass(), mi.parameters());
                link.add(new Label("label", mi.label()));
                item.add(link);

                // Leave out pages the current user may not open
                item.setVisible(getApplication().getSecuritySettings().getAuthorizationStrategy()
                        .isInstantiationAuthorized(mi.pageClass()));

                if (isCurrentPage(mi)) {
                    link.add(AttributeModifier.replace("aria-current", "page"));
                    item.add(new AttributeAppender("class", Model.of("active"), " "));
                } else {
                    link.add(AttributeModifier.remove("aria-current"));
                }
            }
        });
    }

    /**
     * Whether a menu item points at this page: the same page class and, if the item
     * has parameters, the same parameters.
     */
    protected boolean isCurrentPage(MenuItem item) {
        return getPage().getClass().equals(item.pageClass())
                && (item.parameters() == null || item.parameters().equals(getPage().getPageParameters()));
    }

    /**
     * Override to supply menu items for the sidebar.
     */
    protected @NonNull IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of());
    }

    /**
     * Hook to customize the sidebar footer. 
     * Default implementation is an empty container.
     */
    protected Component createFooter(String id) {
        return new WebMarkupContainer(id).setVisible(false);
    }
}

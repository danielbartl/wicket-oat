package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Base page for a phone-first app: the page's content in a {@code <main>} column, and an
 * {@link OatBottomNav} fixed below it, its tabs from {@link #bottomNavItemsModel()}:
 * <pre>{@code
 * public abstract class TimeOffPage extends OatMobileLayout {
 *     @Override
 *     protected IModel<List<BottomNavItem>> bottomNavItemsModel() {
 *         return Model.ofList(List.of(
 *                 BottomNavItem.of("Today", TodayPage.class).withIcon(Icon.named("calendar")),
 *                 BottomNavItem.of("Team", TeamPage.class).withIcon(Icon.named("users"))));
 *     }
 * }
 * }</pre>
 * The content is as wide as the bar's row of tabs ({@code --oat-bottom-nav-max-width},
 * 28rem by default), with room below so the bar never covers it. Like the bar, the room
 * goes away on screens wider than 768px, unless {@link #createBottomNav} makes the bar
 * {@linkplain OatBottomNav#setAlwaysVisible always visible}.
 * <p>
 * Like {@link OatAppLayout}, it applies the user's theme, the density and the page's
 * {@code lang} to {@code <html>}. Its viewport meta tag has {@code viewport-fit=cover},
 * which lets the bar keep clear of the iPhone's home indicator. The {@code <title>} is the
 * {@code OatAppLayout.title} resource, as for {@code OatAppLayout}.
 */
public abstract class OatMobileLayout extends WebPage {

    /** The tab bar; re-render it from an Ajax handler with {@code target.add(bottomNav)}, e.g. to update badges. */
    protected OatBottomNav bottomNav;

    public OatMobileLayout() {
        this((PageParameters) null);
    }

    /** For bookmarkable pages: pass their {@code PageParameters} on. */
    public OatMobileLayout(PageParameters parameters) {
        super(parameters);
        add(new LayoutHtml("html"));
    }

    public OatMobileLayout(IModel<?> model) {
        super(model);
        add(new LayoutHtml("html"));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new Label("appTitle", appTitleModel()));

        bottomNav = createBottomNav("bottomNav", bottomNavItemsModel());
        bottomNav.setOutputMarkupId(true);
        add(bottomNav);
    }

    /** The page {@code <title>}; by default the {@code OatAppLayout.title} resource. */
    protected @NonNull IModel<String> appTitleModel() {
        return new StringResourceModel("OatAppLayout.title", this).setDefaultValue("Wicket Oat Application");
    }

    /**
     * The tabs of the bottom bar: three to five {@link BottomNavItem}s, at most one of
     * them {@linkplain BottomNavItem#asPrimary primary}.
     */
    protected abstract @NonNull IModel<List<BottomNavItem>> bottomNavItemsModel();

    /**
     * Hook to customize the bottom bar, e.g. {@code setAlwaysVisible(true)}, or a subclass
     * of {@link OatBottomNav} with links of its own.
     */
    protected OatBottomNav createBottomNav(String id, IModel<List<BottomNavItem>> items) {
        return new OatBottomNav(id, items);
    }
}

package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.components.MenuItem;
import dev.jbaby.wicket.oat.components.OatAppLayout;
import dev.jbaby.wicket.oat.components.OatThemeSwitcher;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/** An OatAppLayout page with a breadcrumb and the theme switcher, for whole-page checks. */
public class LayoutTestPage extends OatAppLayout {

    public LayoutTestPage() {
        add(Oat.Components.breadcrumb("breadcrumb", Model.ofList(List.of("Home", "Here")), (item, label) -> {
            BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", LayoutTestPage.class);
            link.add(new Label("label", label));
            item.add(link);
        }));
    }

    @Override
    protected IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of(MenuItem.of("Home", LayoutTestPage.class)));
    }

    @Override
    protected Component createFooter(String id) {
        return new OatThemeSwitcher(id);
    }
}

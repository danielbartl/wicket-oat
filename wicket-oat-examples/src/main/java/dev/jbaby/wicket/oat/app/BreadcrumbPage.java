package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatBreadcrumb;
import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;

import java.util.List;

public class BreadcrumbPage extends BasePage {

    public BreadcrumbPage() {
        List<String> crumbs = List.of("Home", "Docs", "Breadcrumb");

        add(OatBreadcrumb.of("breadcrumb", Model.ofList(crumbs), BreadcrumbPage::folderLink));

        // A long trail: the first level, "…" with the ones in between, and the last two
        List<String> folders = List.of("Home", "Projects", "2026", "Wicket Oat", "Docs", "Components", "Breadcrumb");
        add(OatBreadcrumb.of("longBreadcrumb", Model.ofList(folders), BreadcrumbPage::folderLink).setMaxItems(3));
    }

    private static void folderLink(ListItem<String> item, String label) {
        Link<Void> link = new Link<>("link") {
            @Override
            public void onClick() {}
        };
        link.add(new Label("label", label));
        item.add(link);
    }
}

package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import java.util.List;

public class BreadcrumbPage extends BasePage {

    public BreadcrumbPage() {
        List<String> crumbs = List.of("Home", "Docs", "Breadcrumb");

        add(Oat.Components.breadcrumb("breadcrumb", Model.ofList(crumbs), (item, label) -> {
            Link<Void> link = new Link<>("link") {
                @Override
                public void onClick() {}
            };
            link.add(new Label("label", label));
            item.add(link);
        }));
    }
}

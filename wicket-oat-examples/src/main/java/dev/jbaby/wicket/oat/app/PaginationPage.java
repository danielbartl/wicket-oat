package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import java.util.List;

public class PaginationPage extends BasePage {

    private static final int CURRENT_PAGE = 3;

    public PaginationPage() {
        List<Integer> pages = List.of(1, 2, 3, 4, 5);

        add(Oat.Components.pagination("pagination", Model.ofList(pages), (item, page) -> {
            Link<Void> link = new Link<>("link") {
                @Override
                public void onClick() {}
            };
            link.add(new Label("label", String.valueOf(page)));
            boolean current = page == CURRENT_PAGE;
            link.add(AttributeModifier.replace("class", current ? "button small" : "button outline small"));
            if (current) {
                link.add(AttributeModifier.replace("aria-current", "page"));
            }
            item.add(link);
        }));
    }
}

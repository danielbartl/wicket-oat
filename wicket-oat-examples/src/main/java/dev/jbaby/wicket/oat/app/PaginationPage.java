package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.OatAjaxPagingNavigator;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import java.util.List;
import java.util.stream.IntStream;

public class PaginationPage extends BasePage {

    private static final int CURRENT_PAGE = 3;

    public PaginationPage() {
        // A pageable repeater with the Oat-styled Wicket navigator (Ajax variant)
        WebMarkupContainer results = new WebMarkupContainer("results");
        results.setOutputMarkupId(true); // the Ajax navigator re-renders this
        add(results);

        PageableListView<Integer> items = new PageableListView<>("items",
                IntStream.rangeClosed(1, 42).boxed().toList(), 10) {
            @Override
            protected void populateItem(ListItem<Integer> item) {
                item.add(new Label("item", "Item " + item.getModelObject()));
            }
        };
        results.add(items);
        add(new OatAjaxPagingNavigator("navigator", items));

        // Hand-rolled pagination for when there's no IPageable
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

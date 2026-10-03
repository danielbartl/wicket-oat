package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

/** A bookmarkable OatAppLayout page with parameterized menu items and a protected one. */
public class ParamLayoutPage extends OatAppLayout {

    /** A page the test's authorization strategy denies; never rendered. */
    public static class SecretPage extends WebPage {
    }

    public ParamLayoutPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of(
                MenuItem.of("Project 1", ParamLayoutPage.class, new PageParameters().add("id", 1)),
                MenuItem.of("Project 2", ParamLayoutPage.class, new PageParameters().add("id", 2)),
                MenuItem.of("Secret", SecretPage.class)));
    }
}

package dev.jbaby.wicket.oat.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

/** An OatAppLayout page whose menu links are AjaxLinks, through newMenuLink. */
public class AjaxMenuPage extends OatAppLayout {

    public final List<String> clicked = new ArrayList<>();

    @Override
    protected IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of(MenuItem.of("Reports", AjaxMenuPage.class)));
    }

    @Override
    protected AbstractLink newMenuLink(String id, MenuItem item) {
        return new AjaxLink<Void>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                clicked.add(item.label().getObject());
                target.add(getSidebar());
            }
        };
    }
}

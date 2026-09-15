package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;

public class TabsPage extends BasePage {

    record TabData(String label, String content) implements Serializable {}

    public TabsPage() {
        List<TabData> tabs = List.of(
                new TabData("Account", "Manage your account information here."),
                new TabData("Password", "Change your password here."),
                new TabData("Notifications", "Configure your notification preferences.")
        );

        add(Oat.Components.tabs("tabs", Model.ofList(tabs),
                (item, tab) -> item.add(new org.apache.wicket.markup.html.basic.Label("tabLabel", tab.label())),
                (item, tab) -> item.add(new org.apache.wicket.markup.html.basic.Label("panelContent", tab.content()))));
    }
}

package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TabsPage extends BasePage {

    record TabData(String label, String content) implements Serializable {}

    public TabsPage() {
        // Server-side tabs: only the selected tab's panel exists; switching swaps it over Ajax
        add(Oat.Components.tabbedPanel("tabbedPanel", List.of(
                Oat.tab("Overview", id -> createdAt(new Fragment(id, "overviewTab", this))),
                Oat.tab("Activity", id -> createdAt(new Fragment(id, "activityTab", this))),
                Oat.tab("Settings", id -> createdAt(new Fragment(id, "settingsTab", this))))));

        // Client-side tabs: every panel is rendered up front
        List<TabData> tabs = List.of(
                new TabData("Account", "Manage your account information here."),
                new TabData("Password", "Change your password here."),
                new TabData("Notifications", "Configure your notification preferences.")
        );

        add(Oat.Components.tabs("tabs", Model.ofList(tabs),
                (item, tab) -> item.add(new Label("tabLabel", tab.label())),
                (item, tab) -> item.add(new Label("panelContent", tab.content()))));
    }

    /** Shows when the tab's panel was created, to make the lazy creation visible. */
    private static Fragment createdAt(Fragment panel) {
        panel.add(new Label("createdAt", LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString()));
        return panel;
    }
}

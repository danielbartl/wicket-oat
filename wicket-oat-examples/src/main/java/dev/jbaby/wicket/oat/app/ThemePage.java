package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatSettings;
import dev.jbaby.wicket.oat.OatTheme;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class ThemePage extends BasePage {

    public ThemePage() {
        add(new Label("currentTheme", () -> {
            OatTheme current = OatTheme.current();
            return current == null ? "browser default" : current.label() + " (" + current.value() + ")";
        }));

        // The custom theme registered in WicketOatApplication
        add(themeLink("useOcean", OatSettings.get().findTheme("ocean")));
        add(themeLink("useMidnight", OatTheme.MIDNIGHT));
        add(themeLink("useDefault", null));

        // This card keeps the Light theme whatever the page uses
        WebMarkupContainer pinned = new WebMarkupContainer("pinnedCard");
        pinned.add(Oat.Behaviors.theme(OatTheme.LIGHT));
        add(pinned);
    }

    private static Link<Void> themeLink(String id, OatTheme theme) {
        Link<Void> link = new Link<>(id) {
            @Override
            public void onClick() {
                OatTheme.setCurrent(theme);
            }
        };
        link.add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE));
        return link;
    }
}

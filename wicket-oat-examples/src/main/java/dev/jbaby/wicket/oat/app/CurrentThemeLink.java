package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

/** The current theme in the top navigation, linking to the theming demo. */
public class CurrentThemeLink extends Panel {

    public CurrentThemeLink(String id) {
        super(id);
        BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", ThemePage.class);
        link.add(new Label("theme", () -> {
            OatTheme theme = OatTheme.current();
            if (theme == null) {
                return "Browser default";
            }
            return theme.icon() != null ? theme.icon() + " " + theme.label() : theme.label();
        }));
        add(link);
    }
}

package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatSettings;
import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.List;

/**
 * Links to switch between the themes registered in {@link OatSettings} - the built-in
 * ones plus any added with {@link OatSettings#addTheme}. Each link shows the theme's
 * icon (or its label if it has none); the current theme is marked
 * {@code aria-current="true"}.
 */
public class OatThemeSwitcher extends Panel {

    public OatThemeSwitcher(String id) {
        super(id);

        add(new ListView<>("themes", LoadableDetachableModel.of(() -> List.copyOf(OatSettings.get().getThemes()))) {
            @Override
            protected void populateItem(ListItem<OatTheme> item) {
                OatTheme theme = item.getModelObject();
                Link<Void> link = new Link<>("link") {
                    @Override
                    public void onClick() {
                        OatTheme.setCurrent(theme);
                    }
                };
                link.add(AttributeModifier.replace("title", theme.label()));
                link.add(AttributeModifier.replace("aria-label", theme.label()));
                link.add(AttributeModifier.replace("aria-current", () -> theme.equals(OatTheme.current()) ? "true" : null));
                link.add(new Label("icon", theme.icon() != null ? theme.icon() : theme.label()));
                item.add(link);
            }
        });
    }
}

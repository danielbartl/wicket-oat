package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatSession;
import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A reusable panel that provides links to switch between all built-in Oat themes.
 */
public class OatThemeSwitcher extends Panel {

    public OatThemeSwitcher(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new ThemeLink("lightTheme", OatTheme.LIGHT));
        add(new ThemeLink("darkTheme", OatTheme.DARK));
        add(new ThemeLink("midnightTheme", OatTheme.MIDNIGHT));
        add(new ThemeLink("nordTheme", OatTheme.NORD));
        add(new ThemeLink("everforestTheme", OatTheme.EVERFOREST));
        add(new ThemeLink("tokyoNightTheme", OatTheme.TOKYO_NIGHT));
        add(new ThemeLink("rosePineDawnTheme", OatTheme.ROSE_PINE_DAWN));
        add(new ThemeLink("royalTheme", OatTheme.ROYAL));
        add(new ThemeLink("clayTheme", OatTheme.CLAY));
        add(new ThemeLink("catppuccinMochaTheme", OatTheme.CATPPUCCIN_MOCHA));
        add(new ThemeLink("catppuccinLatteTheme", OatTheme.CATPPUCCIN_LATTE));
        add(new ThemeLink("materialTheme", OatTheme.MATERIAL));
        add(new ThemeLink("daisyTheme", OatTheme.DAISY));
        add(new ThemeLink("ultravioletTheme", OatTheme.ULTRAVIOLET));
        add(new ThemeLink("halloweenTheme", OatTheme.HALLOWEEN));
        add(new ThemeLink("xmasTheme", OatTheme.XMAS));
        add(new ThemeLink("wireframeTheme", OatTheme.WIREFRAME));
    }

    private static class ThemeLink extends Link<Void> {
        private final OatTheme theme;

        public ThemeLink(String id, OatTheme theme) {
            super(id);
            this.theme = theme;
        }

        @Override
        public void onClick() {
            OatSession.get().setTheme(theme);
        }
    }
}

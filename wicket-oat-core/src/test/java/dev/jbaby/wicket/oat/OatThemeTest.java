package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.behaviors.OatThemeBehavior;
import dev.jbaby.wicket.oat.components.OatThemeSwitcher;
import jakarta.servlet.http.Cookie;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OatThemeTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    /** Renders a container with the theme behavior and returns its data-theme. */
    private String renderedTheme(OatThemeBehavior behavior) {
        WebMarkupContainer container = new WebMarkupContainer("html");
        container.add(behavior);
        tester.startComponentInPage(container, Markup.of("<div wicket:id='html'></div>"));
        return tester.getTagByWicketId("html").getAttribute("data-theme");
    }

    @Test
    void usersWithoutAChoiceGetTheDefaultTheme() {
        assertThat(renderedTheme(new OatThemeBehavior())).isEqualTo("dark");

        OatSettings.get(tester.getApplication()).setDefaultTheme(OatTheme.LIGHT);
        assertThat(renderedTheme(new OatThemeBehavior())).isEqualTo("light");
    }

    @Test
    void withoutADefaultThemeNoDataThemeIsSetSoOatFollowsTheBrowser() {
        OatSettings.get(tester.getApplication()).setDefaultTheme(null);
        assertThat(renderedTheme(new OatThemeBehavior())).isNull();
    }

    /** The oat-theme cookie set by the last request or the redirect before it. */
    private Cookie themeCookie() {
        return java.util.stream.Stream.concat(tester.getPreviousResponses().stream(), java.util.stream.Stream.of(tester.getLastResponse()))
                .flatMap(r -> r.getCookies().stream())
                .filter(c -> c.getName().equals("oat-theme"))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    @Test
    void choosingAThemeStoresItInALongLivedCookie() {
        tester.startComponentInPage(new OatThemeSwitcher("switcher"));
        tester.clickLink("switcher:themes:3:link"); // NORD

        assertThat(OatTheme.current()).isEqualTo(OatTheme.NORD);
        Cookie cookie = themeCookie();
        assertThat(cookie.getValue()).isEqualTo("nord");
        assertThat(cookie.getMaxAge()).isEqualTo(365 * 24 * 60 * 60);
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    void aNewSessionRestoresTheThemeFromTheCookie() {
        tester.getRequest().addCookie(new Cookie("oat-theme", "nord"));
        assertThat(OatTheme.current()).isEqualTo(OatTheme.NORD);
    }

    @Test
    void anUnknownStoredThemeFallsBackToTheDefault() {
        tester.getRequest().addCookie(new Cookie("oat-theme", "no-such-theme"));
        assertThat(OatTheme.current()).isEqualTo(OatTheme.DARK);
    }

    @Test
    void customThemesCanBeRegisteredAndChosen() {
        OatTheme brand = new OatTheme("brand", "Brand");
        OatSettings.get(tester.getApplication()).addTheme(brand);

        OatTheme.setCurrent(brand);
        assertThat(renderedTheme(new OatThemeBehavior())).isEqualTo("brand");
    }

    @Test
    void themeValuesAreRestrictedToSafeNames() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OatTheme("Brand Theme", "Brand"));
        assertThatIllegalArgumentException().isThrownBy(() -> new OatTheme("x\" onclick=\"y", "X"));
    }

    @Test
    void aComponentCanBePinnedToATheme() {
        OatTheme.setCurrent(OatTheme.LIGHT);
        assertThat(renderedTheme(new OatThemeBehavior(OatTheme.MIDNIGHT))).isEqualTo("midnight");
    }

    @Test
    void theSessionStoreKeepsTheThemeWithoutACookie() {
        OatSettings.get(tester.getApplication()).setThemeStore(new SessionThemeStore());
        tester.startComponentInPage(new OatThemeSwitcher("switcher"));
        tester.clickLink("switcher:themes:8:link"); // CLAY

        assertThat(OatTheme.current()).isEqualTo(OatTheme.CLAY);
        assertThat(themeCookie()).isNull();
        assertThat(tester.getSession().isTemporary()).isFalse();
    }

    @Test
    void theSwitcherListsTheRegisteredThemesAndMarksTheCurrentOne() {
        OatSettings.get(tester.getApplication())
                .setThemes(List.of(OatTheme.LIGHT, OatTheme.DARK))
                .addTheme(new OatTheme("brand", "Brand"));
        OatTheme.setCurrent(OatTheme.DARK);
        tester.startComponentInPage(new OatThemeSwitcher("switcher"));

        List<TagTester> links = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "link", false);
        assertThat(links).extracting(l -> l.getAttribute("title")).containsExactly("Light", "Dark", "Brand");
        assertThat(links).extracting(l -> l.getAttribute("aria-current")).containsExactly(null, "true", null);
        // A theme without an icon shows its label
        assertThat(tester.getLastResponseAsString()).contains(">Brand</span>");

        tester.clickLink("switcher:themes:0:link");
        assertThat(OatTheme.current()).isEqualTo(OatTheme.LIGHT);
    }
}

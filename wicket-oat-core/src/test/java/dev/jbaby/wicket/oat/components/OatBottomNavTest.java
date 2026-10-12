package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.BottomNavItem.Icon;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OatBottomNavTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getApplication().getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy() {
            @Override
            public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
                return componentClass != MobileTestPage.Secret.class;
            }
        });
    }

    /** Markup for a bar with the id "nav" (it needs an application, so not a constant). */
    private static Markup nav() {
        return Markup.of("<nav wicket:id='nav'></nav>");
    }

    private List<TagTester> links() {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "link", false);
    }

    private List<TagTester> tagsWithClass(String cssClass) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "class", cssClass, false);
    }

    private static BottomNavItem tab(String label, Class<? extends Page> page) {
        return BottomNavItem.of(label, page).withIcon(Icon.named("calendar"));
    }

    @Test
    void rendersEachItemAsALinkWithItsLabel() {
        tester.startComponentInPage(Oat.Components.bottomNav("nav", List.of(
                tab("Today", MobileTestPage.Today.class),
                tab("Team", MobileTestPage.Team.class),
                // An item's parameters are on its link
                BottomNavItem.of("Member 7", MobileTestPage.Member.class, new PageParameters().add("id", 7))
                        .withIcon(Icon.named("user")))), nav());

        TagTester nav = tester.getTagByWicketId("nav");
        assertThat(nav.getName()).isEqualTo("nav");
        assertThat(nav.getAttribute("class")).isEqualTo("oat-bottom-nav");
        assertThat(links()).hasSize(3);
        assertThat(links()).allMatch(link -> "a".equals(link.getName()));
        assertThat(TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "label", false))
                .extracting(TagTester::getValue).containsExactly("Today", "Team", "Member 7");
        assertThat(links()).extracting(link -> link.getAttribute("href")).satisfiesExactly(
                href -> assertThat(href).endsWith("MobileTestPage$Today"),
                href -> assertThat(href).endsWith("MobileTestPage$Team"),
                href -> assertThat(href).endsWith("MobileTestPage$Member?id=7"));
        assertThat(tester.getLastResponseAsString()).contains("<ul>", "<li wicket:id=\"items\">");
    }

    @Test
    void marksTheCurrentPagesTab() {
        tester.startPage(MobileTestPage.Today.class);

        assertThat(links()).extracting(link -> link.getAttribute("aria-current")).containsExactly("page", null, null);
    }

    @Test
    void aParameterizedTabIsCurrentOnlyWithItsParameters() {
        tester.startPage(MobileTestPage.Project.class, new PageParameters().add("id", 2));

        assertThat(links()).extracting(link -> link.getAttribute("aria-current")).containsExactly(null, "page");
    }

    @Test
    void aCustomRuleMarksPagesThatBelongToATab() {
        tester.startPage(MobileTestPage.Member.class);

        // Today, Add, Team: Member is no tab's target, but Team's rule includes it
        assertThat(links()).extracting(link -> link.getAttribute("aria-current")).containsExactly(null, null, "page");
    }

    @Test
    void thePrimaryItemIsARoundButtonNamedOnlyForScreenReaders() {
        tester.startPage(MobileTestPage.Today.class);

        TagTester add = links().get(1);
        assertThat(add.getAttribute("class")).isEqualTo("button oat-fab");
        assertThat(add.getAttribute("aria-label")).isEqualTo("Add absence");
        assertThat(add.getValue()).doesNotContain("wicket:id=\"label\"").doesNotContain("Add absence");
        // The other tabs are plain links with a visible label and no aria-label
        assertThat(links().get(0).getAttribute("class")).isNull();
        assertThat(links().get(0).getAttribute("aria-label")).isNull();
    }

    @Test
    void thePrimaryItemNeedsAnIcon() {
        OatBottomNav nav = new OatBottomNav("nav", List.of(BottomNavItem.of("Add", MobileTestPage.Add.class).asPrimary()));

        assertThatThrownBy(() -> tester.startComponentInPage(nav, nav())).hasStackTraceContaining("needs one");
    }

    @Test
    void aBadgeShowsOnlyACountAboveZero() {
        tester.startComponentInPage(new OatBottomNav("nav", List.of(
                tab("Inbox", MobileTestPage.Today.class).withBadge(() -> 3),
                tab("Team", MobileTestPage.Team.class).withBadge(() -> null),
                tab("Member", MobileTestPage.Member.class).withBadge(Model.of(0)),
                tab("Add", MobileTestPage.Add.class))), nav());

        assertThat(tagsWithClass("badge")).extracting(TagTester::getValue).containsExactly("3");
    }

    @Test
    void drawsSpriteSymbolsInlineSvgsAndOatIcons() {
        String svg = "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\"><path d=\"M5 12h14\"/></svg>";
        tester.startComponentInPage(new OatBottomNav("nav", List.of(
                BottomNavItem.of("Today", MobileTestPage.Today.class).withIcon(Icon.sprite("calendar")),
                BottomNavItem.of("Team", MobileTestPage.Team.class).withIcon(Icon.svg(svg)),
                BottomNavItem.of("Member", MobileTestPage.Member.class).withIcon(Icon.named("user")),
                BottomNavItem.of("Add", MobileTestPage.Add.class))), nav());

        List<TagTester> icons = tagsWithClass("oat-bottom-nav-icon");
        assertThat(icons).hasSize(3).allMatch(icon -> "true".equals(icon.getAttribute("aria-hidden")));
        assertThat(icons.get(0).getValue()).isEqualTo("<svg aria-hidden=\"true\" focusable=\"false\"><use href=\"#calendar\"/></svg>");
        // The app's own markup, not escaped
        assertThat(icons.get(1).getValue()).isEqualTo(svg);
        assertThat(icons.get(2).getValue()).startsWith("<svg class=\"oat-icon\"").contains("stroke=\"currentColor\"");
    }

    @Test
    void aSpriteIdIsEscaped() {
        tester.startComponentInPage(new OatBottomNav("nav", List.of(
                BottomNavItem.of("Today", MobileTestPage.Today.class).withIcon(Icon.sprite("a\"><script>")))), nav());

        assertThat(tester.getLastResponseAsString()).contains("href=\"#a&quot;&gt;&lt;script&gt;\"").doesNotContain("<script>");
    }

    @Test
    void anUnknownOatIconIsRejected() {
        assertThatThrownBy(() -> Icon.named("no-such-icon")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theNavIsNamedMainByDefault() {
        tester.startComponentInPage(new OatBottomNav("nav", List.of(tab("Today", MobileTestPage.Today.class))), nav());
        assertThat(tester.getTagByWicketId("nav").getAttribute("aria-label")).isEqualTo("Main");

        tester.getSession().setLocale(Locale.GERMANY);
        tester.startComponentInPage(new OatBottomNav("nav", List.of(tab("Today", MobileTestPage.Today.class))), nav());
        assertThat(tester.getTagByWicketId("nav").getAttribute("aria-label")).isEqualTo("Hauptmenü");
    }

    @Test
    void theNavsNameCanBeSetInJavaOrInTheMarkup() {
        tester.startComponentInPage(new OatBottomNav("nav", List.of(tab("Today", MobileTestPage.Today.class)))
                .setLabel("Sections"), nav());
        assertThat(tester.getTagByWicketId("nav").getAttribute("aria-label")).isEqualTo("Sections");

        tester.startComponentInPage(new OatBottomNav("nav", List.of(tab("Today", MobileTestPage.Today.class))),
                Markup.of("<nav wicket:id='nav' aria-label='Time off'></nav>"));
        assertThat(tester.getTagByWicketId("nav").getAttribute("aria-label")).isEqualTo("Time off");
    }

    @Test
    void canStayVisibleOnWideScreens() {
        tester.startComponentInPage(new OatBottomNav("nav", List.of(tab("Today", MobileTestPage.Today.class))), nav());
        assertThat(tester.getTagByWicketId("nav").hasAttribute("data-always-visible")).isFalse();

        tester.startComponentInPage(new OatBottomNav("nav", List.of(tab("Today", MobileTestPage.Today.class)))
                .setAlwaysVisible(true), nav());
        assertThat(tester.getTagByWicketId("nav").hasAttribute("data-always-visible")).isTrue();
    }

    @Test
    void tabsForPagesTheUserMayNotOpenAreLeftOut() {
        tester.startPage(MobileTestPage.Today.class);

        assertThat(links()).hasSize(3);
        assertThat(tester.getLastResponseAsString()).doesNotContain("Secret");
    }

    @Test
    void subclassesCanUseAjaxLinks() {
        List<String> clicked = new ArrayList<>();
        OatBottomNav nav = new OatBottomNav("nav", List.of(tab("Today", MobileTestPage.Today.class))) {
            @Override
            protected AbstractLink newLink(String id, BottomNavItem item) {
                return new AjaxLink<Void>(id) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        clicked.add(item.label().getObject());
                    }
                };
            }
        };
        tester.startComponentInPage(nav, nav());
        assertThat(tester.getTagByWicketId("label").getValue()).isEqualTo("Today");

        tester.clickLink("nav:items:0:link");
        assertThat(clicked).containsExactly("Today");
    }

    @Test
    void mobileLayoutPutsTheBarAfterTheContent() {
        tester.startPage(MobileTestPage.Today.class);
        String html = tester.getLastResponseAsString();

        // viewport-fit=cover, so the bar can keep clear of the iPhone's home indicator
        assertThat(html).contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, viewport-fit=cover\"/>");
        assertThat(html).contains("<main class=\"oat-mobile-main\" id=\"oat-main\">");
        assertThat(html.indexOf("<p>Content</p>")).isBetween(html.indexOf("<main"), html.indexOf("</main>"));
        assertThat(html.indexOf("<nav")).isGreaterThan(html.indexOf("</main>"));
        // So an Ajax handler can re-render the bar, e.g. for its badges
        assertThat(tester.getTagByWicketId("bottomNav").getAttribute("id")).isNotBlank();
        assertThat(tester.getTagByWicketId("html").getAttribute("lang")).isNotBlank();
        tester.assertLabel("appTitle", "Wicket Oat Application");
    }

    @Test
    void itemsAreImmutable() {
        PageParameters parameters = new PageParameters().add("id", 1);
        BottomNavItem item = BottomNavItem.of("Project", MobileTestPage.Today.class, parameters);
        parameters.set("id", 2);
        item.parameters().set("id", 3);

        assertThat(item.parameters().get("id").toInt()).isEqualTo(1);
        BottomNavItem primary = item.asPrimary();
        assertThat(primary.primary()).isTrue();
        assertThat(item.primary()).isFalse();
    }
}

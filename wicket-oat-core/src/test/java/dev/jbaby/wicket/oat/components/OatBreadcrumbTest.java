package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OatBreadcrumbTest {

    private static final List<String> FOLDERS = List.of("Home", "Projects", "2026", "Wicket Oat", "Docs", "Breadcrumb");

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.US);
    }

    private static OatBreadcrumb<String> breadcrumb(IModel<List<String>> levels) {
        return OatBreadcrumb.of("breadcrumb", levels, (item, folder) -> item.add(new Link<Void>("link") {
            @Override
            public void onClick() {
            }
        }.add(new Label("label", folder))));
    }

    private void render(OatBreadcrumb<String> breadcrumb) {
        tester.startComponentInPage(breadcrumb, Markup.of("<nav wicket:id='breadcrumb'></nav>"));
    }

    /** The labels of the links on the trail and, separately, in the "…" menu. */
    private List<String> labels(String wicketId) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", wicketId, false).stream()
                .map(item -> item.getChild("wicket:id", "label").getValue())
                .toList();
    }

    @Test
    void allLevelsShowByDefault() {
        render(breadcrumb(Model.ofList(FOLDERS)));

        assertThat(labels("items")).containsExactlyElementsOf(FOLDERS);
        tester.assertInvisible("breadcrumb:list:more");
        tester.assertInvisible("breadcrumb:list:currentItem");
        assertThat(tester.getTagByWicketId("breadcrumb").getAttribute("aria-label")).isEqualTo("Breadcrumb");
    }

    @Test
    void aLongTrailShowsTheFirstLevelAMenuOfTheMiddleOnesAndTheLastOnes() {
        render(breadcrumb(Model.ofList(FOLDERS)).setMaxItems(3));

        assertThat(labels("items")).containsExactly("Home");
        assertThat(labels("hidden")).containsExactly("Projects", "2026", "Wicket Oat");
        assertThat(labels("tail")).containsExactly("Docs", "Breadcrumb");

        TagTester button = tester.getTagByWicketId("button");
        assertThat(button.getAttribute("aria-label")).isEqualTo("Show more levels");
        assertThat(button.getAttribute("popovertarget")).isEqualTo(tester.getTagByWicketId("menu").getAttribute("id"));
        assertThat(TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "menuitem", false))
                .hasSize(3);
        // Only the very last level is the current page, and has no separator after it
        assertThat(TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "aria-current", "page", false))
                .extracting(TagTester::getValue).singleElement().asString().contains("Breadcrumb");
        tester.assertVisible("breadcrumb:list:items:0:separator");
        tester.assertVisible("breadcrumb:list:tail:0:separator");
        tester.assertInvisible("breadcrumb:list:tail:1:separator");
    }

    @Test
    void aTrailOfExactlyMaxItemsShowsThemAll() {
        render(breadcrumb(Model.ofList(FOLDERS)).setMaxItems(6));

        assertThat(labels("items")).containsExactlyElementsOf(FOLDERS);
        tester.assertInvisible("breadcrumb:list:more");
    }

    @Test
    void theCurrentPageAsTextEndsTheTrailAndCounts() {
        render(breadcrumb(Model.ofList(FOLDERS)).setCurrent(Model.of("Collapsing")).setMaxItems(2));

        assertThat(labels("items")).containsExactly("Home");
        assertThat(labels("hidden")).containsExactly("Projects", "2026", "Wicket Oat", "Docs", "Breadcrumb");
        assertThat(labels("tail")).isEmpty();
        TagTester current = tester.getTagByWicketId("current");
        assertThat(current.getValue()).isEqualTo("Collapsing");
        assertThat(current.getAttribute("aria-current")).isEqualTo("page");
        // The levels are links to pages above, none of them the current one
        assertThat(TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "aria-current", "page", false)).hasSize(1);
    }

    @Test
    void theLevelsAreReadOncePerRequest() {
        AtomicInteger reads = new AtomicInteger();
        render(breadcrumb(() -> {
            reads.incrementAndGet();
            return FOLDERS;
        }).setMaxItems(3));

        assertThat(reads).hasValue(1);
    }

    @Test
    void noLevelsShowNothing() {
        render(breadcrumb(Model.ofList(null)).setMaxItems(2));

        assertThat(labels("items")).isEmpty();
        tester.assertInvisible("breadcrumb:list:more");
    }

    @Test
    void aTrailShowsAtLeastItsFirstAndLastLevel() {
        assertThatIllegalArgumentException().isThrownBy(() -> breadcrumb(Model.ofList(FOLDERS)).setMaxItems(1));
    }

    @Test
    void theTextsAreTranslated() {
        tester.getSession().setLocale(Locale.GERMAN);
        render(breadcrumb(Model.ofList(FOLDERS)).setMaxItems(3));

        assertThat(tester.getTagByWicketId("breadcrumb").getAttribute("aria-label")).isEqualTo("Brotkrümelnavigation");
        assertThat(tester.getTagByWicketId("button").getAttribute("aria-label")).isEqualTo("Weitere Ebenen anzeigen");
    }

    @Test
    void aPageHeadersBreadcrumbCollapsesToo() {
        OatPageHeader header = new OatPageHeader("header", "Invoice INV-1")
                .setBreadcrumb(List.of(
                        MenuItem.of("Home", LayoutAndNavigationTestPages.Home.class),
                        MenuItem.of("Sales", LayoutAndNavigationTestPages.Home.class),
                        MenuItem.of("Customers", LayoutAndNavigationTestPages.Home.class),
                        MenuItem.of("ACME", LayoutAndNavigationTestPages.Home.class)))
                .setBreadcrumbMaxItems(3);
        tester.startComponentInPage(header, Markup.of("<header wicket:id='header'></header>"));

        assertThat(labels("items")).containsExactly("Home");
        assertThat(labels("hidden")).containsExactly("Sales", "Customers");
        assertThat(labels("tail")).containsExactly("ACME");
        assertThat(tester.getTagByWicketId("current").getValue()).isEqualTo("Invoice INV-1");
        assertThat(tester.getTagByWicketId("breadcrumb").getAttribute("aria-label")).isEqualTo("Breadcrumb");
    }
}

package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MobilePagesTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
        tester.getSession().setLocale(Locale.US);
    }

    private List<TagTester> tabs() {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "link", false);
    }

    private String teamBadge() {
        TagTester badge = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "class", "badge", false).stream()
                .filter(tag -> "badge".equals(tag.getAttribute("wicket:id")))
                .findFirst().orElse(null);
        return badge == null ? null : badge.getValue();
    }

    @Test
    void everyTabRendersWithTheBar() {
        for (Class<? extends MobileBasePage> page : List.of(MobileTodayPage.class, MobileCapacityPage.class,
                MobileTeamPage.class, MobileAddAbsencePage.class)) {
            tester.startPage(page);
            tester.assertRenderedPage(page);
            assertEquals(4, tabs().size(), page.getSimpleName());
            // Shown on desktops too, under the phone-sized column
            assertTrue(tester.getTagByWicketId("bottomNav").hasAttribute("data-always-visible"));
        }

        tester.startPage(MobileCapacityPage.class);
        List<TagTester> tabs = tabs();
        assertEquals("page", tabs.get(1).getAttribute("aria-current"));
        assertEquals("Add absence", tabs.get(3).getAttribute("aria-label"));
        assertTrue(tabs.get(3).getAttribute("class").contains("oat-fab"));
        assertTrue(tester.getLastResponseAsString().contains("<use href=\"#tab-capacity\"/>"));
    }

    @Test
    void addingATentativeAbsenceRaisesTheTeamBadge() {
        tester.startPage(MobileTodayPage.class);
        assertEquals("2", teamBadge());

        tester.startPage(MobileAddAbsencePage.class);
        LocalDate start = DemoAbsences.today().plusDays(20);
        FormTester form = tester.newFormTester("form");
        form.setValue("start", start.toString());
        form.setValue("end", start.plusDays(2).toString());
        form.select("type", 1);
        form.setValue("tentative", true);
        form.submit();

        tester.assertRenderedPage(MobileTodayPage.class);
        assertTrue(tester.getLastResponseAsString().contains("Added your away as tentative."));
        assertEquals("3", teamBadge());
    }

    @Test
    void theLastDayCantBeBeforeTheFirst() {
        tester.startPage(MobileAddAbsencePage.class);
        LocalDate start = DemoAbsences.today().plusDays(5);
        FormTester form = tester.newFormTester("form");
        form.setValue("start", start.toString());
        form.setValue("end", start.minusDays(1).toString());
        form.submit();

        tester.assertRenderedPage(MobileAddAbsencePage.class);
        tester.assertErrorMessages("The last day can't be before the first.");
    }

    @Test
    void confirmingAnAbsenceUpdatesTheBadgeOverAjax() {
        tester.startPage(MobileTeamPage.class);
        assertEquals("2", teamBadge());

        tester.clickLink("team:tentative:0:confirm");

        tester.assertComponentOnAjaxResponse("bottomNav");
        assertTrue(tester.getLastResponseAsString().contains("<span wicket:id=\"badge\" class=\"badge\">1</span>"));
    }
}

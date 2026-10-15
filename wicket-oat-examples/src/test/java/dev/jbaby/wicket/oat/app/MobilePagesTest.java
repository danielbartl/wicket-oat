package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(MobilePagesTest.ShortlyAfterMidnight.class)
class MobilePagesTest {

    /** 00:30 on Monday, 2 March in Vienna, while it is still Sunday in UTC. */
    @TestConfiguration(proxyBeanMethods = false)
    static class ShortlyAfterMidnight {
        @Bean
        @Primary
        Clock viennaShortlyAfterMidnight() {
            return Clock.fixed(Instant.parse("2026-03-01T23:30:00Z"), ZoneId.of("Europe/Vienna"));
        }
    }

    @Autowired
    private WebApplication wicketApp;

    @Autowired
    private Clock clock;

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
    void todayIsTheDayOfTheAppsClock() {
        tester.startPage(MobileTodayPage.class);

        tester.assertLabel("date", "Monday, 2 March");
    }

    @Test
    void someoneWithOverlappingAbsencesIsCountedOnce() {
        tester.startPage(MobileTodayPage.class);
        tester.assertLabel("inCount", "4"); // all but Mira and Jonas
        LocalDate today = LocalDate.now(clock);
        // Mira, on vacation, is also away today
        DemoAbsences.add(new DemoAbsences.Absence("Mira Novak", DemoAbsences.Type.AWAY, today, today, false, false, false));

        tester.startPage(MobileTodayPage.class);
        tester.assertLabel("inCount", "4");
        tester.startPage(MobileCapacityPage.class);
        tester.assertLabel("days:0:count", "4 of 6 in");
    }

    @Test
    void addingATentativeAbsenceRaisesTheTeamBadge() {
        tester.startPage(MobileTodayPage.class);
        assertEquals("2", teamBadge());

        tester.startPage(MobileAddAbsencePage.class);
        LocalDate start = LocalDate.now(clock).plusDays(20);
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
        LocalDate start = LocalDate.now(clock).plusDays(5);
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

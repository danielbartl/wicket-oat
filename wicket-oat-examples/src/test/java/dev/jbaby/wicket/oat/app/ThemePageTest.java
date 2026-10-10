package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
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
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(ThemePageTest.OutOfSeason.class)
class ThemePageTest {

    /** A day without a seasonal theme, so the configured default applies. */
    @TestConfiguration(proxyBeanMethods = false)
    static class OutOfSeason {
        @Bean
        @Primary
        Clock juneClock() {
            return Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    private String htmlTheme() {
        return org.apache.wicket.util.tester.TagTester
                .createTagByName(tester.getLastResponseAsString(), "html").getAttribute("data-theme");
    }

    @Test
    void testThemePageRenders() {
        tester.startPage(ThemePage.class);
        tester.assertRenderedPage(ThemePage.class);

        assertEquals("business", htmlTheme());
        tester.assertLabel("currentTheme", "Business (business)");
        tester.assertLabel("topNav:topNavExtra:link:theme", "💼 Business");
        assertEquals("light", tester.getTagByWicketId("pinnedCard").getAttribute("data-theme"));
        assertEquals("compact", org.apache.wicket.util.tester.TagTester
                .createTagByName(tester.getLastResponseAsString(), "html").getAttribute("data-density"));
        assertEquals("default", tester.getTagByWicketId("defaultTable").getAttribute("data-density"));
        assertEquals("compact", tester.getTagByWicketId("compactTable").getAttribute("data-density"));
    }

    @Test
    void theHeaderShowsTheChosenTheme() {
        tester.startPage(ThemePage.class);

        tester.clickLink("useMidnight");
        tester.assertLabel("topNav:topNavExtra:link:theme", "🌌 Midnight");
    }

    @Test
    void theBusinessThemeCanBeChosen() {
        tester.startPage(ThemePage.class);

        tester.clickLink("useBusiness");
        assertEquals("business", htmlTheme());
        tester.assertLabel("currentTheme", "Business (business)");
    }

    @Test
    void customThemeCanBeChosenAndReset() {
        tester.startPage(ThemePage.class);

        tester.clickLink("useOcean");
        assertEquals("ocean", htmlTheme());
        tester.assertLabel("currentTheme", "Ocean (ocean)");
        // The pinned card keeps its theme
        assertEquals("light", tester.getTagByWicketId("pinnedCard").getAttribute("data-theme"));

        tester.clickLink("useDefault");
        assertEquals("business", htmlTheme());
    }
}

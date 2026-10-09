package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ThemePageTest {

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

        assertEquals("dark", htmlTheme());
        tester.assertLabel("currentTheme", "Dark (dark)");
        assertEquals("light", tester.getTagByWicketId("pinnedCard").getAttribute("data-theme"));
        assertEquals("compact", tester.getTagByWicketId("compactTable").getAttribute("data-density"));
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
        assertEquals("dark", htmlTheme());
    }
}

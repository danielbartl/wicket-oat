package dev.jbaby.wicket.oat;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WicketOatsTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new MockApplication() {
            @Override
            protected void init() {
                super.init();
                // MockApplication disables CSP for tests; restore WebApplication's real default
                getCspSettings().blocking().strict();
                WicketOats.install(this);
                // Render in one pass so the CSP header is on the page response, not a redirect
                getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.ONE_PASS_RENDER);
            }
        });
    }

    @Test
    void installKeepsWicketsStrictCspAndOnlyAddsDataImages() {
        tester.startPage(LayoutTestPage.class);
        String csp = tester.getLastResponse().getHeader("Content-Security-Policy");

        assertThat(csp)
                .contains("script-src 'strict-dynamic' 'nonce-")
                .contains("style-src 'nonce-")
                .contains("img-src 'self' data:")
                .doesNotContain("unsafe-inline")
                .doesNotContain("unsafe-eval");
    }

    @Test
    void layoutBreadcrumbAndThemeSwitcherRenderNoInlineStyles() {
        tester.startPage(LayoutTestPage.class);
        String html = tester.getLastResponseAsString();

        assertThat(html).contains("oat-app-main").contains("oat-breadcrumb").contains("title=\"Midnight\"");
        // Inline style attributes are blocked by the strict CSP's nonce-only style-src
        assertThat(html).doesNotContain("style=");
    }

    @Test
    void installRegistersTheLibraryStylesheet() {
        tester.startPage(LayoutTestPage.class);

        assertThat(tester.getLastResponseAsString()).contains("wicket-oat.css");
    }

    @Test
    void oatsResourcesCanBeReplacedForExampleWithACdn() {
        tester.getApplication().addResourceReplacement(WicketOats.OAT_JS,
                new UrlResourceReference(Url.parse("https://cdn.example.com/oat.min.js")));
        tester.startPage(LayoutTestPage.class);

        assertThat(tester.getLastResponseAsString())
                .contains("https://cdn.example.com/oat.min.js")
                .doesNotContainPattern("OatAppLayout/oat\\.min[^\"]*\\.js")   // the packaged script is gone
                .containsPattern("OatAppLayout/oat\\.min[^\"]*\\.css");      // the packaged CSS stays
    }

    @Test
    void addingOatsResourcesToEveryPageCanBeTurnedOff() {
        OatSettings.get(tester.getApplication()).setAddResources(false);
        tester.startPage(LayoutTestPage.class);

        assertThat(tester.getLastResponseAsString()).doesNotContain("oat.min.css").doesNotContain("oat.min.js");
    }
}

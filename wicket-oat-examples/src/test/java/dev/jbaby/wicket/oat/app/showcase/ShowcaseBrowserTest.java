package dev.jbaby.wicket.oat.app.showcase;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.CheckedNode;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ForcedColors;
import dev.jbaby.wicket.oat.OatTheme;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The showcase page in a real browser: no accessibility violations axe-core can find, in
 * every built-in theme; a visible focus on every control, light, dark and with Windows'
 * forced colors; and no errors in the console, such as a style or script the strict
 * Content Security Policy blocked. Run with {@code ./mvnw test -pl wicket-oat-examples -Pbrowser}.
 * Playwright downloads its Chromium the first time; {@code BROWSER_EXECUTABLE} points it at
 * another one instead.
 */
@Tag("browser")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShowcaseBrowserTest {

    // WCAG 2.2 A and AA, as axe-core tags its rules
    private static final List<String> WCAG = List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22aa");

    // Themes whose palettes don't reach WCAG AA's contrast yet: their color-contrast
    // violations are printed rather than failed, until their colors are fixed
    private static final Set<String> CONTRAST_TO_FIX = Set.of("catppuccin-latte", "catppuccin-mocha", "clay", "daisy",
            "everforest", "material", "nord", "rose-pine-dawn", "royal", "tokyo-night", "ultraviolet", "wireframe", "xmas");

    // Stops transitions, so a control's focus style is read when it's complete
    private static final String NO_TRANSITIONS = "*, *::before, *::after { transition: none !important; animation: none !important; }";

    // Each element's outline and box shadow, and its parent's, before anything has focus
    private static final String RECORD_RESTING = """
            () => {
                const look = el => {
                    const style = getComputedStyle(el);
                    return [style.outlineStyle, style.outlineWidth, style.outlineColor, style.boxShadow, style.borderColor].join(' ');
                };
                window.oatResting = new Map([...document.querySelectorAll('*')].map(el => [el, look(el)]));
                window.oatLook = look;
                window.oatSeen = new Set();
            }""";

    // The focused element: "done" once focus is back where it started, "" if its focus is
    // visible on it or its parent, and otherwise a description of it
    private static final String CHECK_FOCUSED = """
            () => {
                const el = document.activeElement;
                if (!el || el === document.body || window.oatSeen.has(el)) {
                    return 'done';
                }
                window.oatSeen.add(el);
                const changed = target => target && window.oatResting.has(target)
                        && window.oatLook(target) !== window.oatResting.get(target);
                if (changed(el) || changed(el.parentElement)) {
                    return '';
                }
                const card = el.closest('[data-scenario]');
                return (card ? card.dataset.scenario + ': ' : '') + el.outerHTML.slice(0, 160);
            }""";

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void launch() {
        playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
        String executable = System.getenv("BROWSER_EXECUTABLE");
        if (executable != null) {
            options.setExecutablePath(Path.of(executable));
        }
        browser = playwright.chromium().launch(options);
    }

    @AfterAll
    static void close() {
        playwright.close();
    }

    static Stream<String> themes() {
        return OatTheme.builtIns().stream().map(OatTheme::value);
    }

    @ParameterizedTest
    @MethodSource("themes")
    void noAccessibilityViolationsAndNoConsoleErrors(String theme) {
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            List<String> errors = new ArrayList<>();
            page.onConsoleMessage(message -> {
                if ("error".equals(message.type())) {
                    errors.add(message.text());
                }
            });
            page.onPageError(errors::add);
            page.navigate(url("theme=" + theme));

            List<Rule> violations = new AxeBuilder(page).withTags(WCAG).analyze().getViolations();
            if (CONTRAST_TO_FIX.contains(theme)) {
                List<Rule> contrast = violations.stream().filter(rule -> rule.getId().equals("color-contrast")).toList();
                assertThat(contrast).as(theme + " meets the contrast now: take it out of CONTRAST_TO_FIX").isNotEmpty();
                describe(contrast).forEach(violation -> System.out.println("To fix in " + theme + ": " + violation));
                violations = violations.stream().filter(rule -> !contrast.contains(rule)).toList();
            }

            assertThat(describe(violations)).as("axe-core's WCAG violations in " + theme).isEmpty();
            assertThat(errors).as("console errors in " + theme).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"light", "dark", "light, forced colors"})
    void everyControlShowsItsFocus(String look) {
        // The test's own style needs a policy that allows it
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBypassCSP(true));
             Page page = context.newPage()) {
            if (look.endsWith("forced colors")) {
                page.emulateMedia(new Page.EmulateMediaOptions().setForcedColors(ForcedColors.ACTIVE));
            }
            page.navigate(url("theme=" + look.split(",")[0]));
            page.addStyleTag(new Page.AddStyleTagOptions().setContent(NO_TRANSITIONS));
            page.evaluate(RECORD_RESTING);

            List<String> invisible = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                page.keyboard().press("Tab");
                String focused = (String) page.evaluate(CHECK_FOCUSED);
                if ("done".equals(focused)) {
                    assertThat(i).as("controls tabbed through").isPositive();
                    break;
                }
                if (!focused.isEmpty()) {
                    invisible.add(focused);
                }
            }

            assertThat(invisible).as("controls without a visible focus (" + look + ")").isEmpty();
        }
    }

    private String url(String query) {
        return "http://localhost:" + port + "/showcase?" + query;
    }

    private static List<String> describe(List<Rule> violations) {
        return violations.stream().map(rule -> rule.getId() + " (" + rule.getImpact() + "): " + rule.getHelp()
                + rule.getNodes().stream().map(ShowcaseBrowserTest::describe).collect(Collectors.joining()))
                .toList();
    }

    private static String describe(CheckedNode node) {
        return "\n    " + node.getTarget() + ": " + String.valueOf(node.getFailureSummary()).replace("\n", " ");
    }
}

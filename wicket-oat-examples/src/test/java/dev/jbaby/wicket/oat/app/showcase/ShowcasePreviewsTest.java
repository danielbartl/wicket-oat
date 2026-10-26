package dev.jbaby.wicket.oat.app.showcase;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the showcase page as the website shows its previews: in English and without
 * Wicket's own tags and attributes. It checks that every scenario renders, and writes their
 * markup to {@code target/showcase/previews.json}. With
 * {@code -Dshowcase.site=../wicket-oat-site/index.html} it also puts each preview into the
 * website's card with the same {@code data-preview}, so the site shows what the library
 * renders rather than a copy of it.
 */
@SpringBootTest
class ShowcasePreviewsTest {

    private static final Pattern RELATIVE_LINK = Pattern.compile("href=\"(\\./|\\.\\./|\\?)[^\"]*\"");
    private static final Pattern CARD = Pattern.compile("<div[^>]*\\sdata-preview=\"([a-z0-9-]+)\"[^>]*>");
    private static final Pattern ID = Pattern.compile("\\sid=\"([^\"]+)\"");
    private static final Pattern DIV = Pattern.compile("<div[\\s>]|</div>");

    @Autowired
    private WebApplication wicketApp;

    @Test
    void everyScenarioRendersAPreview() throws IOException {
        Map<String, String> previews = render();

        assertThat(previews).containsOnlyKeys(Scenarios.ALL.stream()
                .flatMap(group -> group.scenarios().stream()).map(Scenario::id).toList());
        previews.forEach((id, preview) -> assertThat(preview).as(id).isNotBlank().doesNotContain("wicket:"));

        Path json = Path.of("target/showcase/previews.json");
        Files.createDirectories(json.getParent());
        Files.writeString(json, json(previews));

        String site = System.getProperty("showcase.site");
        if (site != null) {
            Path index = Path.of(site);
            String filled = fillCards(Files.readString(index), previews);
            // Previews bring markup ids; one taken twice breaks labels and menus
            List<String> ids = ID.matcher(filled).results().map(id -> id.group(1)).toList();
            assertThat(ids).as("ids on the website's page").doesNotHaveDuplicates();
            Files.writeString(index, filled);
        }
    }

    @Test
    void fillsTheWebsitesCardsAndTheirLayout() {
        String site = """
                <div class="showcase-card"><div class="showcase-preview" style="display:block" data-preview="alert">
                    <div role="alert">A copy</div>
                </div><div class="showcase-footer">OatAlert</div></div>""";

        assertThat(fillCards(site, Map.of("alert", "<div role=\"alert\">Rendered</div>"))).isEqualTo("""
                <div class="showcase-card"><div class="showcase-preview block" data-preview="alert">
                <div role="alert">Rendered</div>
                </div><div class="showcase-footer">OatAlert</div></div>""");
    }

    private Map<String, String> render() {
        // The tester initializes the application, which opens its settings
        WicketTester tester = new WicketTester(wicketApp);
        boolean strip = wicketApp.getMarkupSettings().getStripWicketTags();
        wicketApp.getMarkupSettings().setStripWicketTags(true);
        try {
            tester.getSession().setLocale(Locale.ENGLISH);
            tester.startPage(ShowcasePage.class, new PageParameters());
            String html = tester.getLastResponseAsString();

            Map<String, String> previews = new LinkedHashMap<>();
            for (Scenarios.Group group : Scenarios.ALL) {
                for (Scenario scenario : group.scenarios()) {
                    TagTester preview = TagTester.createTagByAttribute(html, "data-preview", scenario.id());
                    assertThat(preview).as(scenario.id()).isNotNull();
                    // Links into the examples app lead nowhere on the website
                    previews.put(scenario.id(), RELATIVE_LINK.matcher(preview.getValue().strip()).replaceAll("href=\"#\""));
                }
            }
            return previews;
        } finally {
            wicketApp.getMarkupSettings().setStripWicketTags(strip);
        }
    }

    // The website's page with each card's preview and layout replaced; a card without a
    // scenario fails
    static String fillCards(String site, Map<String, String> previews) {
        StringBuilder filled = new StringBuilder();
        Matcher card = CARD.matcher(site);
        int copied = 0;
        while (card.find(copied)) {
            String id = card.group(1);
            Scenario scenario = Scenarios.find(id);
            assertThat(scenario).as("the website's card " + id + " has a scenario").isNotNull();
            int end = closingDiv(site, card.end());
            filled.append(site, copied, card.start())
                    .append("<div class=\"showcase-preview").append(scenario.block() ? " block" : "")
                    .append("\" data-preview=\"").append(id).append("\">\n")
                    .append(previews.get(id)).append('\n');
            copied = end;
        }
        return filled.append(site.substring(copied)).toString();
    }

    // Where the </div> closing the div whose content starts at the given index starts
    private static int closingDiv(String html, int from) {
        Matcher tag = DIV.matcher(html);
        int depth = 1;
        while (tag.find(from)) {
            depth += tag.group().startsWith("</") ? -1 : 1;
            if (depth == 0) {
                return tag.start();
            }
            from = tag.end();
        }
        throw new IllegalArgumentException("A preview's <div> isn't closed");
    }

    private static String json(Map<String, String> previews) {
        return previews.entrySet().stream()
                .map(preview -> "  " + quote(preview.getKey()) + ": " + quote(preview.getValue()))
                .collect(Collectors.joining(",\n", "{\n", "\n}\n"));
    }

    private static String quote(String text) {
        StringBuilder quoted = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> quoted.append(c < 0x20 ? String.format("\\u%04x", (int) c) : String.valueOf(c));
            }
        }
        return quoted.append('"').toString();
    }
}

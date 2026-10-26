package dev.jbaby.wicket.oat.components;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** The parts of Wicket Oat's CSS that depend on the vendored oat.min.css. */
class OatCssTest {

    private static final Pattern DECLARATION = Pattern.compile("(--[a-z0-9-]+)\\s*:\\s*([^;}]+)");

    /** The tokens a theme sets: the palette, the corners and the shadows. */
    private static final Pattern THEME_TOKEN = Pattern.compile(
            "--(background|foreground|card|primary|secondary|muted|faint|accent|danger|success|warning|border|input|ring)(-foreground)?"
                    + "|--radius-(small|medium|large)|--shadow-[a-z]+");

    @Test
    void pinnedLightAndDarkThemesRedeclareOatsDefaults() throws IOException {
        String oat = read("oat.min.css");
        int background = oat.indexOf("--background:");
        Map<String, String> defaults = declarations(oat.substring(oat.lastIndexOf(":root{", background), oat.indexOf('}', background)));

        String themes = read("themes.css");
        int pinned = themes.indexOf("[data-theme=\"light\"],\n[data-theme=\"dark\"] {");
        Map<String, String> redeclared = declarations(themes.substring(pinned, themes.indexOf('}', pinned)));

        Map<String, String> expected = new LinkedHashMap<>();
        defaults.forEach((token, value) -> {
            if (THEME_TOKEN.matcher(token).matches()) {
                expected.put(token, value);
            }
        });
        assertThat(expected).as("Oat's theme tokens").containsKeys("--primary", "--radius-medium", "--shadow-large");
        assertThat(redeclared).as("themes.css, [data-theme=\"light\"], [data-theme=\"dark\"]").isEqualTo(expected);
    }

    @Test
    void everyDarkThemePrintsInOatsLightPalette() throws IOException {
        String themes = read("themes.css");
        int print = themes.indexOf("@media print {");
        assertThat(print).as("themes.css's print rule").isPositive();
        String rule = themes.substring(print, themes.indexOf('}', print));

        Matcher theme = Pattern.compile("\\n(\\[data-theme=\"[a-z-]+\"\\]) \\{\\s*color-scheme: dark;").matcher(themes);
        int dark = 0;
        while (theme.find()) {
            assertThat(rule).as("the print rule").contains(theme.group(1));
            dark++;
        }
        assertThat(dark).as("dark themes").isGreaterThan(10);

        int pinned = themes.indexOf("[data-theme=\"light\"],\n[data-theme=\"dark\"] {");
        Map<String, String> light = new LinkedHashMap<>();
        declarations(themes.substring(pinned, themes.indexOf('}', pinned))).forEach((token, value) -> {
            if (!token.startsWith("--radius") && !token.startsWith("--shadow")) {
                light.put(token, value.replaceFirst("^light-dark\\(([^,]+),.*\\)$", "$1"));
            }
        });
        assertThat(declarations(rule)).as("the print rule's palette").isEqualTo(light);
    }

    @Test
    void wicketOatCssDeclaresOatsLayerOrderBeforeUsingALayer() throws IOException {
        Matcher oatOrder = Pattern.compile("@layer ([a-z, ]+);").matcher(read("oat.min.css"));
        assertThat(oatOrder.find()).isTrue();

        Matcher firstLayer = Pattern.compile("@layer ([a-z, ]+)[;{]").matcher(read("wicket-oat.css"));
        assertThat(firstLayer.find()).isTrue();
        assertThat(firstLayer.group(0)).endsWith(";");
        assertThat(firstLayer.group(1).replace(" ", "")).isEqualTo(oatOrder.group(1).replace(" ", ""));
    }

    /**
     * wicket-oat.css opens dropdowns without Oat 1.0's zoom, because Oat places a menu
     * while it is zoomed out (https://github.com/knadh/oat/issues/189). Updating Oat fails
     * this test as a reminder: if the issue is fixed in the new version, drop that block
     * from wicket-oat.css; either way, update the version here.
     */
    @Test
    void dropdownZoomWorkaroundWasCheckedAgainstTheVendoredOat() throws IOException {
        assertThat(read("oat.min.css")).startsWith("/* Oat UI v1.0.0 ");
    }

    private static Map<String, String> declarations(String block) {
        Map<String, String> declarations = new LinkedHashMap<>();
        Matcher matcher = DECLARATION.matcher(block);
        while (matcher.find()) {
            declarations.put(matcher.group(1), matcher.group(2).trim().replaceAll("\\s+", " "));
        }
        return declarations;
    }

    private static String read(String name) throws IOException {
        try (InputStream in = OatAppLayout.class.getResourceAsStream(name)) {
            assertThat(in).as(name).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

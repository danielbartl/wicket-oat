package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.components.OatDialog;
import dev.jbaby.wicket.oat.components.form.OatMoneyField;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationsTest {

    /** The languages every library string is translated into. */
    private static final List<String> LANGUAGES = List.of("de", "es", "fr", "it", "ja", "nl", "pt");

    /** Placeholders apps are meant to override, so they stay untranslated. */
    private static final Set<String> UNTRANSLATED = Set.of("OatAppLayout.title", "OatAppLayout.name");

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void dialogButtonsFollowTheLocale() {
        tester.getSession().setLocale(Locale.GERMANY);
        tester.startComponentInPage(new OatDialog("dialog", "Edit"));

        assertThat(tester.getTagByWicketId("cancelLabel").getValue()).isEqualTo("Abbrechen");
        assertThat(tester.getTagByWicketId("confirmLabel").getValue()).isEqualTo("Bestätigen");
    }

    @Test
    void aRegionalLocaleFallsBackToItsLanguage() {
        tester.getSession().setLocale(Locale.forLanguageTag("de-AT"));
        tester.startComponentInPage(new OatDialog("dialog", "Edit"));

        assertThat(tester.getTagByWicketId("cancelLabel").getValue()).isEqualTo("Abbrechen");
    }

    @Test
    void anUntranslatedLanguageFallsBackToEnglish() {
        tester.getSession().setLocale(Locale.forLanguageTag("fi"));
        tester.startComponentInPage(new OatDialog("dialog", "Edit"));

        assertThat(tester.getTagByWicketId("cancelLabel").getValue()).isEqualTo("Cancel");
    }

    @Test
    void appsCanOverrideATextInAPageOrTheirApplicationProperties() {
        WicketTester app = new WicketTester(new OverrideApp());
        app.getSession().setLocale(Locale.GERMANY);
        app.startPage(OverridePage.class);

        assertThat(app.getTagByWicketId("cancelLabel").getValue()).isEqualTo("Verwerfen (Page)");
        assertThat(app.getTagByWicketId("confirmLabel").getValue()).isEqualTo("Speichern (App)");
        app.destroy();
    }

    @Test
    void conversionErrorsAreTranslated() {
        assertThat(moneyError(Locale.GERMANY)).isEqualTo("Der Wert von &#039;Price&#039; ist kein gültiger Betrag.");
        // Japanese checks the files are read as UTF-8
        assertThat(moneyError(Locale.JAPAN)).isEqualTo("&#039;Price&#039; は有効な金額ではありません。");
    }

    private String moneyError(Locale locale) {
        tester.getSession().setLocale(locale);
        Form<Void> form = new Form<>("form");
        form.add(new OatMoneyField("price", "Price", Model.of(BigDecimal.ONE)));
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='price'></div></form>"));
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("price:container:field", "abc");
        formTester.submit();
        return tester.getTagByWicketId("feedback").getValue();
    }

    @ParameterizedTest
    @MethodSource("languages")
    void everyStringIsTranslated(String language) throws IOException {
        // The sources, so a deleted file isn't hidden by a stale copy in target/classes
        Path root = Path.of("src/main/resources/dev/jbaby/wicket/oat");
        List<Path> bundles;
        try (Stream<Path> files = Files.walk(root)) {
            // Icons are drawing data, not texts
            bundles = files.filter(f -> f.getFileName().toString().matches("[A-Za-z]+\\.properties"))
                    .filter(f -> !f.getParent().endsWith("icons")).toList();
        }
        assertThat(bundles).isNotEmpty();

        for (Path bundle : bundles) {
            String name = bundle.getFileName().toString().replace(".properties", "");
            Path translation = bundle.resolveSibling(name + "_" + language + ".utf8.properties");
            assertThat(translation).as("translation of %s", bundle.getFileName()).exists();

            Properties english = load(bundle, StandardCharsets.ISO_8859_1);
            Properties translated = load(translation, StandardCharsets.UTF_8);
            for (String key : english.stringPropertyNames()) {
                if (!UNTRANSLATED.contains(key)) {
                    assertThat(translated.getProperty(key)).as("%s in %s", key, translation.getFileName()).isNotBlank();
                }
            }
            assertThat(english.stringPropertyNames()).as("keys of %s", translation.getFileName())
                    .containsAll(translated.stringPropertyNames());
        }
    }

    static List<String> languages() {
        return LANGUAGES;
    }

    private static Properties load(Path file, Charset charset) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(file); Reader reader = new InputStreamReader(in, charset)) {
            properties.load(reader);
        }
        return properties;
    }
}

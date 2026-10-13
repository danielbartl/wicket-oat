package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatDensity;
import dev.jbaby.wicket.oat.OatSettings;
import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class OatHtmlTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    /** A base page of an app's own, not an Oat layout. */
    public static class OwnBasePage extends WebPage implements IMarkupResourceStreamProvider {
        public OwnBasePage() {
            add(new OatHtml("html"));
            add(new Label("greeting", "Hello"));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html wicket:id='html'><body><p wicket:id='greeting'></p></body></html>");
        }
    }

    @Test
    void anOwnBasePageGetsTheThemeDensityAndLanguage() {
        OatSettings.get(tester.getApplication()).setDefaultTheme(OatTheme.BUSINESS).setDensity(OatDensity.COMPACT);
        tester.getSession().setLocale(Locale.GERMANY);

        tester.startPage(OwnBasePage.class);

        TagTester html = TagTester.createTagByName(tester.getLastResponseAsString(), "html");
        assertThat(html.getAttribute("data-theme")).isEqualTo("business");
        assertThat(html.getAttribute("data-density")).isEqualTo("compact");
        assertThat(html.getAttribute("lang")).isEqualTo("de-DE");
        // Transparent: the page's components can sit anywhere inside it
        tester.assertLabel("greeting", "Hello");
    }
}

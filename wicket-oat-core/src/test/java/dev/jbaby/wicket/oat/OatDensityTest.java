package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.behaviors.OatDensityBehavior;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class OatDensityTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    /** Renders a container with the density behavior and returns its data-density. */
    private String renderedDensity(OatDensityBehavior behavior) {
        WebMarkupContainer container = new WebMarkupContainer("html");
        container.add(behavior);
        tester.startComponentInPage(container, Markup.of("<div wicket:id='html'></div>"));
        return tester.getTagByWicketId("html").getAttribute("data-density");
    }

    @Test
    void theDefaultDensitySetsNoAttribute() {
        assertThat(OatSettings.get(tester.getApplication()).getDensity()).isEqualTo(OatDensity.DEFAULT);
        assertThat(renderedDensity(new OatDensityBehavior())).isNull();
    }

    @Test
    void theConfiguredDensityIsApplied() {
        OatSettings.get(tester.getApplication()).setDensity(OatDensity.COMPACT);
        assertThat(renderedDensity(new OatDensityBehavior())).isEqualTo("compact");
    }

    @Test
    void aComponentCanBePinnedToADensity() {
        assertThat(renderedDensity(new OatDensityBehavior(OatDensity.COMPACT))).isEqualTo("compact");
    }

    @Test
    void theDensityCannotBeNull() {
        assertThatNullPointerException().isThrownBy(() -> OatSettings.get(tester.getApplication()).setDensity(null));
    }

    @Test
    void theAppLayoutAppliesTheDensityToTheHtmlTag() {
        OatSettings.get(tester.getApplication()).setDensity(OatDensity.COMPACT);
        tester.startPage(LayoutTestPage.class);

        TagTester html = TagTester.createTagByName(tester.getLastResponseAsString(), "html");
        assertThat(html.getAttribute("data-density")).isEqualTo("compact");
        assertThat(html.getAttribute("data-theme")).isEqualTo("dark");
    }
}

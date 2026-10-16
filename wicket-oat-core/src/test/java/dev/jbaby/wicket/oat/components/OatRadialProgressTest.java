package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class OatRadialProgressTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.ENGLISH);
    }

    private void start(OatRadialProgress progress) {
        tester.startComponentInPage(progress, Markup.of("<div wicket:id='progress' aria-label='Storage used'></div>"));
    }

    @Test
    void aPercentageFillsThatShareOfTheRing() {
        start(new OatRadialProgress("progress", 72));

        TagTester tag = tester.getTagByWicketId("progress");
        assertThat(tag.getAttribute("class")).isEqualTo("oat-radial-progress");
        assertThat(tag.getAttribute("role")).isEqualTo("progressbar");
        assertThat(tag.getAttribute("aria-label")).isEqualTo("Storage used");
        assertThat(tag.getAttribute("aria-valuemin")).isEqualTo("0");
        assertThat(tag.getAttribute("aria-valuemax")).isEqualTo("100");
        assertThat(tag.getAttribute("aria-valuenow")).isEqualTo("72");
        assertThat(tag.getAttribute("aria-valuetext")).isEqualTo("72%");
        assertThat(tag.getAttribute("id")).isNotBlank();
        assertThat(tester.getTagByWicketId("bar").getAttribute("stroke-dasharray")).isEqualTo("72 100");
        assertThat(tester.getTagByWicketId("text").getValue()).isEqualTo("72%");
    }

    @Test
    void aValueOfAMaximumShowsItsShare() {
        start(new OatRadialProgress("progress", Model.of(3), Model.of(8)));

        TagTester tag = tester.getTagByWicketId("progress");
        assertThat(tag.getAttribute("aria-valuenow")).isEqualTo("3");
        assertThat(tag.getAttribute("aria-valuemax")).isEqualTo("8");
        assertThat(tester.getTagByWicketId("bar").getAttribute("stroke-dasharray")).isEqualTo("37.50 100");
        // Rounded down, so it says 100% only once done
        assertThat(tester.getTagByWicketId("text").getValue()).isEqualTo("37%");
    }

    @Test
    void nothingDoneOrUnknownLeavesTheRingEmpty() {
        start(new OatRadialProgress("progress", 0));
        assertThat(tester.getTagByWicketId("bar")).isNull();
        assertThat(tester.getTagByWicketId("text").getValue()).isEqualTo("0%");

        start(new OatRadialProgress("progress", Model.of()));
        TagTester tag = tester.getTagByWicketId("progress");
        assertThat(tester.getTagByWicketId("bar")).isNull();
        assertThat(tag.getAttribute("aria-valuenow")).isNull();
        assertThat(tag.getAttribute("aria-valuetext")).isNull();
        assertThat(tester.getTagByWicketId("text").getValue()).isEmpty();
    }

    @Test
    void moreThanTheMaximumIsAFullRing() {
        start(new OatRadialProgress("progress", 130));

        assertThat(tester.getTagByWicketId("bar").getAttribute("stroke-dasharray")).isEqualTo("100 100");
        assertThat(tester.getTagByWicketId("text").getValue()).isEqualTo("100%");
    }

    @Test
    void theTextAndColorCanBeChosen() {
        Model<OatVariant> variant = Model.of(OatVariant.DEFAULT);
        OatRadialProgress progress = new OatRadialProgress("progress", Model.of(3), Model.of(5))
                .setText(Model.of("3/5"))
                .setVariant(variant);
        start(progress);

        assertThat(tester.getTagByWicketId("progress").getAttribute("data-variant")).isNull();
        assertThat(tester.getTagByWicketId("progress").getAttribute("aria-valuetext")).isEqualTo("3/5");
        assertThat(tester.getTagByWicketId("text").getValue()).isEqualTo("3/5");

        variant.setObject(OatVariant.DANGER);
        start(progress);
        assertThat(tester.getTagByWicketId("progress").getAttribute("data-variant")).isEqualTo("danger");
    }
}

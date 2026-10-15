package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OatIndicatorTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void aStatusIsADotNamedForScreenReaders() {
        tester.startComponentInPage(new OatStatus("status", OatVariant.SUCCESS, "Online"), Markup.of("<span wicket:id='status'/>"));

        TagTester dot = tester.getTagByWicketId("status");
        assertThat(dot.getAttribute("class")).isEqualTo("oat-status");
        assertThat(dot.getAttribute("data-variant")).isEqualTo("success");
        assertThat(dot.getAttribute("role")).isEqualTo("img");
        assertThat(dot.getAttribute("aria-label")).isEqualTo("Online");
        assertThat(dot.getValue()).isEmpty();
    }

    @Test
    void aStatusWithoutALabelIsLeftToTheTextBesideIt() {
        tester.startComponentInPage(new OatStatus("status", Model.of(OatVariant.DEFAULT), null), Markup.of("<span wicket:id='status'></span>"));

        TagTester dot = tester.getTagByWicketId("status");
        assertThat(dot.getAttribute("data-variant")).isNull();
        assertThat(dot.getAttribute("role")).isNull();
        assertThat(dot.getAttribute("aria-hidden")).isEqualTo("true");
    }

    @Test
    void aStatusIsASpan() {
        assertThatThrownBy(() -> tester.startComponentInPage(new OatStatus("status", OatVariant.SUCCESS, "Online"),
                Markup.of("<div wicket:id='status'></div>"))).hasStackTraceContaining("span");
    }

    @Test
    void aCountIsABadgeOnTheTopEndCornerOfWhatItMarks() {
        Model<Integer> unread = Model.of(3);
        OatIndicator inbox = new OatIndicator("inbox", unread).setVariant(OatVariant.DANGER);
        inbox.add(new Label("link", "Inbox"));
        tester.startComponentInPage(inbox, Markup.of("<span wicket:id='inbox'><a wicket:id='link'></a></span>"));

        TagTester tag = tester.getTagByWicketId("inbox");
        assertThat(tag.getAttribute("class")).isEqualTo("oat-indicator");
        assertThat(tag.getAttribute("id")).isNotBlank();
        TagTester badge = tester.getTagByWicketId("item");
        assertThat(badge.getAttribute("class")).isEqualTo("badge oat-indicator-item");
        // On the badge itself: in development mode Wicket's tags stand between it and the wrapper
        assertThat(badge.getAttribute("data-corner")).isEqualTo("top-end");
        assertThat(badge.getAttribute("data-variant")).isEqualTo("danger");
        assertThat(badge.getValue()).isEqualTo("3");
        // What it marks comes first, so screen readers read "Inbox 3"
        assertThat(tester.getLastResponseAsString().indexOf(">Inbox<")).isLessThan(tester.getLastResponseAsString().indexOf(">3<"));

        unread.setObject(0);
        tester.startComponentInPage(inbox, Markup.of("<span wicket:id='inbox'><a wicket:id='link'></a></span>"));
        assertThat(tester.getTagByWicketId("item")).isNull();
    }

    @Test
    void aStatusSitsOnTheBottomEndCornerAndCornersCanBeChosen() {
        OatIndicator presence = OatIndicator.status("presence", Model.of(OatVariant.SECONDARY), Model.of("Away"));
        presence.add(new OatAvatar("avatar", "AL"));
        tester.startComponentInPage(presence, Markup.of("<span wicket:id='presence'><div wicket:id='avatar'></div></span>"));

        TagTester dot = tester.getTagByWicketId("item");
        assertThat(dot.getAttribute("data-corner")).isEqualTo("bottom-end");
        assertThat(dot.getAttribute("class")).isEqualTo("oat-status oat-indicator-item");
        assertThat(dot.getAttribute("aria-label")).isEqualTo("Away");
        assertThat(presence.getItem()).isInstanceOf(OatStatus.class);

        presence.setCorner(OatIndicator.Corner.TOP_START);
        tester.startComponentInPage(presence, Markup.of("<span wicket:id='presence'><div wicket:id='avatar'></div></span>"));
        assertThat(tester.getTagByWicketId("item").getAttribute("data-corner")).isEqualTo("top-start");
    }

    @Test
    void aBadgeCanBeAWord() {
        OatIndicator indicator = new OatIndicator("feature", Model.of("New"));
        indicator.add(new Label("link", "Reports"));
        tester.startComponentInPage(indicator, Markup.of("<span wicket:id='feature'><a wicket:id='link'></a></span>"));

        assertThat(tester.getTagByWicketId("item").getValue()).isEqualTo("New");
        assertThat(tester.getTagByWicketId("item").getAttribute("data-variant")).isNull();
    }
}

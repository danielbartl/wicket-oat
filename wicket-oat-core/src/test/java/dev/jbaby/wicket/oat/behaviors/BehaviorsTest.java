package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BehaviorsTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void testAlertBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new AlertBehavior(OatVariant.SUCCESS));
        tester.startComponentInPage(container);
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("role")).isEqualTo("alert");
        assertThat(tag.getAttribute("data-variant")).isEqualTo("success");
    }

    @Test
    void testBadgeBehavior() {
        Label label = new Label("id", "New");
        label.add(new BadgeBehavior(OatVariant.DANGER));
        tester.startComponentInPage(label);
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("class")).contains("badge");
        assertThat(tag.getAttribute("data-variant")).isEqualTo("danger");
    }

    @Test
    void testBadgeBehaviorOutline() {
        Label label = new Label("id", "New");
        label.add(new BadgeBehavior(OatVariant.SECONDARY).setOutline(true));
        tester.startComponentInPage(label);
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("class")).contains("outline");
        assertThat(tag.getAttribute("data-variant")).isEqualTo("secondary");
    }

    @Test
    void testButtonBehavior() {
        WebMarkupContainer link = new WebMarkupContainer("id");
        link.add(new ButtonBehavior(OatVariant.SECONDARY));
        tester.startComponentInPage(link, Markup.of("<a wicket:id=\"id\"></a>"));
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("class")).contains("button");
        assertThat(tag.getAttribute("data-variant")).isEqualTo("secondary");
    }

    @Test
    void testButtonBehaviorFactoryVariant() {
        WebMarkupContainer link = new WebMarkupContainer("id");
        link.add(Oat.Behaviors.button(OatVariant.DANGER));
        tester.startComponentInPage(link, Markup.of("<a wicket:id=\"id\"></a>"));
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("data-variant")).isEqualTo("danger");
    }

    @Test
    void testCardBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new CardBehavior());
        tester.startComponentInPage(container);
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("class")).contains("card");
    }

    @Test
    void testTooltipBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new TooltipBehavior("Helpful text"));
        tester.startComponentInPage(container);
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("title")).isEqualTo("Helpful text");
        assertThat(tag.getAttribute("data-tooltip-placement")).isNull();
    }

    @Test
    void testTooltipBehaviorPlacement() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new TooltipBehavior("Helpful text").setPlacement(TooltipBehavior.Placement.BOTTOM));
        tester.startComponentInPage(container);
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("data-tooltip-placement")).isEqualTo("bottom");
    }
}

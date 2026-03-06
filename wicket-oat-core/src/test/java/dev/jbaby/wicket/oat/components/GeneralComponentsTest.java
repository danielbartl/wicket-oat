package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.BadgeBehavior;
import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralComponentsTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void testOatBadge() {
        OatBadge badge = new OatBadge("badge", Model.of("New"), BadgeBehavior.Variant.SUCCESS);
        tester.startComponentInPage(badge);
        TagTester tag = tester.getTagByWicketId("badge");
        assertThat(tag.getAttribute("class")).contains("badge");
        assertThat(tag.getAttribute("class")).contains("success");
        tester.assertLabel("badge", "New");
    }

    @Test
    void testOatAvatar() {
        OatAvatar avatar = new OatAvatar("avatar", Model.of("https://example.com/img.png"), Model.of("JB"), OatAvatar.Size.DEFAULT);
        tester.startComponentInPage(avatar);
        TagTester img = tester.getTagByWicketId("img");
        assertThat(img.getAttribute("src")).isEqualTo("https://example.com/img.png");
    }

    @Test
    void testOatCard() {
        OatCard card = new OatCard("card");
        tester.startComponentInPage(card);
        TagTester tag = tester.getTagByWicketId("card");
        assertThat(tag.getAttribute("class")).contains("card");
    }

    @Test
    void testOatButtonGroup() {
        OatButtonGroup group = new OatButtonGroup("group");
        tester.startComponentInPage(group);
        TagTester tag = tester.getTagByWicketId("group");
        assertThat(tag.getAttribute("class")).contains("buttons");
    }

    @Test
    void testOatProgress() {
        OatProgress progress = new OatProgress("progress", 50);
        tester.startComponentInPage(progress, Markup.of("<progress wicket:id=\"progress\"></progress>"));
        TagTester tag = tester.getTagByWicketId("progress");
        assertThat(tag.getName()).isEqualTo("progress");
        assertThat(tag.getAttribute("value")).isEqualTo("50");
    }

    @Test
    void testOatMeter() {
        OatMeter meter = new OatMeter("meter", 75);
        tester.startComponentInPage(meter, Markup.of("<meter wicket:id=\"meter\"></meter>"));
        TagTester tag = tester.getTagByWicketId("meter");
        assertThat(tag.getName()).isEqualTo("meter");
        assertThat(tag.getAttribute("value")).isEqualTo("75");
    }

    @Test
    void testOatSpinner() {
        OatSpinner spinner = new OatSpinner("spinner");
        tester.startComponentInPage(spinner);
        TagTester tag = tester.getTagByWicketId("spinner");
        assertThat(tag.getAttribute("aria-busy")).isEqualTo("true");
    }

    @Test
    void testOatSkeleton() {
        OatSkeleton skeleton = new OatSkeleton("skeleton", SkeletonBehavior.Shape.BOX);
        tester.startComponentInPage(skeleton);
        TagTester tag = tester.getTagByWicketId("skeleton");
        assertThat(tag.getAttribute("class")).contains("skeleton box");
    }
}

package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.BadgeBehavior;
import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import dev.jbaby.wicket.oat.behaviors.SpinnerBehavior;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        assertThat(tag.getAttribute("data-variant")).isEqualTo("success");
        tester.assertLabel("badge", "New");
    }

    @Test
    void testOatBadgeOutline() {
        OatBadge badge = new OatBadge("badge", Model.of("New"), BadgeBehavior.Variant.SECONDARY);
        badge.setOutline(true);
        tester.startComponentInPage(badge);
        TagTester tag = tester.getTagByWicketId("badge");
        assertThat(tag.getAttribute("class")).contains("outline");
        assertThat(tag.getAttribute("data-variant")).isEqualTo("secondary");
    }

    @Test
    void testOatAvatar() {
        OatAvatar avatar = new OatAvatar("avatar", Model.of("https://example.com/img.png"), Model.of("JB"), OatAvatar.Size.DEFAULT);
        tester.startComponentInPage(avatar);
        TagTester container = tester.getTagByWicketId("container");
        assertThat(container.getName()).isEqualTo("figure");
        assertThat(container.getAttribute("data-variant")).isEqualTo("avatar");
        TagTester img = tester.getTagByWicketId("img");
        assertThat(img.getAttribute("src")).isEqualTo("https://example.com/img.png");
    }

    @Test
    void testOatAvatarInitialsAndSize() {
        OatAvatar avatar = new OatAvatar("avatar", null, Model.of("JB"), OatAvatar.Size.LARGE);
        tester.startComponentInPage(avatar);
        TagTester container = tester.getTagByWicketId("container");
        assertThat(container.getAttribute("class")).contains("large");
        tester.assertInvisible("avatar:container:img");
        tester.assertLabel("avatar:container:initials", "JB");
    }

    @Test
    void testOatAvatarGroup() {
        List<String> data = List.of("JD", "AS");
        OatAvatarGroup<String> group = new OatAvatarGroup<String>("group", Model.ofList(data)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("label", item.getModelObject()));
            }
        };
        tester.startComponentInPage(group, Markup.of(
                "<figure wicket:id=\"group\" data-variant=\"avatar\" role=\"group\"><figure wicket:id=\"avatars\"><span wicket:id=\"label\"></span></figure></figure>"));
        TagTester tag = tester.getTagByWicketId("group");
        assertThat(tag.getAttribute("role")).isEqualTo("group");
        tester.assertLabel("group:avatars:0:label", "JD");
    }

    @Test
    void testOatAvatarGroupFactoryWithSize() {
        List<String> data = List.of("JD");
        OatAvatarGroup<String> group = Oat.Components.avatarGroup("group", Model.ofList(data), OatAvatarGroup.Size.LARGE,
                (item, value) -> item.add(new Label("label", value)));
        tester.startComponentInPage(group, Markup.of(
                "<figure wicket:id=\"group\"><figure wicket:id=\"avatars\"><span wicket:id=\"label\"></span></figure></figure>"));
        TagTester tag = tester.getTagByWicketId("group");
        assertThat(tag.getAttribute("class")).contains("large");
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
        OatButtonGroup<String> group = new OatButtonGroup<String>("group", Model.ofList(List.of("Left", "Right"))) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("button", item.getModelObject()));
            }
        };
        tester.startComponentInPage(group, Markup.of(
                "<menu wicket:id=\"group\"><li wicket:id=\"items\"><button wicket:id=\"button\"></button></li></menu>"));
        TagTester tag = tester.getTagByWicketId("group");
        assertThat(tag.getName()).isEqualTo("menu");
        assertThat(tag.getAttribute("class")).contains("buttons");
        assertThat(tag.getAttribute("role")).isEqualTo("group");
        TagTester item = tester.getTagByWicketId("items");
        assertThat(item.getName()).isEqualTo("li");
        tester.assertLabel("group:items:0:button", "Left");
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
    void testOatProgressFactoryWithMax() {
        OatProgress progress = Oat.Components.progress("progress", Model.of(50), Model.of(200));
        tester.startComponentInPage(progress, Markup.of("<progress wicket:id=\"progress\"></progress>"));
        TagTester tag = tester.getTagByWicketId("progress");
        assertThat(tag.getAttribute("value")).isEqualTo("50");
        assertThat(tag.getAttribute("max")).isEqualTo("200");
    }

    @Test
    void testOatMeterFactoryFull() {
        OatMeter meter = Oat.Components.meter("meter", Model.of(5), Model.of(0), Model.of(10), Model.of(2), Model.of(8), Model.of(6));
        tester.startComponentInPage(meter, Markup.of("<meter wicket:id=\"meter\"></meter>"));
        TagTester tag = tester.getTagByWicketId("meter");
        assertThat(tag.getAttribute("low")).isEqualTo("2");
        assertThat(tag.getAttribute("high")).isEqualTo("8");
        assertThat(tag.getAttribute("optimum")).isEqualTo("6");
    }

    @Test
    void testOatSpinnerFactoryWithModel() {
        OatSpinner spinner = Oat.Components.spinner("spinner", Model.of(SpinnerBehavior.Size.LARGE));
        tester.startComponentInPage(spinner);
        TagTester tag = tester.getTagByWicketId("spinner");
        assertThat(tag.getAttribute("data-spinner")).isEqualTo("large");
    }

    @Test
    void testOatSkeleton() {
        OatSkeleton skeleton = new OatSkeleton("skeleton", SkeletonBehavior.Shape.BOX);
        tester.startComponentInPage(skeleton);
        TagTester tag = tester.getTagByWicketId("skeleton");
        assertThat(tag.getAttribute("class")).contains("skeleton box");
    }
}

package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OatButtonTest {
    private WicketTester tester;
    private boolean clicked = false;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        clicked = false;
    }

    @Test
    void testOatButtonRenders() {
        OatButton button = new OatButton("button", "Click Me") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                clicked = true;
            }
        };
        tester.startComponentInPage(button, Markup.of("<a wicket:id=\"button\"><span wicket:id=\"label\"></span></a>"));
        TagTester tag = tester.getTagByWicketId("button");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttribute("class")).contains("button");
        tester.assertLabel("button:label", "Click Me");
    }

    @Test
    void testOatButtonClick() {
        OatButton button = new OatButton("button", "Click Me") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                clicked = true;
            }
        };
        tester.startComponentInPage(button, Markup.of("<a wicket:id=\"button\"><span wicket:id=\"label\"></span></a>"));
        tester.clickLink("button");
        assertThat(clicked).isTrue();
    }

    @Test
    void testOatButtonFactoryWithModelLabel() {
        OatButton button = Oat.Components.button("button", Model.of("Click Me"), target -> clicked = true);
        tester.startComponentInPage(button, Markup.of("<a wicket:id=\"button\"><span wicket:id=\"label\"></span></a>"));
        tester.assertLabel("button:label", "Click Me");
        tester.clickLink("button");
        assertThat(clicked).isTrue();
    }
}

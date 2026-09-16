package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoreFormTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void testOatTextField() {
        OatTextField field = new OatTextField("id", "Label", Model.of("Value"));
        tester.startComponentInPage(field);
        tester.assertLabel("id:container:label", "Label");
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("value")).isEqualTo("Value");
    }

    @Test
    void testOatCheckBox() {
        OatCheckBox field = new OatCheckBox("id", "Label", Model.of(true));
        tester.startComponentInPage(field);
        tester.assertLabel("id:container:label", "Label");
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("checked")).isEqualTo("checked");
    }

    @Test
    void testOatTextArea() {
        OatTextArea field = new OatTextArea("id", "Label", Model.of("Value"));
        tester.startComponentInPage(field);
        tester.assertLabel("id:container:label", "Label");
        tester.assertModelValue("id:container:field", "Value");
    }

    @Test
    void testOatCheckBoxFactoryWithHelper() {
        OatCheckBox field = Oat.Components.checkBox("id", Model.of("Label"), Model.of(true), Model.of("Helper text"));
        tester.startComponentInPage(field);
        tester.assertLabel("id:container:label", "Label");
        tester.assertLabel("id:container:feedback", "Helper text");
    }
}

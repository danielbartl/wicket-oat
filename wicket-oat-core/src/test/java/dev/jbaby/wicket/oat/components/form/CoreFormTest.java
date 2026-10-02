package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.Component;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
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

    @Test
    void rerenderingDoesNotAccumulateBehaviors() {
        OatTextField<String> field = new OatTextField<>("id", "Label", Model.of("Value"));
        tester.startComponentInPage(field);
        Component container = field.get("container");
        int fieldBehaviors = field.getField().getBehaviors().size();
        int containerBehaviors = container.getBehaviors().size();

        WebPage page = (WebPage) tester.getLastRenderedPage();
        for (int i = 0; i < 3; i++) {
            tester.startPage(page);
        }

        assertThat(field.getField().getBehaviors()).hasSize(fieldBehaviors);
        assertThat(container.getBehaviors()).hasSize(containerBehaviors);
    }

    @Test
    void validationStateIsReflectedInAttributes() {
        Model<String> model = Model.of("");
        Form<Void> form = new Form<>("form");
        form.add(new OatTextField<>("name", "Name", model).setRequired(true));
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='name'></div></form>"));

        assertThat(tester.getTagByWicketId("container").getAttribute("data-field")).isEmpty();
        assertThat(tester.getTagByWicketId("field").getAttribute("aria-invalid")).isEqualTo("false");

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:container:field", "");
        formTester.submit();

        assertThat(tester.getTagByWicketId("container").getAttribute("data-field")).isEqualTo("error");
        assertThat(tester.getTagByWicketId("field").getAttribute("aria-invalid")).isEqualTo("true");

        formTester = tester.newFormTester("form");
        formTester.setValue("name:container:field", "Ada");
        formTester.submit();

        assertThat(tester.getTagByWicketId("container").getAttribute("data-field")).isEmpty();
        assertThat(tester.getTagByWicketId("field").getAttribute("aria-invalid")).isEqualTo("false");
        assertThat(model.getObject()).isEqualTo("Ada");
    }

    @Test
    void dropdownChoiceKeepsAccessibilityWiringOnItsRecreatedSelect() {
        Form<Void> form = new Form<>("form");
        OatDropdownChoice<String> choice = new OatDropdownChoice<>("choice", "Choice", Model.of((String) null), Model.ofList(java.util.List.of("A", "B")));
        choice.setRequired(true);
        form.add(choice);
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='choice'></div></form>"));

        TagTester select = tester.getTagByWicketId("field");
        assertThat(tester.getTagByWicketId("label").getAttribute("for")).isEqualTo(select.getAttribute("id"));
        assertThat(select.getAttribute("aria-describedby")).isEqualTo(tester.getTagByWicketId("feedback").getAttribute("id"));

        tester.newFormTester("form").submit();
        assertThat(tester.getTagByWicketId("field").getAttribute("aria-invalid")).isEqualTo("true");
    }
}

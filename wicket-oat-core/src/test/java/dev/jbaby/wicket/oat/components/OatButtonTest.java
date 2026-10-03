package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OatButtonTest {
    private WicketTester tester;
    private boolean clicked = false;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        clicked = false;
    }

    private OatButton button(String label) {
        return new OatButton("button", label) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                clicked = true;
            }
        };
    }

    @Test
    void testOatButtonRendersItsLabelWithoutInnerMarkup() {
        tester.startComponentInPage(button("Click <Me>"), Markup.of("<a wicket:id=\"button\"></a>"));
        TagTester tag = tester.getTagByWicketId("button");
        assertThat(tag.getAttribute("class")).contains("button");
        assertThat(tag.getValue()).isEqualTo("Click &lt;Me&gt;");
    }

    @Test
    void testOatButtonOnAButtonTagNeverSubmitsAForm() {
        tester.startComponentInPage(button("Click Me"), Markup.of("<button wicket:id=\"button\"></button>"));
        assertThat(tester.getTagByWicketId("button").getAttribute("type")).isEqualTo("button");
    }

    @Test
    void testOatButtonWithoutALabelRendersItsMarkupBody() {
        OatButton icon = new OatButton("button") {
            @Override
            public void onClick(AjaxRequestTarget target) {
            }
        }.setIcon(true).setVariant(OatVariant.DANGER);
        tester.startComponentInPage(icon, Markup.of("<button wicket:id=\"button\"><svg></svg></button>"));

        TagTester tag = tester.getTagByWicketId("button");
        assertThat(tag.getValue()).isEqualTo("<svg></svg>");
        assertThat(tag.getAttribute("class")).contains("icon");
        assertThat(tag.getAttribute("data-variant")).isEqualTo("danger");
    }

    @Test
    void testOatButtonClick() {
        tester.startComponentInPage(button("Click Me"), Markup.of("<a wicket:id=\"button\"></a>"));
        tester.clickLink("button");
        assertThat(clicked).isTrue();
    }

    @Test
    void testOatButtonFactoryWithModelLabel() {
        OatButton button = Oat.Components.button("button", Model.of("Click Me"), target -> clicked = true);
        tester.startComponentInPage(button, Markup.of("<a wicket:id=\"button\"></a>"));
        assertThat(tester.getTagByWicketId("button").getValue()).isEqualTo("Click Me");
        tester.clickLink("button");
        assertThat(clicked).isTrue();
    }

    @Test
    void testOatSubmitButtonRunsOnSubmitOrOnError() {
        List<String> calls = new ArrayList<>();
        Model<String> name = Model.of("");
        Form<Void> form = new Form<>("form");
        form.add(new OatTextField<>("name", "Name", name).setRequired(true));
        form.add(Oat.Components.submitButton("save", "Save", target -> calls.add("submit"), target -> calls.add("error")));
        tester.startComponentInPage(form, Markup.of(
                "<form wicket:id='form'><div wicket:id='name'></div><button wicket:id='save'></button></form>"));

        TagTester save = tester.getTagByWicketId("save");
        assertThat(save.getValue()).isEqualTo("Save");

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:container:field", "");
        tester.executeAjaxEvent("form:save", "click");
        assertThat(calls).containsExactly("error");

        formTester = tester.newFormTester("form");
        formTester.setValue("name:container:field", "Ada");
        tester.executeAjaxEvent("form:save", "click");
        assertThat(calls).containsExactly("error", "submit");
        assertThat(name.getObject()).isEqualTo("Ada");
    }

    @Test
    void testOatSubmitButtonOnAnInputUsesTheLabelAsValue() {
        Form<Void> form = new Form<>("form");
        form.add(new OatSubmitButton("save", "Save").setVariant(OatVariant.SECONDARY));
        tester.startComponentInPage(form, Markup.of(
                "<form wicket:id='form'><input type='submit' wicket:id='save'/></form>"));

        TagTester save = tester.getTagByWicketId("save");
        assertThat(save.getAttribute("value")).isEqualTo("Save");
        assertThat(save.getAttribute("data-variant")).isEqualTo("secondary");
    }
}

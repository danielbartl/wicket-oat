package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.Component;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChoiceFieldsTest {

    private static final IModel<List<String>> SIZES = Model.ofList(List.of("Small", "Medium", "Large"));

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    private FormTester formWith(Component field) {
        Form<Void> form = new Form<>("form");
        form.add(field);
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='choice'></div></form>"));
        return tester.newFormTester("form");
    }

    @Test
    void radioChoiceRendersAnOatFieldsetOfLabelledRadios() {
        formWith(new OatRadioChoice<>("choice", "Size", Model.of("Medium"), SIZES).setRequired(true));

        TagTester container = tester.getTagByWicketId("container");
        assertThat(container.getName()).isEqualTo("fieldset");
        assertThat(container.getAttribute("data-required")).isEmpty();
        TagTester legend = tester.getTagByWicketId("label");
        assertThat(legend.getName()).isEqualTo("legend");
        assertThat(legend.getAttribute("for")).isNull();

        TagTester group = tester.getTagByWicketId("field");
        assertThat(group.getAttribute("role")).isEqualTo("radiogroup");
        assertThat(group.getAttribute("class")).isEqualTo("vstack gap-2");
        assertThat(group.getAttribute("aria-required")).isEqualTo("true");
        // Each radio is wrapped in its label, the markup Oat styles
        assertThat(group.getValue()).contains("<label><input name=\"choice:container:field\" type=\"radio\"");
        assertThat(group.getValue()).containsPattern("checked=\"checked\"[^>]*/> Medium</label>");
    }

    @Test
    void radioChoiceCanBeInlineAndUpdatesItsModel() {
        Model<String> size = Model.of((String) null);
        FormTester formTester = formWith(Oat.Components.radioChoice("choice", Model.of("Size"), size, SIZES).setInline(true));
        assertThat(tester.getTagByWicketId("field").getAttribute("class")).isEqualTo("hstack");

        formTester.select("choice:container:field", 2);
        formTester.submit();
        assertThat(size.getObject()).isEqualTo("Large");
    }

    @Test
    void checkBoxMultipleChoiceUpdatesTheCollectionInPlace() {
        List<String> selected = new ArrayList<>(List.of("Small"));
        IModel<List<String>> model = Model.ofList(selected);
        FormTester formTester = formWith(new OatCheckBoxMultipleChoice<>("choice", "Sizes", model, SIZES));

        assertThat(tester.getTagByWicketId("field").getAttribute("role")).isEqualTo("group");
        formTester.selectMultiple("choice:container:field", new int[] {1, 2}, true);
        formTester.submit();

        tester.assertNoErrorMessage();
        assertThat(selected).containsExactly("Medium", "Large");
    }

    @Test
    void requiredCheckBoxMultipleChoiceNeedsASelection() {
        IModel<List<String>> model = Model.ofList(new ArrayList<>());
        FormTester formTester = formWith(new OatCheckBoxMultipleChoice<>("choice", "Sizes", model, SIZES).setRequired(true));
        formTester.submit();

        tester.assertErrorMessages("'Sizes' is required.");
    }

    @Test
    void listMultipleChoiceIsAMultiSelect() {
        IModel<List<String>> model = Model.ofList(new ArrayList<>());
        FormTester formTester = formWith(new OatListMultipleChoice<>("choice", "Sizes", model, SIZES).setMaxRows(3));

        TagTester select = tester.getTagByWicketId("field");
        assertThat(select.getName()).isEqualTo("select");
        assertThat(select.getAttribute("multiple")).isEqualTo("multiple");
        assertThat(select.getAttribute("size")).isEqualTo("3");
        assertThat(tester.getTagByWicketId("label").getAttribute("for")).isEqualTo(select.getAttribute("id"));

        formTester.selectMultiple("choice:container:field", new int[] {0, 2}, true);
        formTester.submit();
        assertThat(model.getObject()).containsExactly("Small", "Large");
    }
}

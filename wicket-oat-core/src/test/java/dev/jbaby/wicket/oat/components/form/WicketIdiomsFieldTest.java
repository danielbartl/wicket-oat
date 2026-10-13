package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.validation.validator.StringValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Oat form fields used the way Wicket developers use plain form components:
 * inherited models, labels from properties, Ajax re-rendering, add(validator).
 */
class WicketIdiomsFieldTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void labelsAreLookedUpByIdAndFallBackToTheId() {
        tester.startPage(FieldTestPage.class);

        tester.assertLabel("form:name:label", "Full name");
        tester.assertLabel("form:age:label", "Age");
        tester.assertLabel("form:nickname:label", "nickname");
    }

    @Test
    void validationMessagesUseTheLookedUpLabel() {
        tester.startPage(FieldTestPage.class);

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:input", "");
        formTester.submit();

        tester.assertErrorMessages("'Full name' is required.");
        // ... and is shown inline (HTML-escaped) in the field's feedback slot
        assertThat(tester.getTagByWicketId("feedback").getValue()).isEqualTo("&#039;Full name&#039; is required.");
    }

    @Test
    void modelsAreInheritedFromTheCompoundPropertyModelById() {
        FieldTestPage page = new FieldTestPage();
        page.person.name = "Ada";
        page.person.age = 36;
        tester.startPage(page);

        assertThat(fieldTag("name").getAttribute("value")).isEqualTo("Ada");
        assertThat(fieldTag("age").getAttribute("value")).isEqualTo("36");

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:input", "Grace");
        formTester.setValue("age:input", "42");
        formTester.setValue("active:input", "true");
        formTester.submit();

        tester.assertNoErrorMessage();
        assertThat(page.person.name).isEqualTo("Grace");
        // Converted to the bean property's type, which the inherited model exposes
        assertThat(page.person.age).isEqualTo(42);
        assertThat(page.person.active).isTrue();
    }

    @Test
    void fieldsCanBeReRenderedAfterAnAjaxSubmit() {
        tester.startPage(FieldTestPage.class);

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:input", "");
        tester.executeAjaxEvent("form:submit", "click");

        tester.assertComponentOnAjaxResponse("form:name");
        String response = tester.getLastResponseAsString();
        assertThat(response).contains("data-field=\"error\"").contains("&#039;Full name&#039; is required.");
    }

    @Test
    void thePanelRendersItsOwnTagWithAnIdAndKeepsTheMarkupAttributes() {
        tester.startPage(FieldTestPage.class);

        TagTester tag = tester.getTagByWicketId("name");
        assertThat(tag.getAttribute("id")).isNotBlank();
        assertThat(tag.getAttribute("class")).isEqualTo("span-2");
    }

    @Test
    void validatorsAndAjaxUpdatingBehaviorsAreAddedToTheInnerField() {
        Model<String> model = Model.of("");
        OatTextField<String> field = new OatTextField<>("id", "Label", model);
        AjaxFormComponentUpdatingBehavior onChange = new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        };
        field.add(StringValidator.maximumLength(3), onChange, AttributeModifier.append("class", "wide"));

        assertThat(field.getInput().getValidators()).hasSize(1);
        assertThat(field.getInput().getBehaviors(AjaxFormComponentUpdatingBehavior.class)).containsExactly(onChange);
        assertThat(field.getBehaviors()).hasSize(1); // only the AttributeModifier

        tester.startComponentInPage(field);
        assertThat(tester.getTagByWicketId("id").getAttribute("class")).isEqualTo("wide");
    }

    @Test
    void addToInputPutsBehaviorsOnTheInputItself() {
        OatTextField<String> field = new OatTextField<String>("id", "Email", Model.of(""))
                .addToInput(AttributeModifier.replace("autocomplete", "email"))
                .setPlaceholder("you@example.com");
        tester.startComponentInPage(field);

        TagTester input = tester.getTagByWicketId("input");
        assertThat(input.getAttribute("autocomplete")).isEqualTo("email");
        assertThat(input.getAttribute("placeholder")).isEqualTo("you@example.com");
        assertThat(tester.getTagByWicketId("id").getAttribute("autocomplete")).isNull();
    }

    @Test
    void choiceUpdatingBehaviorsAreAddedToTheInnerChoice() {
        OatRadioChoice<String> choice = new OatRadioChoice<>("id", "Size", Model.of("S"), Model.ofList(List.of("S", "M")));
        AjaxFormChoiceComponentUpdatingBehavior onChange = new AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        };
        choice.add(onChange);

        assertThat(choice.getInput().getBehaviors(AjaxFormChoiceComponentUpdatingBehavior.class)).containsExactly(onChange);
        tester.startComponentInPage(choice);
    }

    @Test
    void theInputIsAChildOfTheFieldWithAShortPath() {
        OatTextField<String> field = new OatTextField<>("email", "Email", Model.of(""));
        tester.startComponentInPage(field);

        assertThat(field.get(BaseOatField.INPUT_ID)).isSameAs(field.getInput());
        assertThat(field.getInput().getInputName()).isEqualTo("email:input");
    }

    @Test
    void fluentSettersReturnTheConcreteFieldType() {
        // These declarations only compile if the setters keep the concrete type
        OatNumberField<Integer> number = new OatNumberField<Integer>("n", Model.of(1)).setRequired(true).setMin(0).setMax(10);
        OatSwitch toggle = new OatSwitch("s", Model.of(true)).setRequired(false);
        OatTextField<String> text = new OatTextField<>("t", Model.of("")).setPlaceholder(Model.of("Type here")).setLabel(Model.of("Text"));

        assertThat(number.getInput().isRequired()).isTrue();
        assertThat(toggle.getInput().isRequired()).isFalse();
        assertThat(text.getInput().getLabel().getObject()).isEqualTo("Text");
    }

    @Test
    void setLabelUpdatesTheVisibleLabelAndTheValidationLabel() {
        OatTextField<String> field = new OatTextField<String>("id", Model.of("")).setLabel(Model.of("Email"));
        tester.startComponentInPage(field);

        tester.assertLabel("id:label", "Email");
        assertThat(field.getInput().getLabel().getObject()).isEqualTo("Email");
    }

    @Test
    void dropdownChoiceIsCreatedOnceWithItsChoices() {
        OatDropdownChoice<String> choice = new OatDropdownChoice<>("id", Model.of("B"), Model.ofList(List.of("A", "B")));
        tester.startComponentInPage(choice);

        assertThat(choice.get("input")).isSameAs(choice.getInput());
        assertThat(tester.getLastResponseAsString()).contains(">A</option>").contains("selected=\"selected\" value=\"1\">B</option>");
    }

    /** The inner input of the field with the given id; inputs are named by their form-relative path. */
    private TagTester fieldTag(String fieldId) {
        return TagTester.createTagByAttribute(tester.getLastResponseAsString(), "name", fieldId + ":input");
    }
}

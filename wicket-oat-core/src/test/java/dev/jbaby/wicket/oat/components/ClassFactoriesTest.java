package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The static factories that let a lambda stand in for a subclass, as Wicket's AjaxLink.onClick does. */
class ClassFactoriesTest {

    private WicketTester tester;
    private final List<String> log = new ArrayList<>();

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void aButtonRunsItsLambdaOnClick() {
        tester.startComponentInPage(OatButton.onClick("save", "Save", target -> log.add("clicked")),
                Markup.of("<a wicket:id='save'></a>"));
        tester.executeAjaxEvent("save", "click");
        assertThat(log).containsExactly("clicked");
    }

    @Test
    void aSubmitButtonRunsOnSubmitWhenValidAndOnErrorWhenNot() {
        Form<Void> form = new Form<>("form");
        form.add(new OatTextField<>("name", "Name", Model.of("")).setRequired(true));
        form.add(OatSubmitButton.onSubmit("save", "Save", target -> log.add("submit"), target -> log.add("error")));
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='name'></div><button wicket:id='save'></button></form>"));

        tester.executeAjaxEvent("form:save", "click");
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:input", "Ada");
        tester.executeAjaxEvent("form:save", "click");

        assertThat(log).containsExactly("error", "submit");
    }

    @Test
    void listComponentsFillEachItemFromItsObject() {
        tester.startComponentInPage(OatAccordion.of("faq", Model.ofList(List.of("Shipping", "Returns")), (item, question) -> {
            item.add(new Label("title", question));
            item.add(new Label("content", "About " + question));
        }), Markup.of("<details wicket:id='faq'><summary wicket:id='title'></summary><div wicket:id='content'></div></details>"));

        tester.assertLabel("faq:1:content", "About Returns");
    }

    @Test
    void theOatFacadeDelegatesToTheClassFactories() {
        Oat.Components.button("save", "Save", target -> log.add("facade")).onClick(null);
        assertThat(log).containsExactly("facade");
        assertThat(Oat.Components.accordion("faq", Model.ofList(List.of("x")), (item, x) -> { })).isInstanceOf(OatAccordion.class);
    }
}

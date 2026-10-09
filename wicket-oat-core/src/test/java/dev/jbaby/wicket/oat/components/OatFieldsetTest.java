package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OatFieldsetTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    private static Markup markup() {
        return Markup.of("<fieldset wicket:id='id'><span wicket:id='child'></span></fieldset>");
    }

    @Test
    void rendersLegendAndBody() {
        OatFieldset fieldset = Oat.Components.fieldset("id", "Billing address");
        fieldset.add(new Label("child", "Inside"));
        tester.startComponentInPage(fieldset, markup());

        assertThat(tester.getTagByWicketId("legend").getName()).isEqualTo("legend");
        assertThat(tester.getTagByWicketId("legend").getValue()).isEqualTo("Billing address");
        assertThat(tester.getTagByWicketId("child").getValue()).isEqualTo("Inside");
        assertThat(tester.getTagByWicketId("description")).isNull();
    }

    @Test
    void legendFallsBackToTheId() {
        OatFieldset fieldset = new OatFieldset("id");
        fieldset.add(new Label("child", "Inside"));
        tester.startComponentInPage(fieldset, markup());

        assertThat(tester.getTagByWicketId("legend").getValue()).isEqualTo("id");
    }

    @Test
    void showsADescriptionWhenSet() {
        OatFieldset fieldset = new OatFieldset("id", Model.of("Shipping")).setDescription("Where we send the goods");
        fieldset.add(new Label("child", "Inside"));
        tester.startComponentInPage(fieldset, markup());

        assertThat(tester.getTagByWicketId("description").getValue()).isEqualTo("Where we send the goods");

        fieldset.setDescription((String) null);
        tester.startComponentInPage(fieldset, markup());
        assertThat(tester.getTagByWicketId("description")).isNull();
    }

    @Test
    void mustBeOnAFieldset() {
        OatFieldset fieldset = new OatFieldset("id", "Billing");
        fieldset.add(new Label("child", "Inside"));
        assertThatThrownBy(() -> tester.startComponentInPage(fieldset,
                Markup.of("<div wicket:id='id'><span wicket:id='child'></span></div>")))
                .hasMessageContaining("fieldset");
    }

    public static class Address implements Serializable {
        public String street = "Main St";
    }

    @Test
    void fieldsInsideInheritACompoundPropertyModel() {
        Address address = new Address();
        Form<Address> form = new Form<>("form", new CompoundPropertyModel<>(address));
        OatFieldset fieldset = new OatFieldset("billing", "Billing");
        OatTextField<String> street = new OatTextField<>("street");
        fieldset.add(street);
        form.add(fieldset);
        tester.startComponentInPage(form, Markup.of(
                "<form wicket:id='form'><fieldset wicket:id='billing'><div wicket:id='street'></div></fieldset></form>"));

        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("Main St");

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue(street.getField(), "Elm St");
        formTester.submit();
        assertThat(address.street).isEqualTo("Elm St");
    }
}

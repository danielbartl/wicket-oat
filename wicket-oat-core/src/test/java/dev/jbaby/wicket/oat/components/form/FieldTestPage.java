package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;

import java.io.Serializable;

/**
 * A form written the plain-Wicket way: a CompoundPropertyModel supplies the field
 * models by id, FieldTestPage.properties supplies the labels, and an Ajax submit
 * re-renders the fields so validation errors show without a page reload.
 */
public class FieldTestPage extends WebPage {

    public static class Person implements Serializable {
        public String name;
        public Integer age;
        public String nickname;
        public Boolean active = Boolean.FALSE;
    }

    public final Person person = new Person();
    public final OatTextField<String> name;
    public final OatNumberField<Integer> age;
    public final OatTextField<String> nickname;
    public final OatSwitch active;

    public FieldTestPage() {
        Form<Person> form = new Form<>("form", new CompoundPropertyModel<>(person));
        add(form);

        form.add(name = new OatTextField<String>("name").setRequired(true));
        form.add(age = new OatNumberField<Integer>("age").setMin(0));
        form.add(nickname = new OatTextField<>("nickname"));
        form.add(active = new OatSwitch("active"));

        form.add(new AjaxButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                target.add(name, age, nickname, active);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(name, age, nickname, active);
            }
        });
    }
}

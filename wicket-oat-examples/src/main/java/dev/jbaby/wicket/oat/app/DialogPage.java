package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.OatDialog;
import dev.jbaby.wicket.oat.components.form.OatEmailField;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DialogPage extends BasePage {

    public static class Person implements Serializable {
        public String name;
        public String email;

        Person(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    private final List<Person> people = new ArrayList<>(List.of(
            new Person("Ada Lovelace", "ada@example.com"),
            new Person("Grace Hopper", "grace@example.com")));

    public DialogPage() {
        // Show success()/error() messages as toasts
        add(Oat.Behaviors.feedbackToasts());

        // 1. A confirmation dialog with its own trigger button
        add(new OatDialog("deleteDialog", Model.of("Delete item"), Model.of("Delete item?")) {
            @Override
            protected void onConfirm(AjaxRequestTarget target) {
                success("Item deleted.");
            }
        }.setBody(new Label(OatDialog.BODY_ID, "This action cannot be undone."))
                .setConfirmLabel(Model.of("Delete"))
                .setConfirmVariant(OatVariant.DANGER));

        // 2. An edit dialog opened from the server, with validated fields in its body
        WebMarkupContainer table = new WebMarkupContainer("people");
        table.setOutputMarkupId(true);
        add(table);

        Model<Person> selected = Model.of(people.get(0));
        OatDialog editDialog = new OatDialog("editDialog", Model.of("Edit person")) {
            @Override
            protected void onConfirm(AjaxRequestTarget target) {
                success("Saved " + selected.getObject().name + ".");
                target.add(table);
            }
        };
        // Fields take their models from the CompoundPropertyModel and their labels from
        // DialogPage.properties, both by id
        Fragment fields = new Fragment(OatDialog.BODY_ID, "editFields", this, new CompoundPropertyModel<>(selected));
        fields.add(new OatTextField<String>("name").setRequired(true));
        fields.add(new OatEmailField("email").setRequired(true));
        editDialog.setBody(fields).setConfirmLabel(Model.of("Save"));
        add(editDialog);

        table.add(new ListView<>("rows", people) {
            @Override
            protected void populateItem(ListItem<Person> item) {
                item.add(new Label("name", item.getModelObject().name));
                item.add(new Label("email", item.getModelObject().email));
                item.add(new AjaxLink<Void>("edit") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selected.setObject(item.getModelObject());
                        editDialog.open(target);
                    }
                }.add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE).setSize(ButtonBehavior.Size.SMALL)));
            }
        });
    }
}

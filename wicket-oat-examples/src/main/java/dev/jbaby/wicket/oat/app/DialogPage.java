package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.OatDescriptionList;
import dev.jbaby.wicket.oat.components.OatDialog;
import dev.jbaby.wicket.oat.components.OatDrawer;
import dev.jbaby.wicket.oat.components.form.OatDropdownChoice;
import dev.jbaby.wicket.oat.components.form.OatEmailField;
import dev.jbaby.wicket.oat.components.form.OatSwitch;
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
import java.util.Locale;

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
        }.setContent(new Label(OatDialog.CONTENT_ID, "This action cannot be undone."))
                .setConfirmLabel(Model.of("Delete"))
                .setConfirmVariant(OatVariant.DANGER));

        // 2. An edit dialog opened from the server, with validated fields in its content
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
        Fragment fields = new Fragment(OatDialog.CONTENT_ID, "editFields", this, new CompoundPropertyModel<>(selected));
        fields.add(new OatTextField<String>("name").setRequired(true));
        fields.add(new OatEmailField("email").setRequired(true));
        editDialog.setContent(fields).setConfirmLabel(Model.of("Save"));
        add(editDialog);

        // 3. A drawer for filters, with its own trigger button; Apply keeps what was chosen
        Model<String> status = Model.of("Open");
        Model<Boolean> overdueOnly = Model.of(false);
        OatDrawer filters = new OatDrawer("filtersDrawer", "Filters", "Filter orders") {
            @Override
            protected void onConfirm(AjaxRequestTarget target) {
                success("Showing " + status.getObject().toLowerCase(Locale.ROOT) + " orders"
                        + (Boolean.TRUE.equals(overdueOnly.getObject()) ? ", overdue only." : "."));
            }
        };
        Fragment filterFields = new Fragment(OatDialog.CONTENT_ID, "filterFields", this);
        filterFields.add(new OatDropdownChoice<>("status", "Status", status, Model.ofList(List.of("Open", "Paid", "Cancelled"))));
        filterFields.add(new OatSwitch("overdueOnly", "Overdue only", overdueOnly));
        filters.setContent(filterFields).setConfirmLabel(Model.of("Apply"));
        add(filters);

        // 4. A drawer that only shows a person's details, opened from a row; the × closes it
        OatDrawer details = new OatDrawer("detailsDrawer", Model.of("Details"));
        details.setFooterVisible(false);
        add(details);

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
                item.add(new AjaxLink<Void>("details") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Fragment content = new Fragment(OatDialog.CONTENT_ID, "personDetails", DialogPage.this);
                        content.add(new OatDescriptionList("list", new CompoundPropertyModel<>(item.getModel()))
                                .addProperty("name")
                                .addProperty("email"));
                        details.setHeader(Model.of(item.getModelObject().name));
                        details.open(content, target);
                    }
                }.add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.GHOST).setSize(ButtonBehavior.Size.SMALL)));
            }
        });
    }
}

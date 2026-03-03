package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.SpinnerBehavior;
import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.Model;

import java.util.Arrays;

public class FormPage extends BasePage {

    public FormPage() {
        Form<Void> form = new Form<>("form");
        form.setMultiPart(true); // Required for file upload
        add(form);

        // Input
        WebMarkupContainer nameField = new WebMarkupContainer("nameField");
        nameField.add(Oat.Behaviors.field());
        nameField.add(new TextField<>("name", Model.of("")));
        form.add(nameField);

        // Email
        WebMarkupContainer emailField = new WebMarkupContainer("emailField");
        emailField.add(Oat.Behaviors.field());
        emailField.add(new TextField<>("email", Model.of("")));
        form.add(emailField);

        // Password with Hint
        WebMarkupContainer passwordField = new WebMarkupContainer("passwordField");
        passwordField.add(Oat.Behaviors.field());
        passwordField.add(new PasswordTextField("password", Model.of("")));
        WebMarkupContainer hint = new WebMarkupContainer("hint");
        hint.add(Oat.Behaviors.hint());
        passwordField.add(hint);
        form.add(passwordField);

        // Checkbox
        WebMarkupContainer checkboxField = new WebMarkupContainer("checkboxField");
        checkboxField.add(Oat.Behaviors.field());
        checkboxField.add(new CheckBox("agree", Model.of(false)));
        form.add(checkboxField);

        // Switch
        WebMarkupContainer switchField = new WebMarkupContainer("switchField");
        CheckBox switchBtn = new CheckBox("switch", Model.of(true));
        switchBtn.add(Oat.Behaviors.switchBehavior());
        switchField.add(switchBtn);
        form.add(switchField);

        // Radio
        RadioGroup<String> group = new RadioGroup<>("radioGroup", Model.of("A"));
        group.add(new Radio<>("radioA", Model.of("A")));
        group.add(new Radio<>("radioB", Model.of("B")));
        form.add(group);

        // Select
        WebMarkupContainer selectField = new WebMarkupContainer("selectField");
        selectField.add(Oat.Behaviors.field());
        selectField.add(new DropDownChoice<>("select", Model.of("A"), Arrays.asList("A", "B", "C")));
        form.add(selectField);

        // Textarea
        WebMarkupContainer textareaField = new WebMarkupContainer("textareaField");
        textareaField.add(Oat.Behaviors.field());
        textareaField.add(new TextArea<>("message", Model.of("")));
        form.add(textareaField);

        // New Encapsulated Fields via Oat.Components factory
        form.add(Oat.Components.textField("oatName", "Oat Name", Model.of(""))
                .setPlaceholder(Model.of("Enter your name..."))
                .setRequired(true));

        form.add(Oat.Components.passwordField("oatPassword", Model.of("Oat Password"), Model.of(""), Model.of("Must be at least 8 chars"))
                .setPlaceholder(Model.of("Enter your password...")));

        form.add(Oat.Components.emailField("oatEmail", "Oat Email", Model.of(""))
                .setPlaceholder(Model.of("you@example.com")));

        form.add(Oat.Components.numberField("oatAge", "Oat Age (18-99)", Model.of(25))
                .setMin(18)
                .setMax(99));

        form.add(Oat.Components.textArea("oatBio", "Oat Biography", Model.of(""))
                .setPlaceholder(Model.of("Tell us about yourself...")));

        form.add(Oat.Components.dropdownChoice("oatSelect", "Oat Select", Model.of("A"), Model.ofList(Arrays.asList("A", "B", "C")))
                .setRequired(true));

        form.add(Oat.Components.dateField("oatDate", "Oat Date", Model.of(java.time.LocalDate.now())));

        form.add(Oat.Components.urlField("oatUrl", "Oat Website", Model.of("")));

        form.add(Oat.Components.colorField("oatColor", "Oat Color Picker", Model.of("#6200ee")));

        form.add(Oat.Components.rangeField("oatRange", "Oat Range", Model.of(50))
                .setMin(0)
                .setMax(100));

        form.add(Oat.Components.checkBox("oatCheck", "Oat Checkbox", Model.of(true)));

        form.add(Oat.Components.oatSwitch("oatSwitch", "Oat Switch", Model.of(false)));

        form.add(Oat.Components.timeField("oatTime", "Oat Time", Model.of(java.time.LocalTime.now())));

        form.add(Oat.Components.searchField("oatSearch", "Oat Search", Model.of("")));

        form.add(Oat.Components.fileUpload("oatFile", "Oat Upload", Model.ofList(new java.util.ArrayList<>())));

        form.add(Oat.Components.dateTimeLocalField("oatDateTime", "Oat Date & Time", Model.of(java.time.LocalDateTime.now())));

        form.add(Oat.Components.monthField("oatMonth", "Oat Month", Model.of("2026-02")));

        form.add(Oat.Components.weekField("oatWeek", "Oat Week", Model.of("2026-W09")));

        form.add(Oat.Components.telField("oatTel", "Oat Phone", Model.of("")));
        
        // Spinner
        WebMarkupContainer smallSpinner = new WebMarkupContainer("smallSpinner");
        smallSpinner.add(Oat.Behaviors.spinner(SpinnerBehavior.Size.SMALL));
        add(smallSpinner);

        WebMarkupContainer defaultSpinner = new WebMarkupContainer("defaultSpinner");
        defaultSpinner.add(Oat.Behaviors.spinner());
        add(defaultSpinner);

        WebMarkupContainer largeSpinner = new WebMarkupContainer("largeSpinner");
        largeSpinner.add(Oat.Behaviors.spinner(SpinnerBehavior.Size.LARGE));
        add(largeSpinner);

        // Skeleton
        WebMarkupContainer lineSkeleton = new WebMarkupContainer("lineSkeleton");
        lineSkeleton.add(Oat.Behaviors.skeleton(SkeletonBehavior.Shape.LINE));
        add(lineSkeleton);

        WebMarkupContainer boxSkeleton = new WebMarkupContainer("boxSkeleton");
        boxSkeleton.add(Oat.Behaviors.skeleton(SkeletonBehavior.Shape.BOX));
        add(boxSkeleton);

        // Progress & Meter
        add(Oat.Components.progress("progress", 60));
        add(Oat.Components.meter("meter", 0.8));
    }
}

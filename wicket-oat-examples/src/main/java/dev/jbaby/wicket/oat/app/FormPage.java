package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.*;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.Model;

import java.util.Arrays;

public class FormPage extends BasePage {

    public FormPage() {
        Form<Void> form = new Form<>("form");
        add(form);

        // Input
        WebMarkupContainer nameField = new WebMarkupContainer("nameField");
        nameField.add(new FieldBehavior());
        nameField.add(new TextField<>("name", Model.of("")));
        form.add(nameField);

        // Email
        WebMarkupContainer emailField = new WebMarkupContainer("emailField");
        emailField.add(new FieldBehavior());
        emailField.add(new TextField<>("email", Model.of("")));
        form.add(emailField);

        // Password with Hint
        WebMarkupContainer passwordField = new WebMarkupContainer("passwordField");
        passwordField.add(new FieldBehavior());
        passwordField.add(new PasswordTextField("password", Model.of("")));
        WebMarkupContainer hint = new WebMarkupContainer("hint");
        hint.add(new HintBehavior());
        passwordField.add(hint);
        form.add(passwordField);

        // Checkbox
        WebMarkupContainer checkboxField = new WebMarkupContainer("checkboxField");
        checkboxField.add(new FieldBehavior());
        checkboxField.add(new CheckBox("agree", Model.of(false)));
        form.add(checkboxField);

        // Switch
        WebMarkupContainer switchField = new WebMarkupContainer("switchField");
        CheckBox switchBtn = new CheckBox("switch", Model.of(true));
        switchBtn.add(new SwitchBehavior());
        switchField.add(switchBtn);
        form.add(switchField);

        // Radio
        RadioGroup<String> group = new RadioGroup<>("radioGroup", Model.of("A"));
        group.add(new Radio<>("radioA", Model.of("A")));
        group.add(new Radio<>("radioB", Model.of("B")));
        form.add(group);

        // Select
        WebMarkupContainer selectField = new WebMarkupContainer("selectField");
        selectField.add(new FieldBehavior());
        selectField.add(new DropDownChoice<>("select", Model.of("A"), Arrays.asList("A", "B", "C")));
        form.add(selectField);

        // Textarea
        WebMarkupContainer textareaField = new WebMarkupContainer("textareaField");
        textareaField.add(new FieldBehavior());
        textareaField.add(new TextArea<>("message", Model.of("")));
        form.add(textareaField);
        
        // Spinner
        WebMarkupContainer smallSpinner = new WebMarkupContainer("smallSpinner");
        smallSpinner.add(new SpinnerBehavior(SpinnerBehavior.Size.SMALL));
        add(smallSpinner);

        WebMarkupContainer defaultSpinner = new WebMarkupContainer("defaultSpinner");
        defaultSpinner.add(new SpinnerBehavior());
        add(defaultSpinner);

        WebMarkupContainer largeSpinner = new WebMarkupContainer("largeSpinner");
        largeSpinner.add(new SpinnerBehavior(SpinnerBehavior.Size.LARGE));
        add(largeSpinner);

        // Skeleton
        WebMarkupContainer lineSkeleton = new WebMarkupContainer("lineSkeleton");
        lineSkeleton.add(new SkeletonBehavior(SkeletonBehavior.Shape.LINE));
        add(lineSkeleton);

        WebMarkupContainer boxSkeleton = new WebMarkupContainer("boxSkeleton");
        boxSkeleton.add(new SkeletonBehavior(SkeletonBehavior.Shape.BOX));
        add(boxSkeleton);

        // Progress & Meter
        add(new Progress("progress", 60));
        add(new Meter("meter", Model.of(0.8), Model.of(0), Model.of(1), Model.of(0.3), Model.of(0.7), Model.of(1)));
    }
}

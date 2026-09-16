package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Collections;
import java.util.List;

/**
 * An Oat-styled form field wrapping a native {@code <select>} ({@link DropDownChoice}).
 *
 * @param <T> the type of the selectable choices
 */
public class OatDropdownChoice<T> extends BaseOatField<T, DropDownChoice<T>> {

    private final IModel<? extends List<? extends T>> choices;
    private final IChoiceRenderer<? super T> renderer;

    public OatDropdownChoice(String id, String label, IModel<T> model, IModel<? extends List<? extends T>> choices) {
        this(id, Model.of(label), model, choices, null, null);
    }

    public OatDropdownChoice(String id, IModel<String> label, IModel<T> model, IModel<? extends List<? extends T>> choices) {
        this(id, label, model, choices, null, null);
    }

    public OatDropdownChoice(String id, IModel<String> label, IModel<T> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer) {
        this(id, label, model, choices, renderer, null);
    }
    
    public OatDropdownChoice(String id, IModel<String> label, IModel<T> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer, IModel<String> helper) {
        super(id, label, model, helper);
        this.choices = choices;
        this.renderer = renderer;

        // Re-create the component correctly now that choices are available
        field = createFormComponent("field", model);
        container.replace(field);

        // Re-apply standard Oat field configuration
        field.setLabel(label);
        field.setOutputMarkupId(true);
        
        Label feedback = (Label) container.get("feedback");
        field.add(AttributeModifier.replace("aria-describedby", feedback.getMarkupId()));
        
        Label fieldLabel = (Label) container.get("label");
        fieldLabel.add(AttributeModifier.replace("for", field.getMarkupId()));
    }

    @Override
    protected DropDownChoice<T> createFormComponent(String id, IModel<T> model) {
        if (choices == null) {
            return new DropDownChoice<>(id, model, Collections.emptyList());
        }
        return new DropDownChoice<>(id, model, choices, renderer);
    }
}

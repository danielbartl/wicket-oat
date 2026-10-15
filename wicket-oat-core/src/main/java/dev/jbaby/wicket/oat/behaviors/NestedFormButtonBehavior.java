package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;

/**
 * For an Ajax submit button whose form may end up inside another form, e.g. in a
 * dialog or a table cell on a page with a form of its own. A nested form renders as a
 * {@code <div>}, so its submit buttons belong to the outer form in the browser, and
 * Enter in a field of the outer form clicks the first of them: possibly this one. Inside
 * another form this renders the button as {@code type="button"}, which the browser
 * never picks for Enter, and which still submits its own form over Ajax when clicked.
 * <p>
 * Enter in the nested form's own fields then no longer reaches the button by itself;
 * make it the form's {@linkplain Form#setDefaultButton default button} for that.
 * {@code OatSubmitButton} has this behavior already.
 */
public class NestedFormButtonBehavior extends Behavior {

    @Override
    public void bind(Component component) {
        super.bind(component);
        if (!(component instanceof IFormSubmittingComponent)) {
            throw new IllegalArgumentException("NestedFormButtonBehavior is for buttons, not " + component.getClass().getName());
        }
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        Form<?> form = ((IFormSubmittingComponent) component).getForm();
        if (form != null && !form.isRootForm()) {
            tag.put("type", "button");
        }
    }
}

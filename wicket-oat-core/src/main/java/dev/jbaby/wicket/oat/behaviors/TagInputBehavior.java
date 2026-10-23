package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * Wires an {@code <ot-taginput>} element to a hidden form field so its tags are
 * included in native form submission. Oat's taginput web component only tracks
 * tags client-side (as sibling badge elements) and never populates its inner
 * &lt;input&gt;'s value for submission, so a bridge is required: on load the hidden
 * field's comma-separated value seeds the component via its {@code .value}
 * property setter, and whenever the tags or the text being typed change, the
 * component's current tags are written back into the hidden field, with the text
 * not yet made a tag as one more.
 */
public class TagInputBehavior extends Behavior {

    private final FormComponent<?> hiddenField;

    public TagInputBehavior(FormComponent<?> hiddenField) {
        this.hiddenField = hiddenField;
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        component.setOutputMarkupId(true);
        hiddenField.setOutputMarkupId(true);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        // The component's "input" event comes when a tag is added or removed, the text box's
        // as the user types. Oat stops the text box's from bubbling, so listen in the capture
        // phase; then text not yet made a tag, e.g. before a click on Save, is sent too.
        String script = String.format(
                "(function(){var el=document.getElementById('%s'),hidden=document.getElementById('%s');" +
                        "if(!el||!hidden)return;" +
                        "if(hidden.value){el.value=hidden.value.split(',').map(function(s){return s.trim();}).filter(Boolean);}" +
                        "el.addEventListener('input',function(){var tags=Array.isArray(el.value)?el.value.slice():[]," +
                        "text=el.querySelector('input');if(text&&text.value.trim()){tags.push(text.value);}" +
                        "hidden.value=tags.join(',');},true);" +
                        "})();",
                component.getMarkupId(), hiddenField.getMarkupId());
        response.render(OnDomReadyHeaderItem.forScript(script));
    }
}

package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

/**
 * A behavior that attaches a client-side click listener to a component.
 * This is CSP-compliant as it uses Wicket's nonced script mechanism
 * instead of inline 'onclick' attributes.
 */
public class ClientSideClickBehavior extends Behavior {

    private final String javascript;

    public ClientSideClickBehavior(String javascript) {
        this.javascript = javascript;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        
        // Wicket will automatically add a nonce to this script tag
        String script = String.format(
            "document.getElementById('%s').addEventListener('click', function(e) { %s });",
            component.getMarkupId(),
            javascript
        );
        
        response.render(OnDomReadyHeaderItem.forScript(script));
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        component.setOutputMarkupId(true);
    }
}

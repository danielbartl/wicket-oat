package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.CardBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * A simple container component that renders as an Oat Card.
 * The markup for this component should ideally be an &lt;article&gt; tag.
 *
 * <pre>
 * &lt;article wicket:id="card"&gt;
 *     &lt;header&gt;
 *         &lt;h3&gt;Title&lt;/h3&gt;
 *     &lt;/header&gt;
 *     &lt;p&gt;Content goes here.&lt;/p&gt;
 *     &lt;footer&gt;
 *         Footer actions.
 *     &lt;/footer&gt;
 * &lt;/article&gt;
 * </pre>
 */
public class OatCard extends WebMarkupContainer {

    public OatCard(String id) {
        super(id);
        add(new CardBehavior());
    }
}

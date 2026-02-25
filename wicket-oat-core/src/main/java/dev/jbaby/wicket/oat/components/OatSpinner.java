package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.SpinnerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * A simple container component that renders as an Oat Spinner.
 */
public class OatSpinner extends WebMarkupContainer {

    public OatSpinner(String id) {
        this(id, SpinnerBehavior.Size.DEFAULT);
    }

    public OatSpinner(String id, SpinnerBehavior.Size size) {
        super(id);
        add(new SpinnerBehavior(size));
    }

    public OatSpinner(String id, IModel<SpinnerBehavior.Size> sizeModel) {
        super(id);
        add(new SpinnerBehavior(sizeModel));
    }
}

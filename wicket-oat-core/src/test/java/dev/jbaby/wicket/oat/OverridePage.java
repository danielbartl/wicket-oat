package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.components.OatDialog;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/** A page whose OverridePage.properties overrides a library text. */
public class OverridePage extends WebPage implements IMarkupResourceStreamProvider {

    public OverridePage() {
        add(new OatDialog("dialog", "Edit"));
    }

    @Override
    public IResourceStream getMarkupResourceStream(org.apache.wicket.MarkupContainer container, Class<?> containerClass) {
        return new StringResourceStream("<html><body><div wicket:id='dialog'></div></body></html>");
    }
}

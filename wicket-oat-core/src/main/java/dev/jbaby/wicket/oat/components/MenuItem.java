package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;

/**
 * A single sidebar navigation entry for {@link OatAppLayout}: a label and the Wicket
 * {@link Page} it links to, optionally with {@link PageParameters}. Entries for pages
 * the current user isn't authorized to instantiate are left out.
 *
 * @param label the label; use a {@code ResourceModel} to translate it
 * @param pageClass the page to link to
 * @param parameters the page parameters, or {@code null}
 */
public record MenuItem(
        IModel<String> label,
        Class<? extends Page> pageClass,
        PageParameters parameters
) implements Serializable {

    public MenuItem(String label, Class<? extends Page> pageClass) {
        this(Model.of(label), pageClass, null);
    }

    public static MenuItem of(String label, Class<? extends Page> pageClass) {
        return new MenuItem(Model.of(label), pageClass, null);
    }

    public static MenuItem of(String label, Class<? extends Page> pageClass, PageParameters parameters) {
        return new MenuItem(Model.of(label), pageClass, parameters);
    }

    public static MenuItem of(IModel<String> label, Class<? extends Page> pageClass) {
        return new MenuItem(label, pageClass, null);
    }

    public static MenuItem of(IModel<String> label, Class<? extends Page> pageClass, PageParameters parameters) {
        return new MenuItem(label, pageClass, parameters);
    }
}

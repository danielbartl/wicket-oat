package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatSettings;
import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Response;
import org.apache.wicket.response.StringResponse;

import java.io.Serializable;

/**
 * Behavior that applies Oat's Button styling.
 * It adds data-variant for colors and classes for styles/sizes.
 * <p>
 * With {@link OatSettings#setAccessibilityChecks accessibility checks} on, as they are in
 * development mode, a button without an accessible name fails to render: one with no
 * text, no {@code aria-label} and no labelled icon, which a screen reader announces only
 * as "button".
 */
public class ButtonBehavior extends Behavior {

    public enum Style implements Serializable {
        DEFAULT(null),
        OUTLINE("outline"),
        GHOST("ghost");

        private final String className;

        Style(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    public enum Size implements Serializable {
        DEFAULT(null),
        SMALL("small"),
        LARGE("large");

        private final String className;

        Size(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    private IModel<OatVariant> variantModel = Model.of(OatVariant.DEFAULT);
    private IModel<Style> styleModel = Model.of(Style.DEFAULT);
    private IModel<Size> sizeModel = Model.of(Size.DEFAULT);
    private boolean icon = false;
    // While a button renders under the accessibility checks: the response it would have
    // been written to, as it is captured to be checked first
    private transient Response original;

    public ButtonBehavior() {}

    public ButtonBehavior(OatVariant variant) {
        this(Model.of(variant));
    }

    public ButtonBehavior(IModel<OatVariant> variantModel) {
        this.variantModel = variantModel;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        // Oat styles <a> as buttons if they have the .button class
        if (tag.getName().equalsIgnoreCase("a")) {
            tag.append("class", "button", " ");
        }

        OatVariant variant = variantModel.getObject();
        if (variant != null && variant.getValue() != null) {
            tag.put("data-variant", variant.getValue());
        }

        Style style = styleModel.getObject();
        if (style != null && style.getClassName() != null) {
            tag.append("class", style.getClassName(), " ");
        }

        Size size = sizeModel.getObject();
        if (size != null && size.getClassName() != null) {
            tag.append("class", size.getClassName(), " ");
        }

        if (icon) {
            tag.append("class", "icon", " ");
        }
    }

    @Override
    public void beforeRender(Component component) {
        super.beforeRender(component);
        if (!component.getRenderBodyOnly() && OatSettings.get(component.getApplication()).isAccessibilityChecks()) {
            original = component.getRequestCycle().setResponse(new StringResponse());
        }
    }

    @Override
    public void afterRender(Component component) {
        if (original != null) {
            CharSequence html = ((StringResponse) component.getRequestCycle().setResponse(original)).getBuffer();
            original.write(html);
            original = null;
            if (!AccessibleNames.hasName(html)) {
                throw new IllegalStateException(unnamed(component));
            }
        }
        super.afterRender(component);
    }

    @Override
    public void onException(Component component, RuntimeException exception) {
        if (original != null) {
            component.getRequestCycle().setResponse(original);
            original = null;
        }
        super.onException(component, exception);
    }

    private static String unnamed(Component component) {
        Class<?> type = component.getClass();
        while (type.isAnonymousClass()) {
            type = type.getSuperclass();
        }
        return "The button \"" + component.getPageRelativePath() + "\" (" + type.getSimpleName() + " on "
                + component.getPage().getClass().getSimpleName() + ") has no accessible name, so a screen reader announces it only as \"button\". Give it a"
                + " label, an aria-label (add(AttributeModifier.replace(\"aria-label\", ...))) or a labelled"
                + " icon (new OatIcon(\"icon\", \"trash-2\").setLabel(...)). Wicket Oat checks this in"
                + " development mode; OatSettings.setAccessibilityChecks(false) turns the check off.";
    }

    public ButtonBehavior setVariant(OatVariant variant) {
        this.variantModel = Model.of(variant);
        return this;
    }

    public ButtonBehavior setStyle(Style style) {
        this.styleModel.setObject(style);
        return this;
    }

    public ButtonBehavior setSize(Size size) {
        this.sizeModel.setObject(size);
        return this;
    }

    public ButtonBehavior setIcon(boolean icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public void detach(Component component) {
        super.detach(component);
        variantModel.detach();
        styleModel.detach();
        sizeModel.detach();
    }
}

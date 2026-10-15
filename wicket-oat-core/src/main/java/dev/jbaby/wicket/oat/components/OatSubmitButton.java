package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.AjaxBusyBehavior;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.NestedFormButtonBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import java.util.Objects;

/**
 * An Oat-styled {@link AjaxButton}: submits its form over Ajax, running
 * {@code onSubmit(target)} when the form is valid and {@code onError(target)} when it
 * isn't. Attach it to a {@code <button>} or an {@code <input type="submit">}; the
 * label becomes the button's text (or {@code value}), so
 * {@code <button wicket:id="save"></button>} is enough.
 * <p>
 * For a slow save, {@link #setBusyIndicator setBusyIndicator(true)} shows a spinner
 * while the form is submitted and ignores further clicks, so it can't be submitted twice.
 * <p>
 * In a form nested in another, it renders as {@code type="button"}, so Enter in a field
 * of the outer form can't click it ({@link NestedFormButtonBehavior}); make it the nested
 * form's default button for Enter in that form's fields.
 */
public class OatSubmitButton extends AjaxButton {

    private final ButtonBehavior buttonBehavior = new ButtonBehavior();

    /** A button keeping its markup label. */
    public OatSubmitButton(String id) {
        this(id, (IModel<String>) null);
    }

    public OatSubmitButton(String id, String label) {
        this(id, Model.of(label));
    }

    public OatSubmitButton(String id, IModel<String> label) {
        super(id, label);
        add(buttonBehavior, new NestedFormButtonBehavior());
    }

    /** A button submitting the given form, which needn't contain it. */
    public OatSubmitButton(String id, IModel<String> label, Form<?> form) {
        super(id, label, form);
        add(buttonBehavior, new NestedFormButtonBehavior());
    }

    public OatSubmitButton setStyle(ButtonBehavior.Style style) {
        buttonBehavior.setStyle(style);
        return this;
    }

    public OatSubmitButton setSize(ButtonBehavior.Size size) {
        buttonBehavior.setSize(size);
        return this;
    }

    public OatSubmitButton setVariant(OatVariant variant) {
        buttonBehavior.setVariant(variant);
        return this;
    }

    /** Square button sized for a single icon. */
    public OatSubmitButton setIcon(boolean icon) {
        buttonBehavior.setIcon(icon);
        return this;
    }

    /**
     * Whether the button shows a spinner and ignores further clicks while its Ajax
     * request runs (off by default; see {@link AjaxBusyBehavior}).
     */
    public OatSubmitButton setBusyIndicator(boolean busyIndicator) {
        if (busyIndicator && getBehaviors(AjaxBusyBehavior.class).isEmpty()) {
            add(new AjaxBusyBehavior());
        } else if (!busyIndicator) {
            getBehaviors(AjaxBusyBehavior.class).forEach(this::remove);
        }
        return this;
    }

    /**
     * A button submitting its form over Ajax, running {@code onSubmit} when the form is
     * valid, without a subclass:
     * {@code form.add(OatSubmitButton.onSubmit("save", "Save", target -> ...))}.
     */
    public static OatSubmitButton onSubmit(String id, String label, SerializableConsumer<AjaxRequestTarget> onSubmit) {
        return onSubmit(id, Model.of(label), onSubmit, null);
    }

    /** A button submitting its form over Ajax, running {@code onSubmit} when the form is valid. */
    public static OatSubmitButton onSubmit(String id, IModel<String> label, SerializableConsumer<AjaxRequestTarget> onSubmit) {
        return onSubmit(id, label, onSubmit, null);
    }

    /**
     * A button submitting its form over Ajax, running {@code onSubmit} when the form is
     * valid and {@code onError} when it isn't, e.g. {@code target -> target.add(form)} to
     * show the errors.
     */
    public static OatSubmitButton onSubmit(String id, String label, SerializableConsumer<AjaxRequestTarget> onSubmit,
                                           SerializableConsumer<AjaxRequestTarget> onError) {
        return onSubmit(id, Model.of(label), onSubmit, onError);
    }

    /**
     * A button submitting its form over Ajax, running {@code onSubmit} when the form is
     * valid and {@code onError}, if given, when it isn't.
     */
    public static OatSubmitButton onSubmit(String id, IModel<String> label, SerializableConsumer<AjaxRequestTarget> onSubmit,
                                           SerializableConsumer<AjaxRequestTarget> onError) {
        Objects.requireNonNull(onSubmit);
        return new OatSubmitButton(id, label) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                onSubmit.accept(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                if (onError != null) {
                    onError.accept(target);
                }
            }
        };
    }
}

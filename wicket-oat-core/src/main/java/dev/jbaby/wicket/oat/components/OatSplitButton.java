package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A button for the usual action, joined to a "▾" button opening a menu of related
 * ones - "Save", with "Save as draft" and "Save and send" in the menu:
 * <pre>{@code
 * OatSplitButton save = new OatSplitButton("save", "Save", target -> save(false));
 * save.addAction("Save as draft", target -> saveDraft());
 * save.addAction("Save and send", target -> save(true));
 * add(save);
 * }</pre>
 * on {@code <div wicket:id="save"></div>}. Everything runs over Ajax; choosing a menu
 * entry closes the menu. The "▾" button is named by the {@code OatSplitButton.more}
 * resource ("More actions") for screen readers. Variant, style and size apply to both
 * buttons.
 */
public class OatSplitButton extends Panel {

    private final ButtonBehavior buttons = new ButtonBehavior();
    private final List<MenuAction> actions = new ArrayList<>();
    private final OatDropdown<MenuAction> menu;

    public OatSplitButton(String id, String label, SerializableConsumer<AjaxRequestTarget> onClick) {
        this(id, Model.of(label), onClick);
    }

    public OatSplitButton(String id, IModel<String> label, SerializableConsumer<AjaxRequestTarget> onClick) {
        super(id);
        setOutputMarkupId(true);

        AjaxLink<Void> primary = new AjaxLink<>("primary") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onClick.accept(target);
            }
        };
        primary.setBody(label);
        primary.add(buttons);
        add(primary);

        menu = new OatDropdown<>("menu", Model.of("▾"), Model.ofList(actions)) {
            @Override
            protected void populateItem(ListItem<MenuAction> item) {
                MenuAction action = item.getModelObject();
                item.add(new Label("label", action.label));
                if (action.variant != null && action.variant != OatVariant.DEFAULT) {
                    item.add(AttributeModifier.replace("data-variant", action.variant.getValue()));
                }
                item.add(AjaxEventBehavior.onEvent("click", target -> {
                    action.onClick.accept(target);
                    close(target);
                }));
            }
        };
        // Styled like the primary button rather than the dropdown's default outline
        menu.getTrigger().add(AttributeModifier.remove("class"), buttons);
        menu.getTrigger().add(AttributeModifier.replace("aria-label",
                new StringResourceModel("OatSplitButton.more", this).setDefaultValue("More actions")));
        add(menu);
    }

    /**
     * Adds an entry to the menu.
     *
     * @return the entry, e.g. to {@link MenuAction#setVariant set its variant}
     */
    public MenuAction addAction(String label, SerializableConsumer<AjaxRequestTarget> onClick) {
        return addAction(Model.of(label), onClick);
    }

    /**
     * Adds an entry to the menu.
     *
     * @return the entry, e.g. to {@link MenuAction#setVariant set its variant}
     */
    public MenuAction addAction(IModel<String> label, SerializableConsumer<AjaxRequestTarget> onClick) {
        MenuAction action = new MenuAction(label, onClick);
        actions.add(action);
        return action;
    }

    public OatSplitButton setVariant(OatVariant variant) {
        buttons.setVariant(variant);
        return this;
    }

    public OatSplitButton setStyle(ButtonBehavior.Style style) {
        buttons.setStyle(style);
        return this;
    }

    public OatSplitButton setSize(ButtonBehavior.Size size) {
        buttons.setSize(size);
        return this;
    }

    /** Closes the menu, e.g. from another Ajax handler. */
    public OatSplitButton close(AjaxRequestTarget target) {
        menu.close(target);
        return this;
    }

    /** An entry in the menu. */
    public static final class MenuAction implements Serializable {

        private final IModel<String> label;
        private final SerializableConsumer<AjaxRequestTarget> onClick;
        private OatVariant variant;

        MenuAction(IModel<String> label, SerializableConsumer<AjaxRequestTarget> onClick) {
            this.label = label;
            this.onClick = onClick;
        }

        /** E.g. {@code DANGER} for a destructive action, shown in red. */
        public MenuAction setVariant(OatVariant variant) {
            this.variant = variant;
            return this;
        }
    }
}

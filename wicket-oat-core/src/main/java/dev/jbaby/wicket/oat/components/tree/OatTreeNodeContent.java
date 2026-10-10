package dev.jbaby.wicket.oat.components.tree;

import dev.jbaby.wicket.oat.components.OatIcon;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/** A tree node's icon and label, in a link when nodes can be selected. */
final class OatTreeNodeContent<T> extends Panel {

    OatTreeNodeContent(String id, IModel<T> model, SerializableFunction<T, String> label,
                       SerializableFunction<T, String> icon, SerializableBiConsumer<AjaxRequestTarget, T> onClick,
                       IModel<Boolean> current) {
        super(id, model);
        AjaxLink<Void> link = new AjaxLink<>("link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onClick.accept(target, model.getObject());
            }
        };
        link.setVisible(onClick != null);
        link.add(AttributeModifier.replace("aria-current", () -> Boolean.TRUE.equals(current.getObject()) ? "true" : null));
        add(link);
        WebMarkupContainer plain = new WebMarkupContainer("plain");
        plain.setVisible(onClick == null);
        add(plain);

        MarkupContainer holder = onClick != null ? link : plain;
        String name = icon == null ? null : icon.apply(model.getObject());
        holder.add(name == null ? new WebMarkupContainer("icon").setVisible(false) : new OatIcon("icon", name));
        holder.add(new Label("label", () -> label.apply(model.getObject())));
        MarkupContainer other = onClick != null ? plain : link;
        other.add(new WebMarkupContainer("icon"), new WebMarkupContainer("label"));
    }

}

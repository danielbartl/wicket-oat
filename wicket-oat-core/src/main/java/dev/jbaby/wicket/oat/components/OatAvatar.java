package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Renders as an Oat Avatar: {@code <figure data-variant="avatar">} wrapping either
 * an {@code <img>} or text initials in an {@code <abbr>}.
 */
public class OatAvatar extends Panel {

    public enum Size {
        SMALL("small"),
        DEFAULT(null),
        LARGE("large");

        private final String className;
        Size(String className) { this.className = className; }
        public String getClassName() { return className; }
    }

    public OatAvatar(String id, IModel<String> imageUrl) {
        this(id, imageUrl, null, Size.DEFAULT);
    }

    public OatAvatar(String id, String initials) {
        this(id, null, Model.of(initials), Size.DEFAULT);
    }

    public OatAvatar(String id, IModel<String> imageUrl, IModel<String> initials, Size size) {
        super(id);
        setRenderBodyOnly(true);

        WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        if (size != Size.DEFAULT) {
            container.add(AttributeModifier.append("class", size.getClassName()));
        }

        boolean hasImage = imageUrl != null && imageUrl.getObject() != null;

        WebMarkupContainer img = new WebMarkupContainer("img");
        if (hasImage) {
            img.add(AttributeModifier.replace("src", imageUrl));
        }
        img.setVisible(hasImage);
        container.add(img);

        Label label = new Label("initials", initials != null ? initials : Model.of(""));
        label.setVisible(!hasImage);
        if (!hasImage && initials != null) {
            label.add(AttributeModifier.replace("title", initials));
        }
        container.add(label);
    }
}

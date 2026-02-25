package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class OatAvatar extends Panel {

    public enum Size {
        SMALL("sm"),
        DEFAULT(null),
        LARGE("lg");

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

        WebMarkupContainer img = new WebMarkupContainer("img");
        if (imageUrl != null && imageUrl.getObject() != null) {
            img.add(AttributeModifier.replace("src", imageUrl));
            img.setVisible(true);
        } else {
            img.setVisible(false);
        }
        container.add(img);

        Label label = new Label("initials", initials != null ? initials : Model.of(""));
        label.setVisible(imageUrl == null || imageUrl.getObject() == null);
        container.add(label);
    }
}

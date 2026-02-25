package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A component to display when a data-bound container is empty.
 */
public class OatEmptyState extends Panel {

    public OatEmptyState(String id, String title) {
        this(id, Model.of(title), null);
    }

    public OatEmptyState(String id, IModel<String> titleModel, IModel<String> messageModel) {
        super(id);
        
        add(new Label("title", titleModel));
        
        Label message = new Label("message", messageModel);
        message.setVisible(messageModel != null && messageModel.getObject() != null);
        add(message);
    }
}

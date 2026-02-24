package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

public class OatFileUpload extends BaseOatField<List<FileUpload>, FileUploadField> {

    public OatFileUpload(String id, String label, IModel<List<FileUpload>> model) {
        this(id, Model.of(label), model, null);
    }

    public OatFileUpload(String id, IModel<String> label, IModel<List<FileUpload>> model) {
        this(id, label, model, null);
    }

    public OatFileUpload(String id, IModel<String> label, IModel<List<FileUpload>> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected FileUploadField createFormComponent(String id, IModel<List<FileUpload>> model) {
        return new FileUploadField(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "file");
                super.onComponentTag(tag);
            }
        };
    }
}

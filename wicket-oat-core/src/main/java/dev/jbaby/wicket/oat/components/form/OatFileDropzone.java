package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * A click-and-drag-and-drop file uploader backed by Oat's {@code ot-upload}
 * web component. This is a richer, drag-and-drop-capable alternative to
 * {@link OatFileUpload}'s plain file input; both wrap the same native
 * multi-file {@code <input type="file">} and need no special server-side
 * handling beyond what {@link OatFileUpload} already requires, since
 * {@code ot-upload} only enhances presentation and leaves native file
 * selection/submission untouched.
 */
public class OatFileDropzone extends BaseOatField<List<FileUpload>, FileUploadField> {

    public OatFileDropzone(String id, String label, IModel<List<FileUpload>> model) {
        this(id, Model.of(label), model, null);
    }

    public OatFileDropzone(String id, IModel<String> label, IModel<List<FileUpload>> model) {
        this(id, label, model, null);
    }

    public OatFileDropzone(String id, IModel<String> label, IModel<List<FileUpload>> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected FileUploadField createFormComponent(String id, IModel<List<FileUpload>> model) {
        return new FileUploadField(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "file");
                tag.put("multiple", "multiple");
                super.onComponentTag(tag);
            }
        };
    }
}

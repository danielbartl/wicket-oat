package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.Model;

import java.util.ArrayList;

public class UploadPage extends BasePage {

    public UploadPage() {
        Form<Void> form = new Form<>("form");
        form.setMultiPart(true);
        add(form);

        form.add(Oat.Components.fileUpload("plainUpload", "Plain file input", Model.ofList(new ArrayList<FileUpload>())));
        form.add(Oat.Components.fileDropzone("dropzoneUpload", "Drag-and-drop upload", Model.ofList(new ArrayList<FileUpload>())));
    }
}

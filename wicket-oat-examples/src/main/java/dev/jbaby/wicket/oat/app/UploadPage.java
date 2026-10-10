package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.OatFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UploadPage extends BasePage {

    public UploadPage() {
        Form<Void> form = new Form<>("form");
        form.setMultiPart(true);
        form.setOutputMarkupId(true);
        add(form);
        // Also shows Wicket's error for files over the application's upload limit
        form.add(new OatFeedbackPanel("feedback"));

        IModel<List<FileUpload>> plain = Model.ofList(new ArrayList<>());
        IModel<List<FileUpload>> dropzone = Model.ofList(new ArrayList<>());
        form.add(Oat.Components.fileUpload("plainUpload", "Plain file input", plain));
        form.add(Oat.Components.fileDropzone("dropzoneUpload", "Drag-and-drop upload", dropzone));

        form.add(Oat.Components.submitButton("upload", "Upload", target -> {
            // A field without a file sets its model to null
            List<FileUpload> files = Stream.of(plain.getObject(), dropzone.getObject())
                    .filter(Objects::nonNull).flatMap(List::stream).toList();
            if (files.isEmpty()) {
                form.info("Choose a file first.");
            } else {
                form.success("Received " + files.stream()
                        .map(file -> file.getClientFileName() + " (" + Bytes.bytes(file.getSize()) + ")")
                        .collect(Collectors.joining(", ")) + ".");
            }
            // The demo keeps nothing: forget the uploads and clear the inputs
            plain.setObject(new ArrayList<>());
            dropzone.setObject(new ArrayList<>());
            target.add(form);
        }));
    }
}

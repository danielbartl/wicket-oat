package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UploadPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testUploadPageRenders() {
        tester.startPage(UploadPage.class);
        tester.assertRenderedPage(UploadPage.class);

        tester.assertComponent("form:plainUpload", dev.jbaby.wicket.oat.components.form.OatFileUpload.class);
        tester.assertComponent("form:dropzoneUpload", dev.jbaby.wicket.oat.components.form.OatFileDropzone.class);
    }

    @Test
    void reportsTheUploadedFile(@TempDir Path dir) throws IOException {
        tester.startPage(UploadPage.class);
        FormTester form = tester.newFormTester("form");
        form.setFile("plainUpload:container:field", new File(Files.write(dir.resolve("notes.txt"), new byte[1024]).toFile()), "text/plain");
        tester.executeAjaxEvent("form:upload", "click");

        String message = tester.getFeedbackMessages(null).get(0).getMessage().toString();
        assertTrue(message.startsWith("Received notes.txt"), message);
    }

    @Test
    void rejectsFilesOverTheUploadLimit(@TempDir Path dir) throws IOException {
        tester.startPage(UploadPage.class);
        FormTester form = tester.newFormTester("form");
        form.setFile("plainUpload:container:field", new File(Files.write(dir.resolve("big.bin"), new byte[6 * 1024 * 1024]).toFile()), "application/octet-stream");
        tester.executeAjaxEvent("form:upload", "click");

        tester.assertErrorMessages("File must be less than 5MB.");
    }
}

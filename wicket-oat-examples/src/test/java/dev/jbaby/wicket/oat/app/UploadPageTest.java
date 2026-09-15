package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
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
}

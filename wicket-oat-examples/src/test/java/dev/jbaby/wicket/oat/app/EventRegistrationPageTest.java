package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EventRegistrationPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void labelsComeFromThePropertiesFile() {
        tester.startPage(EventRegistrationPage.class);
        tester.assertRenderedPage(EventRegistrationPage.class);
        tester.assertLabel("registrationForm:fullName:container:label", "Full Name");
        tester.assertLabel("registrationForm:subscribeNewsletter:container:label", "Subscribe to Newsletter");
    }

    @Test
    void ajaxSubmitShowsInlineValidationErrors() {
        tester.startPage(EventRegistrationPage.class);

        FormTester formTester = tester.newFormTester("registrationForm");
        formTester.setValue("fullName:container:field", "");
        tester.executeAjaxEvent("registrationForm:submit", "click");

        tester.assertComponentOnAjaxResponse("registrationForm");
        assertThat(tester.getLastResponseAsString())
                .contains("data-field=\"error\"")
                .contains("&#039;Full Name&#039; is required.");
    }

    @Test
    void ajaxSubmitUpdatesTheBeanThroughTheCompoundPropertyModel() {
        tester.startPage(EventRegistrationPage.class);

        FormTester formTester = tester.newFormTester("registrationForm");
        formTester.setValue("fullName:container:field", "Ada Lovelace");
        formTester.setValue("email:container:field", "ada@example.com");
        tester.executeAjaxEvent("registrationForm:submit", "click");

        tester.assertNoErrorMessage();
        assertThat(tester.getLastResponseAsString()).contains("Registration successful for Ada Lovelace!");
    }
}

package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

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
        tester.assertLabel("registrationForm:fullName:label", "Full Name");
        tester.assertLabel("registrationForm:subscribeNewsletter:label", "Subscribe to Newsletter");
    }

    @Test
    void ajaxSubmitShowsInlineValidationErrors() {
        tester.startPage(EventRegistrationPage.class);

        FormTester formTester = tester.newFormTester("registrationForm");
        formTester.setValue("fullName:input", "");
        tester.executeAjaxEvent("registrationForm:submit", "click");

        // The field and the form's feedback panel show the errors without the form being re-rendered
        tester.assertComponentOnAjaxResponse("registrationForm:fullName:feedback");
        tester.assertComponentOnAjaxResponse("registrationForm:formFeedback");
        assertThat(tester.getLastResponseAsString())
                .contains("&#039;Full Name&#039; is required.")
                .contains("Please fix the errors in the form.")
                .doesNotContain("ot.toast(");
    }

    @Test
    void ajaxSubmitUpdatesTheBeanThroughTheCompoundPropertyModel() {
        tester.startPage(EventRegistrationPage.class);

        FormTester formTester = tester.newFormTester("registrationForm");
        formTester.setValue("fullName:input", "Ada Lovelace");
        formTester.setValue("email:input", "ada@example.com");
        tester.executeAjaxEvent("registrationForm:submit", "click");

        tester.assertNoErrorMessage();
        assertThat(tester.getLastResponseAsString()).contains("ot.toast(\"Registration successful for Ada Lovelace!\"");
    }
}

package dev.jbaby.wicket.oat.components;

import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OatAlertTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void testOatAlertRenders() {
        OatAlert alert = new OatAlert("alert", "Test Message");
        tester.startComponentInPage(alert);
        tester.assertLabel("alert", "Test Message");
        TagTester tag = tester.getTagByWicketId("alert");
        assertThat(tag.getAttribute("role")).isEqualTo("alert");
    }

    @Test
    void testOatAlertRendersHtml() {
        OatAlert alert = new OatAlert("alert", "<strong>Html</strong>");
        tester.startComponentInPage(alert);
        // assertLabel checks the model object, but we want to check the response for HTML
        assertThat(tester.getLastResponseAsString()).contains("<strong>Html</strong>");
    }
}

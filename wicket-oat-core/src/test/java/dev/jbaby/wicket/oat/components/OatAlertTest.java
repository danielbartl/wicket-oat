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
    void testOatAlertEscapesHtmlByDefault() {
        OatAlert alert = new OatAlert("alert", "<strong>Html</strong>");
        tester.startComponentInPage(alert);
        // The message must be escaped by default - only an explicit opt-in should
        // ever render raw HTML, since messages can come from user-controlled data.
        assertThat(tester.getLastResponseAsString()).doesNotContain("<strong>Html</strong>");
        assertThat(tester.getLastResponseAsString()).contains("&lt;strong&gt;Html&lt;/strong&gt;");
    }

    @Test
    void testOatAlertRendersRawHtmlWhenOptedIn() {
        OatAlert alert = new OatAlert("alert", "<strong>Html</strong>");
        alert.setEscapeModelStrings(false);
        tester.startComponentInPage(alert);
        assertThat(tester.getLastResponseAsString()).contains("<strong>Html</strong>");
    }
}

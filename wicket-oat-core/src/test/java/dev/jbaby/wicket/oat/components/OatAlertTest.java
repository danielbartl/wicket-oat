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
        OatAlert alert = new OatAlert("alert");
        tester.startComponentInPage(alert);
        TagTester tag = tester.getTagByWicketId("alert");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttribute("role")).isEqualTo("alert");
    }
}

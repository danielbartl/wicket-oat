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
class HomePageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testHomePageRenders() {
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
        
        tester.assertLabel("message", "Wicket is running!");
        tester.assertComponent("now", org.apache.wicket.markup.html.basic.Label.class);
        tester.assertComponent("timeForm", org.apache.wicket.markup.html.form.Form.class);
        tester.assertComponent("timeForm:refreshTime", org.apache.wicket.ajax.markup.html.form.AjaxButton.class);
    }

    @Test
    void testRefreshTimeUpdatesLabel() {
        tester.startPage(HomePage.class);
        String initialTime = tester.getComponentFromLastRenderedPage("now").getDefaultModelObjectAsString();
        
        tester.executeAjaxEvent("timeForm:refreshTime", "click");
        
        String updatedTime = tester.getComponentFromLastRenderedPage("now").getDefaultModelObjectAsString();
        
        assert(updatedTime != null);
    }
}

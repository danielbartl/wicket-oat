package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.TooltipBehavior;
import dev.jbaby.wicket.oat.components.OatAvatar;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ComponentsPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testComponentsPageRenders() {
        tester.startPage(ComponentsPage.class);
        tester.assertRenderedPage(ComponentsPage.class);
        
        tester.assertComponent("tooltipButton", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertBehavior("tooltipButton", TooltipBehavior.class);
        tester.assertBehavior("tooltipButton", ButtonBehavior.class);

        tester.assertComponent("successToast", org.apache.wicket.ajax.markup.html.AjaxLink.class);
        tester.assertComponent("errorToast", org.apache.wicket.ajax.markup.html.AjaxLink.class);
        
        tester.assertComponent("typography", org.apache.wicket.markup.html.WebMarkupContainer.class);
        
        tester.assertComponent("avatar1", OatAvatar.class);
        tester.assertComponent("avatar2", OatAvatar.class);
        tester.assertComponent("avatar3", OatAvatar.class);
    }
}

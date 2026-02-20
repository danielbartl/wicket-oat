package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import dev.jbaby.wicket.oat.components.ButtonBehavior;
import dev.jbaby.wicket.oat.components.ClientSideClickBehavior;
import dev.jbaby.wicket.oat.components.TooltipBehavior;
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
        
        // Assert the presence of various components
        tester.assertComponent("tooltipButton", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertBehavior("tooltipButton", TooltipBehavior.class);
        tester.assertBehavior("tooltipButton", ButtonBehavior.class);

        tester.assertComponent("openDialog", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertBehavior("openDialog", ClientSideClickBehavior.class);

        tester.assertComponent("dialog", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertLabel("dialog:dialogTitle", "Confirmation");
        tester.assertComponent("dialog:closeDialog1", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertComponent("dialog:closeDialog2", org.apache.wicket.markup.html.WebMarkupContainer.class);

        tester.assertComponent("tabs", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertComponent("successToast", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertComponent("errorToast", org.apache.wicket.markup.html.WebMarkupContainer.class);
        tester.assertComponent("typography", org.apache.wicket.markup.html.WebMarkupContainer.class);
    }
}

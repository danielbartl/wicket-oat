package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CardPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testCardPageRenders() {
        tester.startPage(CardPage.class);
        tester.assertRenderedPage(CardPage.class);

        // Check card container
        TagTester card = tester.getTagByWicketId("simpleCard");
        assertTrue(card.getAttribute("class").contains("card"));

        // Check components inside card
        tester.assertLabel("simpleCard:title", "Project Alpha");
        tester.assertLabel("simpleCard:description", "A groundbreaking initiative to redefine UI development.");
        
        TagTester saveBtn = tester.getTagByWicketId("save");
        assertTrue(saveBtn.getAttribute("class").contains("button"));
    }
}

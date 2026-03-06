package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatSession;
import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.Session;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OatThemeSwitcherTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new MockApplication() {
            @Override
            public Session newSession(Request request, Response response) {
                return new OatSession(request);
            }
        });
    }

    @Test
    void testOatThemeSwitcherToggle() {
        OatThemeSwitcher switcher = new OatThemeSwitcher("switcher");
        tester.startComponentInPage(switcher);
        
        OatSession session = OatSession.get();
        session.setTheme(OatTheme.LIGHT);
        
        // Click the dark theme link
        tester.clickLink("switcher:darkTheme");
        assertThat(session.getTheme()).isEqualTo(OatTheme.DARK);
        
        // Click the light theme link
        tester.clickLink("switcher:lightTheme");
        assertThat(session.getTheme()).isEqualTo(OatTheme.LIGHT);
    }
}

package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatSession;
import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BehaviorsCoverageTest {
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
    void testClientSideClickBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        ClientSideClickBehavior behavior = new ClientSideClickBehavior("alert('clicked')");
        container.add(behavior);
        
        tester.startComponentInPage(container, Markup.of("<div wicket:id=\"id\"></div>"));
        
        // Verify behavior is present
        assertThat(container.getBehaviors(ClientSideClickBehavior.class)).hasSize(1);
        // Verify outputMarkupId was set (required for JS selection)
        assertThat(container.getOutputMarkupId()).isTrue();
    }

    @Test
    void testClientSideClickBehaviorRejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> new ClientSideClickBehavior(null));
    }

    @Test
    void testFieldBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new FieldBehavior());
        tester.startComponentInPage(container, Markup.of("<div wicket:id=\"id\"></div>"));
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("data-field")).isNotNull();
    }

    @Test
    void testHintBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new HintBehavior());
        tester.startComponentInPage(container, Markup.of("<div wicket:id=\"id\"></div>"));
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("data-hint")).isNotNull();
    }

    @Test
    void testOatThemeBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new OatThemeBehavior());
        OatSession.get().setTheme(OatTheme.NORD);
        tester.startComponentInPage(container, Markup.of("<div wicket:id=\"id\"></div>"));
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("data-theme")).isEqualTo("nord");
    }

    @Test
    void testSwitchBehavior() {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(new SwitchBehavior());
        tester.startComponentInPage(container, Markup.of("<div wicket:id=\"id\"></div>"));
        TagTester tag = tester.getTagByWicketId("id");
        assertThat(tag.getAttribute("role")).isEqualTo("switch");
    }

    @Test
    void testOatToastBehavior() {
        IPartialPageRequestHandler handler = mock(IPartialPageRequestHandler.class);
        OatToastBehavior.toast(handler, "Message", OatToastBehavior.Variant.SUCCESS, "Title");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(handler).appendJavaScript(captor.capture());

        assertThat(captor.getValue()).contains("ot.toast('Message', 'Title', {variant: 'success'})");
    }

    @Test
    void testOatToastBehaviorDanger() {
        IPartialPageRequestHandler handler = mock(IPartialPageRequestHandler.class);
        OatToastBehavior.toast(handler, "Message", OatToastBehavior.Variant.DANGER);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(handler).appendJavaScript(captor.capture());

        assertThat(captor.getValue()).contains("variant: 'danger'");
    }

    @Test
    void testOatToastBehaviorDefaultOmitsVariant() {
        IPartialPageRequestHandler handler = mock(IPartialPageRequestHandler.class);
        OatToastBehavior.toast(handler, "Message");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(handler).appendJavaScript(captor.capture());

        assertThat(captor.getValue()).doesNotContain("variant:");
    }
}

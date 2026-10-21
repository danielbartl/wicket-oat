package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class OatDrawerTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.US);
    }

    /** A filter drawer with a required field and a trigger button, and a link opening it from the server. */
    public static class DrawerPage extends WebPage implements IMarkupResourceStreamProvider {

        public final Model<String> customer = Model.of("ACME");
        public final OatDrawer drawer;
        public int applied;

        public DrawerPage() {
            drawer = new OatDrawer("drawer", "Filters", "Filter orders") {
                @Override
                protected void onConfirm(AjaxRequestTarget target) {
                    applied++;
                }
            };
            Fragment fields = new Fragment(OatDialog.CONTENT_ID, "fields", this);
            fields.add(new OatTextField<String>("customer", "Customer", customer).setRequired(true));
            drawer.setContent(fields);
            add(drawer);
            add(new AjaxLink<Void>("open") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    drawer.open(target);
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><div wicket:id='drawer'></div><a wicket:id='open'>Filters</a>"
                    + "<wicket:fragment wicket:id='fields'><div wicket:id='customer'></div></wicket:fragment></body></html>");
        }
    }

    private TagTester dialogTag() {
        return TagTester.createTagByName(tester.getLastResponseAsString(), "dialog");
    }

    @Test
    void itIsADialogAtTheEndOfTheScreenWithAnXInItsCorner() {
        tester.startPage(new DrawerPage());

        TagTester dialog = dialogTag();
        assertThat(dialog.getAttribute("class")).isEqualTo("oat-drawer");
        assertThat(dialog.getAttribute("data-side")).isEqualTo("end");
        TagTester close = tester.getTagByWicketId("close");
        assertThat(close.getAttribute("command")).isEqualTo("close");
        assertThat(close.getAttribute("commandfor")).isEqualTo(dialog.getAttribute("id"));
        assertThat(close.getAttribute("aria-label")).isEqualTo("Close");
        // The trigger opens it in the browser
        assertThat(tester.getTagByWicketId("trigger").getAttribute("commandfor")).isEqualTo(dialog.getAttribute("id"));
    }

    @Test
    void itCanSlideInFromTheStartOrTheBottom() {
        DrawerPage page = new DrawerPage();
        page.drawer.setSide(OatDrawer.Side.START);
        tester.startPage(page);
        assertThat(dialogTag().getAttribute("data-side")).isEqualTo("start");

        page.drawer.setSide(OatDrawer.Side.BOTTOM);
        tester.startPage(page);
        assertThat(dialogTag().getAttribute("data-side")).isEqualTo("bottom");
    }

    @Test
    void itOpensAndConfirmsAsADialogDoes() {
        DrawerPage page = tester.startPage(new DrawerPage());
        tester.clickLink("open");
        assertThat(page.drawer.isOpen()).isTrue();

        FormTester form = tester.newFormTester("drawer:dialog:form");
        form.setValue("content:customer:input", "");
        tester.executeAjaxEvent("drawer:dialog:form:confirm", "click");
        assertThat(page.applied).isZero();
        assertThat(page.drawer.isOpen()).isTrue();

        form = tester.newFormTester("drawer:dialog:form");
        form.setValue("content:customer:input", "Initech");
        tester.executeAjaxEvent("drawer:dialog:form:confirm", "click");
        assertThat(page.applied).isEqualTo(1);
        assertThat(page.customer.getObject()).isEqualTo("Initech");
        assertThat(page.drawer.isOpen()).isFalse();
    }

    @Test
    void theXClosesItInTheBrowserWhichTellsTheServer() {
        DrawerPage page = tester.startPage(new DrawerPage());
        tester.clickLink("open");

        tester.executeAjaxEvent("drawer:dialog", "close");

        assertThat(page.drawer.isOpen()).isFalse();
    }

    @Test
    void aDrawerThatOnlyShowsSomethingLeavesOutCancelAndConfirm() {
        DrawerPage page = new DrawerPage();
        page.drawer.setFooterVisible(false);
        tester.startPage(page);

        tester.assertInvisible("drawer:dialog:form:cancel");
        tester.assertInvisible("drawer:dialog:form:confirm");
        tester.assertVisible("drawer:dialog:form:close");
    }

    @Test
    void aDialogHasNoXUnlessAskedFor() {
        OatDialog dialog = new OatDialog("dialog", "Delete");
        dialog.setContent(new Label(OatDialog.CONTENT_ID, "Sure?"));
        tester.startComponentInPage(dialog);
        tester.assertInvisible("dialog:dialog:form:close");

        dialog.setCloseButton(true);
        tester.startComponentInPage(dialog);
        tester.assertVisible("dialog:dialog:form:close");
        assertThat(dialogTag().getAttribute("class")).isNull();
    }

    @Test
    void theXIsNamedInTheUsersLanguage() {
        tester.getSession().setLocale(Locale.GERMAN);
        tester.startPage(new DrawerPage());

        assertThat(tester.getTagByWicketId("close").getAttribute("aria-label")).isEqualTo("Schließen");
    }
}

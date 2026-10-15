package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.components.OatLoginForm;
import dev.jbaby.wicket.oat.components.OatSubmitButton;
import dev.jbaby.wicket.oat.components.OatWizard;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class NestedFormButtonBehaviorTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    /** A page form with a form nested in it, and a form of its own beside them. */
    public static class FormsPage extends WebPage implements IMarkupResourceStreamProvider {

        public final List<String> saved = new ArrayList<>();

        public FormsPage() {
            Form<Void> outer = new Form<>("outer");
            add(outer);
            Form<Void> inner = new Form<>("inner");
            outer.add(inner);
            inner.add(OatSubmitButton.onSubmit("innerSave", "Save", target -> saved.add("inner")));
            outer.add(OatSubmitButton.onSubmit("outerSave", "Save", target -> saved.add("outer")));
            Form<Void> own = new Form<>("own");
            add(own);
            own.add(OatSubmitButton.onSubmit("ownSave", "Save", target -> saved.add("own")));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body>"
                    + "<form wicket:id='outer'><form wicket:id='inner'><button wicket:id='innerSave' type='submit'></button></form>"
                    + "<button wicket:id='outerSave' type='submit'></button></form>"
                    + "<form wicket:id='own'><button wicket:id='ownSave' type='submit'></button></form>"
                    + "</body></html>");
        }
    }

    @Test
    void aButtonOfANestedFormIsNoSubmitButtonOfThePageForm() {
        FormsPage page = tester.startPage(FormsPage.class);

        // Else Enter in a field of the outer form could click it, as that form's first submit button
        assertThat(tester.getTagByWicketId("innerSave").getAttribute("type")).isEqualTo("button");
        assertThat(tester.getTagByWicketId("outerSave").getAttribute("type")).isEqualTo("submit");
        assertThat(tester.getTagByWicketId("ownSave").getAttribute("type")).isEqualTo("submit");

        // It still submits its own form over Ajax
        tester.executeAjaxEvent("outer:inner:innerSave", "click");
        assertThat(page.saved).containsExactly("inner");
    }

    @Test
    void theLibrarysOtherFormButtonsHaveIt() {
        OatWizard wizard = new OatWizard("wizard") {
            @Override
            protected void onFinish(AjaxRequestTarget target) {
            }
        }.addStep("One", Label::new).addStep("Two", Label::new);
        assertThat(wizard.get("form:next").getBehaviors(NestedFormButtonBehavior.class)).hasSize(1);
        assertThat(wizard.get("form:finish").getBehaviors(NestedFormButtonBehavior.class)).hasSize(1);

        // Enter in its fields signs in also where the login form is nested in a page's form
        OatLoginForm login = new OatLoginForm("login") {
            @Override
            protected boolean signIn(String username, String password, boolean rememberMe) {
                return false;
            }
        };
        assertThat(((Form<?>) login.get("form")).getDefaultButton()).isSameAs(login.get("form:signIn"));
    }

    @Test
    void itIsOnlyForButtons() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Label("label").add(new NestedFormButtonBehavior()));
    }
}

package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.form.OatCustomField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TierAComponentsTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.US);
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), attribute, value, false);
    }

    // --- Login form ---

    public static class LoginPage extends WebPage implements IMarkupResourceStreamProvider {
        public final List<String> attempts = new ArrayList<>();
        public boolean signedIn;
        public final OatLoginForm login;

        public LoginPage() {
            login = new OatLoginForm("login") {
                @Override
                protected boolean signIn(String username, String password, boolean rememberMe) {
                    attempts.add(username + "/" + password + "/" + rememberMe);
                    return "ada".equals(username) && "secret".equals(password);
                }

                @Override
                protected void onSignedIn(AjaxRequestTarget target) {
                    signedIn = true;
                }
            };
            add(login);
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><div wicket:id='login'></div></body></html>");
        }
    }

    private void signIn(String username, String password) {
        FormTester form = tester.newFormTester("login:form");
        form.setValue("username:container:field", username);
        form.setValue("password:container:field", password);
        tester.executeAjaxEvent("login:form:signIn", "click");
    }

    @Test
    void loginFormHelpsPasswordManagers() {
        tester.startPage(LoginPage.class);
        List<TagTester> inputs = tags("wicket:id", "field");
        assertThat(inputs.get(0).getAttribute("autocomplete")).isEqualTo("username");
        assertThat(inputs.get(1).getAttribute("type")).isEqualTo("password");
        assertThat(inputs.get(1).getAttribute("autocomplete")).isEqualTo("current-password");
        tester.assertInvisible("login:form:failed");
        tester.assertInvisible("login:form:rememberMe");
        tester.assertInvisible("login:form:forgot");
        assertThat(tester.getLastResponseAsString()).contains(">Sign in</button>");
    }

    @Test
    void aFailedSignInSaysSoWithoutDetailsAndClearsThePassword() {
        LoginPage page = tester.startPage(LoginPage.class);
        signIn("ada", "wrong");

        assertThat(page.attempts).containsExactly("ada/wrong/false");
        assertThat(page.signedIn).isFalse();
        assertThat(tester.getTagByWicketId("failed").getValue()).isEqualTo("Wrong username or password.");
        assertThat(tester.getTagByWicketId("failed").getAttribute("role")).isEqualTo("alert");
        assertThat(tags("wicket:id", "field").get(1).getAttribute("value")).isNullOrEmpty();
    }

    @Test
    void aSuccessfulSignInContinues() {
        LoginPage page = tester.startPage(LoginPage.class);
        signIn("ada", "secret");
        assertThat(page.signedIn).isTrue();
    }

    @Test
    void emptyCredentialsAreNotTried() {
        LoginPage page = tester.startPage(LoginPage.class);
        signIn("", "");
        assertThat(page.attempts).isEmpty();
        assertThat(tester.getLastResponseAsString()).contains("data-field=\"error\"");
    }

    @Test
    void loginFormOffersRememberMeAndAForgotLink() {
        LoginPage page = new LoginPage();
        page.login.setRememberMe(true).setForgotPasswordLink(LayoutAndNavigationTestPages.Home.class);
        tester.startPage(page);
        assertThat(tester.getTagByWicketId("forgot").getValue()).contains("Forgot your password?");

        FormTester form = tester.newFormTester("login:form");
        form.setValue("rememberMe:container:field", true);
        signIn("ada", "secret");
        assertThat(page.attempts).containsExactly("ada/secret/true");
    }

    @Test
    void loginFactoryUsesTheCallback() {
        tester.startComponentInPage(Oat.Components.loginForm("login", (user, password) -> true));
        tester.assertComponent("login", OatLoginForm.class);
    }

    // --- Master-detail ---

    public static class MasterDetailPage extends WebPage implements IMarkupResourceStreamProvider {
        public final OatMasterDetail<String> customers = new OatMasterDetail<>("customers", new Model<>());

        public MasterDetailPage() {
            add(customers);
            customers.setMaster(id -> {
                Fragment list = new Fragment(id, "list", this);
                list.add(new ListView<>("items", List.of("ACME", "Globex")) {
                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new AjaxLink<Void>("link") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                customers.select(target, item.getModelObject());
                            }
                        }.setBody(item.getModel()));
                    }
                });
                return list;
            });
            customers.setDetail((id, customer) -> new Label(id, () -> "Details of " + customer.getObject()));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><div wicket:id='customers'></div>"
                    + "<wicket:fragment wicket:id='list'><ul><li wicket:id='items'><a wicket:id='link'></a></li></ul></wicket:fragment>"
                    + "</body></html>");
        }
    }

    @Test
    void masterDetailShowsTheSelectedItem() {
        MasterDetailPage page = tester.startPage(MasterDetailPage.class);
        TagTester root = tester.getTagByWicketId("customers");
        assertThat(root.getAttribute("class")).isEqualTo("oat-master-detail");
        assertThat(root.getAttribute("data-showing")).isEqualTo("master");
        assertThat(tester.getLastResponseAsString()).contains("Select an item to see its details.");

        tester.clickLink("customers:master:items:1:link");
        assertThat(page.customers.getSelection().getObject()).isEqualTo("Globex");
        assertThat(page.customers.isSelected("Globex")).isTrue();
        assertThat(tester.getLastResponseAsString()).contains("Details of Globex").contains("dataset.showing='detail'");

        tester.clickLink("customers:detailArea:back");
        assertThat(tester.getLastResponseAsString()).contains("dataset.showing='master'");
        assertThat(page.customers.getSelection().getObject()).isEqualTo("Globex");
    }

    @Test
    void masterDetailIsValidated() {
        OatMasterDetail<String> md = new OatMasterDetail<>("md", new Model<>());
        assertThatIllegalArgumentException().isThrownBy(() -> md.setMaster(id -> new Label("other")));
        md.getSelection().setObject("x");
        assertThatIllegalArgumentException().isThrownBy(() -> md.setDetail((id, item) -> new Label("other")));
    }

    // --- Messages ---

    record Comment(String author, String text, LocalDateTime at) implements Serializable {
    }

    @Test
    void messageListShowsAuthorTimeAndText() {
        List<Comment> comments = List.of(
                new Comment("Jordan Lee", "Sent the invoice.\nCall back on Monday.", LocalDateTime.of(2026, 10, 9, 14, 5)),
                new Comment("ada", "<b>Done</b>", null));
        tester.startComponentInPage(Oat.Components.messageList("comments", Model.ofList(comments),
                Comment::author, Comment::text, Comment::at));

        assertThat(tags("wicket:id", "initials")).extracting(TagTester::getValue).containsExactly("JL", "A");
        assertThat(tags("wicket:id", "author")).extracting(TagTester::getValue).containsExactly("Jordan Lee", "ada");
        TagTester time = tester.getTagByWicketId("time");
        assertThat(time.getAttribute("datetime")).isEqualTo("2026-10-09T14:05");
        assertThat(tags("wicket:id", "time")).hasSize(1);
        assertThat(tags("wicket:id", "text")).extracting(TagTester::getValue)
                .containsExactly("Sent the invoice.\nCall back on Monday.", "&lt;b&gt;Done&lt;/b&gt;");
    }

    @Test
    void messageListCanShowAvatarImages() {
        tester.startComponentInPage(new OatMessageList<>("comments", Model.ofList(List.of(new Comment("Ada", "Hi", null))),
                Comment::author, Comment::text, Comment::at).setAvatar(c -> "/avatars/ada.png"));
        assertThat(tester.getTagByWicketId("img").getAttribute("src")).isEqualTo("/avatars/ada.png");
    }

    @Test
    void messageInputSendsTheTextAndEmptiesTheBox() {
        List<String> sent = new ArrayList<>();
        tester.startComponentInPage(Oat.Components.messageInput("reply", (target, text) -> sent.add(text)));
        assertThat(tester.getTagByWicketId("text").getAttribute("aria-label")).isEqualTo("Message");

        FormTester form = tester.newFormTester("reply:form");
        form.setValue("text", "  Thanks!  ");
        tester.executeAjaxEvent("reply:form:send", "click");
        assertThat(sent).containsExactly("Thanks!");
        assertThat(tester.getTagByWicketId("text").getValue()).isEmpty();

        form = tester.newFormTester("reply:form");
        form.setValue("text", "   ");
        tester.executeAjaxEvent("reply:form:send", "click");
        assertThat(sent).containsExactly("Thanks!");
    }

    // --- Custom field ---

    /** A size as width x height, from two inputs. */
    static class SizeInput extends FormComponentPanel<String> {
        private final TextField<String> width = new TextField<>("width", new Model<>());
        private final TextField<String> height = new TextField<>("height", new Model<>());

        SizeInput(String id, IModel<String> model) {
            super(id, model);
            add(width, height);
        }

        @Override
        public void convertInput() {
            setConvertedInput(width.getConvertedInput() == null ? null : width.getConvertedInput() + "x" + height.getConvertedInput());
        }
    }

    @Test
    void customFieldWrapsAnInputOfYourOwn() {
        Model<String> size = new Model<>();
        OatCustomField<String> field = Oat.Components.customField("size", "Size", size, SizeInput::new);
        field.setRequired(true);
        Form<Void> form = new Form<>("form");
        form.add(field);
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='size'></div></form>"));

        assertThat(tester.getTagByWicketId("label").getName()).isEqualTo("legend");
        String feedbackId = tester.getTagByWicketId("feedback").getAttribute("id");
        assertThat(tester.getTagByWicketId("width").getAttribute("aria-describedby")).isEqualTo(feedbackId);
        assertThat(tester.getTagByWicketId("height").getAttribute("aria-invalid")).isEqualTo("false");

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("size:container:field:width", "3");
        formTester.setValue("size:container:field:height", "4");
        formTester.submit();
        assertThat(size.getObject()).isEqualTo("3x4");

        formTester = tester.newFormTester("form");
        formTester.setValue("size:container:field:width", "");
        formTester.setValue("size:container:field:height", "");
        formTester.submit();
        assertThat(tester.getTagByWicketId("width").getAttribute("aria-invalid")).isEqualTo("true");
        assertThat(tester.getTagByWicketId("container").getAttribute("data-field")).isEqualTo("error");
    }

    @Test
    void customFieldIsValidated() {
        OatCustomField<String> field = new OatCustomField<>("size", "Size", new Model<>(), (id, model) -> new TextField<>("other", model));
        assertThatIllegalArgumentException().isThrownBy(field::getField);
    }
}

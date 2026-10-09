package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.form.OatCheckBox;
import dev.jbaby.wicket.oat.components.form.OatPasswordField;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * A sign-in form: username, password, an optional "remember me" checkbox and "forgot
 * your password?" link, and a Sign in button. Implement {@link #signIn} to check the
 * credentials with your session or security framework:
 * <pre>{@code
 * add(new OatLoginForm("login") {
 *     @Override
 *     protected boolean signIn(String username, String password, boolean rememberMe) {
 *         return AuthenticatedWebSession.get().signIn(username, password);   // wicket-auth-roles
 *     }
 * });
 * }</pre>
 * or {@code Oat.Components.loginForm("login", (username, password) -> ...)}.
 * <p>
 * After a successful sign-in the session gets a new id (against session fixation) and
 * {@link #onSignedIn} continues to the page the user was going to, or else the home
 * page. A failed one shows the {@code OatLoginForm.failed} message ("Wrong username or
 * password.") without telling which of the two was wrong, and clears the password.
 * The fields carry {@code autocomplete} hints, so password managers fill them in. The
 * texts are the {@code OatLoginForm.*} resources; override them, e.g. to label the
 * username "Email".
 */
public abstract class OatLoginForm extends Panel {

    private final IModel<String> username = new Model<>();
    private final IModel<String> password = new Model<>();
    private final IModel<Boolean> rememberMe = Model.of(false);
    private final Form<Void> form;
    private final OatCheckBox rememberMeField;
    private boolean failed;

    public OatLoginForm(String id) {
        super(id);

        form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);

        form.add(new OatAlert("failed", new StringResourceModel("OatLoginForm.failed", this)
                .setDefaultValue("Wrong username or password."), OatVariant.DANGER) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(failed);
            }
        });

        OatTextField<String> usernameField = new OatTextField<String>("username",
                new StringResourceModel("OatLoginForm.username", this).setDefaultValue("Username"), username)
                .setRequired(true);
        usernameField.getField().add(AttributeModifier.replace("autocomplete", "username"),
                AttributeModifier.replace("autocapitalize", "none"),
                AttributeModifier.replace("spellcheck", "false"));
        form.add(usernameField);

        OatPasswordField passwordField = new OatPasswordField("password",
                new StringResourceModel("OatLoginForm.password", this).setDefaultValue("Password"), password)
                .setRequired(true);
        passwordField.getField().add(AttributeModifier.replace("autocomplete", "current-password"));
        form.add(passwordField);

        rememberMeField = new OatCheckBox("rememberMe",
                new StringResourceModel("OatLoginForm.rememberMe", this).setDefaultValue("Remember me"), rememberMe);
        rememberMeField.setVisible(false);
        form.add(rememberMeField);
        form.add(new WebMarkupContainer("forgot").setVisible(false));

        form.add(new OatSubmitButton("signIn", new StringResourceModel("OatLoginForm.signIn", this).setDefaultValue("Sign in")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (signIn(username.getObject(), password.getObject(), Boolean.TRUE.equals(rememberMe.getObject()))) {
                    failed = false;
                    // A new session id once signed in, so an id planted before can't be used
                    getSession().replaceSession();
                    onSignedIn(target);
                } else {
                    failed = true;
                    password.setObject(null);
                    target.add(form);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(form);
            }
        }.setBusyIndicator(true));
    }

    /**
     * Checks the credentials and signs the user in.
     *
     * @return whether they were right
     */
    protected abstract boolean signIn(String username, String password, boolean rememberMe);

    /**
     * Called after a successful sign-in. By default continues to the page the user was
     * sent here from, or else the application's home page.
     */
    protected void onSignedIn(AjaxRequestTarget target) {
        continueToOriginalDestination();
        setResponsePage(getApplication().getHomePage());
    }

    /** Shows a "Remember me" checkbox; its value is passed to {@link #signIn}. */
    public OatLoginForm setRememberMe(boolean shown) {
        rememberMeField.setVisible(shown);
        return this;
    }

    /** Shows a "Forgot your password?" link to the given page. */
    public OatLoginForm setForgotPasswordLink(Class<? extends Page> page) {
        BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("forgot", page);
        link.add(new Label("label", new StringResourceModel("OatLoginForm.forgotPassword", this)
                .setDefaultValue("Forgot your password?")));
        form.addOrReplace(link);
        return this;
    }

    /** The form, e.g. to add fields of your own. */
    public Form<Void> getForm() {
        return form;
    }
}

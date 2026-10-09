package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.OatAlert;
import dev.jbaby.wicket.oat.components.OatLoginForm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * An OatLoginForm. The demo accepts "demo" / "demo" and then shows that it worked; a
 * real application would check the credentials in signIn, e.g. with
 * AuthenticatedWebSession.get().signIn(...), and keep the default onSignedIn, which
 * continues to the page the user came from.
 */
public class SignInPage extends BasePage {

    public SignInPage() {
        WebMarkupContainer card = new WebMarkupContainer("card");
        card.setOutputMarkupId(true);
        add(card);

        OatAlert signedIn = new OatAlert("signedIn", "Signed in as demo. A real application would now continue "
                + "to the page you came from.", OatVariant.SUCCESS);
        signedIn.setVisible(false);
        card.add(signedIn);

        card.add(new OatLoginForm("login") {
            @Override
            protected boolean signIn(String username, String password, boolean rememberMe) {
                return "demo".equals(username) && "demo".equals(password);
            }

            @Override
            protected void onSignedIn(AjaxRequestTarget target) {
                setVisible(false);
                signedIn.setVisible(true);
                target.add(card);
            }
        }.setRememberMe(true).setForgotPasswordLink(HomePage.class));
    }
}

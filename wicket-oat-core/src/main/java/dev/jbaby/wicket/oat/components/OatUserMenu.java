package dev.jbaby.wicket.oat.components;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy.RequestType;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy.ResourceIsolationOutcome;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The signed-in user's menu, for the top bar of an {@link OatAppLayout}: their picture,
 * or initials, and name open a menu with their name and a second line such as their
 * email, links of yours, and "Sign out". While nobody is signed in it is a "Sign in"
 * link instead, or nothing.
 * <pre>{@code
 * @Override
 * protected Component newTopNavExtra(String id) {
 *     OatUserMenu user = new OatUserMenu(id, () -> MySession.get().getUserName()); // null while signed out
 *     user.setDetail(() -> MySession.get().getEmail());
 *     user.addLink("Profile", ProfilePage.class).setIcon("user");
 *     user.addLink("Settings", SettingsPage.class).setIcon("settings");
 *     user.setSignInPage(SignInPage.class);
 *     return user;
 * }
 * }</pre>
 * "Sign out" invalidates the session and then shows the home page; override
 * {@link #signOut()} and {@link #onSignedOut()} to do otherwise, e.g. to sign out of
 * Spring Security too. It is a POST, which is refused with 403 Forbidden when the browser
 * says it comes from another site's page ({@code Sec-Fetch-Site}, which current browsers
 * send), so another site can't sign the user out; Wicket's
 * {@code ResourceIsolationRequestCycleListener} does the same for all of an application's
 * requests. It works on a page that is still open after the session expired. The menu
 * opens in the browser and its links and
 * Sign out form are stateless, so it keeps a page stateless, such as a sign-in page,
 * where the "Sign in" link hides itself.
 * <p>
 * Someone is signed in while the name isn't {@code null}; override {@link #isSignedIn()}
 * for a user without a name. The name and the other models are read once per request.
 * <p>
 * Screen readers name the button "Account: " and the name. On phones it shows only the
 * picture. The texts are the {@code OatUserMenu.*} resources. It renders its own tag with
 * a markup id, so {@code target.add(userMenu)} updates it after a sign-in over Ajax.
 */
public class OatUserMenu extends Panel {

    /** Refuses a request the browser says comes from another site's page (Sec-Fetch-Site). */
    private static final IResourceIsolationPolicy FROM_ANOTHER_SITE = new FetchMetadataResourceIsolationPolicy();

    private final IModel<String> name;
    private final List<MenuLink> links = new ArrayList<>();
    private IModel<String> detail;
    private IModel<String> picture;

    /**
     * @param name the signed-in user's name, or a model of {@code null} while nobody is
     *             signed in; read on every request
     */
    public OatUserMenu(String id, IModel<String> name) {
        super(id);
        this.name = OncePerRequestModel.of(Objects.requireNonNull(name));
        setOutputMarkupId(true);

        WebMarkupContainer dropdown = new WebMarkupContainer("dropdown") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isSignedIn());
            }
        };
        add(dropdown);

        WebMarkupContainer menu = new WebMarkupContainer("menu");
        menu.setOutputMarkupId(true);
        dropdown.add(menu);

        WebMarkupContainer trigger = new WebMarkupContainer("trigger");
        trigger.add(AttributeModifier.replace("popovertarget", (IModel<String>) menu::getMarkupId));
        trigger.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatUserMenu.account", this)
                .setParameters((IModel<String>) () -> Objects.toString(this.name.getObject(), "")).setDefaultValue("Account: {0}")));
        trigger.add(new OatAvatar("avatar", this::getPicture, () -> OatAvatar.initialsOf(this.name.getObject()), OatAvatar.Size.SMALL));
        trigger.add(new Label("name", this.name));
        dropdown.add(trigger);

        menu.add(new Label("menuName", this.name));
        menu.add(new Label("detail", (IModel<String>) this::getDetail) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDetail() != null);
            }
        });
        menu.add(new ListView<>("links", Model.ofList(links)) {
            @Override
            protected void populateItem(ListItem<MenuLink> item) {
                MenuLink link = item.getModelObject();
                BookmarkablePageLink<Void> anchor = new BookmarkablePageLink<>("link", link.page, link.parameters);
                if (CurrentPage.isLinkedBy(getPage(), link.page, link.parameters)) {
                    anchor.add(AttributeModifier.replace("aria-current", "page"));
                }
                anchor.add(OatIcon.ofNullable("icon", link.icon));
                anchor.add(new Label("label", link.label));
                item.add(anchor);
            }
        });
        menu.add(new WebMarkupContainer("linksEnd") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!links.isEmpty());
            }
        });

        StatelessForm<Void> signOut = new StatelessForm<>("signOut") {
            // Also on a page built again after the session expired, which hides the menu
            @Override
            public boolean canCallListener() {
                return true;
            }

            // Instead of processing: the form has no fields, and processing skips a hidden form
            @Override
            public void process(IFormSubmitter submitter) {
                // Not for another site's page: browsers send the session cookie with its POST
                // unless the cookie is SameSite, which only Chromium assumes by default
                if (getRequest().getContainerRequest() instanceof HttpServletRequest request
                        && FROM_ANOTHER_SITE.isRequestAllowed(request, getPage(), RequestType.LISTENER) == ResourceIsolationOutcome.DISALLOWED) {
                    throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_FORBIDDEN, "Sign out from another site");
                }
                signOut();
                onSignedOut();
            }
        };
        signOut.add(OatIcon.ofNullable("icon", "log-out"));
        signOut.add(new Label("label", new StringResourceModel("OatUserMenu.signOut", this).setDefaultValue("Sign out")));
        menu.add(signOut);

        add(new WebMarkupContainer("signIn").setVisible(false));
    }

    /**
     * Whether someone is signed in, which shows the menu rather than the "Sign in" link.
     * By default while the name isn't {@code null}.
     */
    protected boolean isSignedIn() {
        return name.getObject() != null;
    }

    private String getDetail() {
        return detail != null ? detail.getObject() : null;
    }

    private String getPicture() {
        return picture != null ? picture.getObject() : null;
    }

    /** A second line under the name in the menu, such as the user's email. */
    public OatUserMenu setDetail(IModel<String> detail) {
        this.detail = OncePerRequestModel.of(detail);
        return this;
    }

    /** The URL of the user's picture; without one, or while it loads, the initials show. */
    public OatUserMenu setPicture(IModel<String> url) {
        this.picture = OncePerRequestModel.of(url);
        return this;
    }

    /**
     * Adds a link to a page to the menu, above "Sign out".
     *
     * @return the link, e.g. to {@link MenuLink#setIcon set its icon}
     */
    public MenuLink addLink(String label, Class<? extends Page> page) {
        return addLink(Model.of(label), page, null);
    }

    /**
     * Adds a link to a page to the menu, above "Sign out".
     *
     * @param parameters the page's parameters, or {@code null}
     * @return the link, e.g. to {@link MenuLink#setIcon set its icon}
     */
    public MenuLink addLink(IModel<String> label, Class<? extends Page> page, PageParameters parameters) {
        MenuLink link = new MenuLink(Objects.requireNonNull(label), Objects.requireNonNull(page), parameters);
        links.add(link);
        return link;
    }

    /**
     * Shows a "Sign in" link to this page while nobody is signed in, except on the page
     * itself or a subclass of it.
     */
    public OatUserMenu setSignInPage(Class<? extends Page> page) {
        Objects.requireNonNull(page);
        BookmarkablePageLink<Void> signIn = new BookmarkablePageLink<>("signIn", page) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!isSignedIn() && !page.isInstance(getPage()));
            }
        };
        signIn.setBody(new StringResourceModel("OatUserMenu.signIn", this).setDefaultValue("Sign in"));
        addOrReplace(signIn);
        return this;
    }

    /**
     * Signs the user out when they choose "Sign out". By default invalidates the session,
     * which signs out of wicket-auth-roles' {@code AuthenticatedWebSession} too. Wicket
     * invalidates it at the end of the request, so the session still has the user until
     * then.
     */
    protected void signOut() {
        getSession().invalidate();
    }

    /**
     * Called after {@link #signOut()}. By default shows the application's home page. Show
     * a page with {@code setResponsePage(SomePage.class)}, which Wicket shows in a new
     * request: a page rendered in this one would still see the user in the session.
     */
    protected void onSignedOut() {
        setResponsePage(getApplication().getHomePage());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        name.detach();
        if (detail != null) {
            detail.detach();
        }
        if (picture != null) {
            picture.detach();
        }
    }

    /** A link in the menu. */
    public static final class MenuLink implements Serializable {

        private final IModel<String> label;
        private final Class<? extends Page> page;
        private final PageParameters parameters;
        private String icon;

        MenuLink(IModel<String> label, Class<? extends Page> page, PageParameters parameters) {
            this.label = label;
            this.page = page;
            this.parameters = parameters;
        }

        /** An {@link OatIcon} before the label, e.g. {@code "settings"}; {@code null} for none. */
        public MenuLink setIcon(String icon) {
            this.icon = OatIcon.requireKnown(icon);
            return this;
        }
    }
}

package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
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
 * Spring Security too. The menu opens in the browser and its links are stateless, so it
 * keeps a page stateless, such as a sign-in page, where the "Sign in" link hides itself.
 * <p>
 * Screen readers name the button "Account: " and the name. On phones it shows only the
 * picture. The texts are the {@code OatUserMenu.*} resources. It renders its own tag with
 * a markup id, so {@code target.add(userMenu)} updates it after a sign-in over Ajax.
 */
public class OatUserMenu extends Panel {

    private final IModel<String> name;
    private final List<MenuLink> links = new ArrayList<>();
    private IModel<String> detail;
    private IModel<String> picture;

    /**
     * @param name the signed-in user's name, or a model of {@code null} while nobody is
     *             signed in; read on every render
     */
    public OatUserMenu(String id, IModel<String> name) {
        super(id);
        this.name = Objects.requireNonNull(name);
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
                .setParameters(name).setDefaultValue("Account: {0}")));
        trigger.add(new OatAvatar("avatar", this::getPicture, () -> OatAvatar.initialsOf(name.getObject()), OatAvatar.Size.SMALL));
        trigger.add(new Label("name", name));
        dropdown.add(trigger);

        menu.add(new Label("menuName", name));
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
                anchor.add(icon("icon", link.icon));
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

        StatelessLink<Void> signOut = new StatelessLink<>("signOut") {
            @Override
            public void onClick() {
                signOut();
                onSignedOut();
            }
        };
        signOut.add(icon("icon", "log-out"));
        signOut.add(new Label("label", new StringResourceModel("OatUserMenu.signOut", this).setDefaultValue("Sign out")));
        menu.add(signOut);

        add(new WebMarkupContainer("signIn").setVisible(false));
    }

    private static Component icon(String id, String name) {
        return name != null ? new OatIcon(id, name) : new WebMarkupContainer(id).setVisible(false);
    }

    private boolean isSignedIn() {
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
        this.detail = detail;
        return this;
    }

    /** The URL of the user's picture; without one, or while it loads, the initials show. */
    public OatUserMenu setPicture(IModel<String> url) {
        this.picture = url;
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
     * itself.
     */
    public OatUserMenu setSignInPage(Class<? extends Page> page) {
        Objects.requireNonNull(page);
        BookmarkablePageLink<Void> signIn = new BookmarkablePageLink<>("signIn", page) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!isSignedIn() && !getPage().getClass().equals(page));
            }
        };
        signIn.setBody(new StringResourceModel("OatUserMenu.signIn", this).setDefaultValue("Sign in"));
        addOrReplace(signIn);
        return this;
    }

    /**
     * Signs the user out when they choose "Sign out". By default invalidates the session,
     * which signs out of wicket-auth-roles' {@code AuthenticatedWebSession} too.
     */
    protected void signOut() {
        getSession().invalidate();
    }

    /** Called after {@link #signOut()}. By default shows the application's home page. */
    protected void onSignedOut() {
        setResponsePage(getApplication().getHomePage());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
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
            if (icon != null && !OatIcon.names().contains(icon)) {
                throw new IllegalArgumentException("No icon \"" + icon + "\"; the icons are " + OatIcon.names());
            }
            this.icon = icon;
            return this;
        }
    }
}

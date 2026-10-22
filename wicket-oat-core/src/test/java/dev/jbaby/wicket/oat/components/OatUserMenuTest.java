package dev.jbaby.wicket.oat.components;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OatUserMenuTest {

    /** The signed-in user's name, kept in the session as an application would. */
    static final MetaDataKey<String> USER = new MetaDataKey<>() {
    };

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    /** A page with the user menu; stateless, as a public page or a sign-in page is. */
    public static class MenuPage extends WebPage implements IMarkupResourceStreamProvider {

        public MenuPage() {
            OatUserMenu user = new OatUserMenu("user", () -> Session.get().getMetaData(USER));
            user.setDetail(Model.of("ada@example.com"));
            user.addLink("Profile", ProfilePage.class).setIcon("user");
            user.addLink("Settings", MenuPage.class);
            user.setSignInPage(SignInPage.class);
            add(user);
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><div wicket:id='user'></div></body></html>");
        }
    }

    public static class SignInPage extends MenuPage {
    }

    public static class ProfilePage extends MenuPage {
    }

    /** The application's own sign-in page, built on another one. */
    public static class ThemedSignInPage extends SignInPage {
    }

    private void signIn(String name) {
        tester.getSession().setMetaData(USER, name);
    }

    @Test
    void whileSignedOutItIsASignInLink() {
        tester.startPage(MenuPage.class);

        tester.assertInvisible("user:dropdown");
        TagTester signIn = tester.getTagByWicketId("signIn");
        assertThat(signIn.getValue()).isEqualTo("Sign in");
        assertThat(signIn.getAttribute("class")).isEqualTo("button outline small");
        tester.clickLink("user:signIn");
        tester.assertRenderedPage(SignInPage.class);
    }

    @Test
    void theSignInLinkHidesOnTheSignInPage() {
        tester.startPage(SignInPage.class);

        tester.assertInvisible("user:signIn");
        tester.assertInvisible("user:dropdown");

        tester.startPage(ThemedSignInPage.class);
        tester.assertInvisible("user:signIn");
    }

    @Test
    void withoutASignInPageItShowsNothingWhileSignedOut() {
        tester.startComponentInPage(new OatUserMenu("user", Model.of((String) null)));

        tester.assertInvisible("user:dropdown");
        tester.assertInvisible("user:signIn");
    }

    @Test
    void whileSignedInTheButtonShowsInitialsAndNameAndIsNamedForScreenReaders() {
        signIn("Ada Lovelace");
        tester.startPage(MenuPage.class);

        tester.assertInvisible("user:signIn");
        TagTester trigger = tester.getTagByWicketId("trigger");
        assertThat(trigger.getAttribute("aria-label")).isEqualTo("Account: Ada Lovelace");
        assertThat(trigger.getAttribute("popovertarget")).isEqualTo(tester.getTagByWicketId("menu").getAttribute("id"));
        assertThat(trigger.getAttribute("aria-haspopup")).isEqualTo("menu");
        assertThat(trigger.getAttribute("aria-expanded")).isEqualTo("false");
        assertThat(tester.getTagByWicketId("menu").getAttribute("role")).isEqualTo("menu");
        tester.assertLabel("user:dropdown:trigger:avatar:container:initials", "AL");
        tester.assertLabel("user:dropdown:trigger:name", "Ada Lovelace");
    }

    @Test
    void theMenuShowsTheNameTheDetailTheLinksAndSignOut() {
        signIn("Ada Lovelace");
        tester.startPage(ProfilePage.class);

        tester.assertLabel("user:dropdown:menu:menuName", "Ada Lovelace");
        tester.assertLabel("user:dropdown:menu:detail", "ada@example.com");
        List<TagTester> items = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "menuitem", false);
        assertThat(items).extracting(item -> item.getChild("wicket:id", "label").getValue())
                .containsExactly("Profile", "Settings", "Sign out");
        // The link to the page shown is marked as the current one
        assertThat(items).extracting(item -> item.getAttribute("aria-current")).containsExactly("page", null, null);
        assertThat(items.get(0).getChild("wicket:id", "icon").getName()).isEqualTo("svg");
        assertThat(items.get(1).getChild("wicket:id", "icon")).isNull();
        tester.assertVisible("user:dropdown:menu:linksEnd");
    }

    @Test
    void withoutLinksOrDetailTheMenuHoldsTheNameAndSignOut() {
        tester.startComponentInPage(new OatUserMenu("user", Model.of("Ada Lovelace")));

        tester.assertInvisible("user:dropdown:menu:detail");
        tester.assertInvisible("user:dropdown:menu:linksEnd");
        assertThat(TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "menuitem", false)).hasSize(1);
    }

    @Test
    void signOutInvalidatesTheSessionAndShowsTheHomePage() {
        signIn("Ada Lovelace");
        tester.startPage(MenuPage.class);

        tester.submitForm("user:dropdown:menu:signOut");

        tester.assertRenderedPage(MockHomePage.class);
        assertThat(tester.getSession().getMetaData(USER)).isNull();
    }

    @Test
    void signOutIsAPost() {
        signIn("Ada Lovelace");
        tester.startPage(MenuPage.class);

        TagTester form = tester.getTagByWicketId("signOut");
        assertThat(form.getName()).isEqualTo("form");
        assertThat(form.getAttribute("method")).isEqualTo("post");
    }

    @Test
    void signOutWorksOnAPageStillOpenAfterTheSessionExpired() {
        signIn("Ada Lovelace");
        tester.startPage(MenuPage.class);
        // Signed out meanwhile: the stateless page is built again, without the menu
        signIn(null);

        tester.submitForm("user:dropdown:menu:signOut");

        tester.assertRenderedPage(MockHomePage.class);
    }

    @Test
    void signOutAndWhatFollowsCanBeOverridden() {
        StringBuilder calls = new StringBuilder();
        tester.startComponentInPage(new OatUserMenu("user", Model.of("Ada Lovelace")) {
            @Override
            protected void signOut() {
                calls.append("signOut ");
            }

            @Override
            protected void onSignedOut() {
                calls.append("onSignedOut");
            }
        });

        tester.submitForm("user:dropdown:menu:signOut");

        assertThat(calls).hasToString("signOut onSignedOut");
    }

    @Test
    void itKeepsAPageStateless() {
        signIn("Ada Lovelace");
        MenuPage page = tester.startPage(MenuPage.class);

        assertThat(page.isPageStateless()).isTrue();
    }

    @Test
    void itFollowsTheSignedInUserOnEveryRender() {
        tester.startPage(MenuPage.class);
        tester.assertInvisible("user:dropdown");

        signIn("Grace Hopper");
        tester.startPage(tester.getLastRenderedPage());

        tester.assertLabel("user:dropdown:trigger:name", "Grace Hopper");
        tester.assertLabel("user:dropdown:trigger:avatar:container:initials", "GH");
    }

    @Test
    void someoneSignedInWithoutANameGetsTheMenu() {
        tester.startComponentInPage(new OatUserMenu("user", Model.of((String) null)) {
            @Override
            protected boolean isSignedIn() {
                return true;
            }
        });

        tester.assertVisible("user:dropdown");
        assertThat(tester.getTagByWicketId("trigger").getAttribute("aria-label")).startsWith("Account:").doesNotContain("null");
        tester.assertLabel("user:dropdown:trigger:avatar:container:initials", "?");
    }

    @Test
    void theModelsAreReadOncePerRequest() {
        AtomicInteger nameReads = new AtomicInteger();
        AtomicInteger pictureReads = new AtomicInteger();
        tester.startComponentInPage(new OatUserMenu("user", () -> {
            nameReads.incrementAndGet();
            return "Ada Lovelace";
        }).setPicture(() -> {
            pictureReads.incrementAndGet();
            return "/ada.png";
        }));

        assertThat(nameReads).hasValue(1);
        assertThat(pictureReads).hasValue(1);
    }

    @Test
    void aPictureCoversTheInitials() {
        tester.startComponentInPage(new OatUserMenu("user", Model.of("Ada Lovelace")).setPicture(Model.of("/ada.png")));

        TagTester img = tester.getTagByWicketId("img");
        assertThat(img.getAttribute("src")).isEqualTo("/ada.png");
        assertThat(img.getAttribute("alt")).isEmpty();
        tester.assertLabel("user:dropdown:trigger:avatar:container:initials", "AL");
    }

    @Test
    void theTextsAreTranslated() {
        tester.getSession().setLocale(Locale.GERMAN);
        tester.startComponentInPage(new OatUserMenu("user", Model.of("Ada Lovelace")));

        assertThat(tester.getTagByWicketId("trigger").getAttribute("aria-label")).isEqualTo("Konto: Ada Lovelace");
        assertThat(tester.getLastResponseAsString()).contains("Abmelden");
    }

    @Test
    void anUnknownIconIsRefusedRightAway() {
        OatUserMenu menu = new OatUserMenu("user", Model.of("Ada Lovelace"));

        assertThatIllegalArgumentException().isThrownBy(() -> menu.addLink("Profile", MenuPage.class).setIcon("no-such-icon"));
    }
}

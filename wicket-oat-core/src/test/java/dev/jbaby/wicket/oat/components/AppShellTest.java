package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.behaviors.AjaxBusyBehavior;
import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class AppShellTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new MockApplication() {
            @Override
            protected void init() {
                super.init();
                WicketOats.install(this);
            }
        });
    }

    @AfterEach
    void tearDown() {
        tester.destroy();
    }

    // --- Sidebar groups and badges ---

    @Nested
    class Menu {

        @BeforeEach
        void denySecretPage() {
            MenuGroupsPage.unread = 3;
            tester.getApplication().getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy() {
                @Override
                public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
                    return componentClass != ParamLayoutPage.SecretPage.class;
                }
            });
        }

        private List<TagTester> tags(String wicketId) {
            return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", wicketId, false);
        }

        @Test
        void groupsRenderAsCollapsibleSectionsOfLinks() {
            tester.startPage(MenuGroupsPage.class, new PageParameters().add("id", 1));

            List<TagTester> groups = tags("menuGroup");
            assertThat(groups).hasSize(1); // Admin has no page the user may open
            assertThat(groups.get(0).getName()).isEqualTo("details");
            assertThat(groups.get(0).getAttribute("open")).isNull();
            assertThat(tester.getLastResponseAsString()).contains("<summary wicket:id=\"label\">Sales</summary>");
            assertThat(tags("link")).hasSize(3);
            assertThat(tester.getLastResponseAsString()).doesNotContain("Admin").doesNotContain("Secret");
        }

        @Test
        void aGroupIsOpenWhileOneOfItsPagesIsShown() {
            tester.startPage(MenuGroupsPage.class, new PageParameters().add("id", 3));

            assertThat(tags("menuGroup").get(0).getAttribute("open")).isEqualTo("open");
            assertThat(tags("link")).extracting(l -> l.getAttribute("aria-current")).containsExactly(null, null, "page");
        }

        @Test
        void badgesShowTheirContentAndHideWhenZero() {
            tester.startPage(MenuGroupsPage.class, new PageParameters().add("id", 1));
            assertThat(tags("menuBadge")).extracting(TagTester::getValue).containsExactly("3", "New");
            assertThat(tags("menuBadge")).allMatch(b -> "badge".equals(b.getAttribute("class")));

            MenuGroupsPage.unread = 0;
            tester.startPage(MenuGroupsPage.class, new PageParameters().add("id", 1));
            assertThat(tags("menuBadge")).extracting(TagTester::getValue).containsExactly("New");
        }

        @Test
        void groupsAreValidated() {
            MenuItem link = MenuItem.of("Home", MenuGroupsPage.class);
            MenuItem group = MenuItem.group("Sales", link);

            assertThat(group.isGroup()).isTrue();
            assertThat(link.isGroup()).isFalse();
            assertThatIllegalArgumentException().isThrownBy(() -> MenuItem.group("Nested", group));
            assertThatIllegalArgumentException().isThrownBy(() -> MenuItem.group("Empty"));
            assertThatIllegalStateException().isThrownBy(() -> group.withBadge("1"));
            assertThatNullPointerException().isThrownBy(() -> MenuItem.of("No page", null));
        }

        @Test
        void theThreeArgumentConstructorStillWorks() {
            MenuItem item = new MenuItem(Model.of("Home"), MenuGroupsPage.class, null);
            assertThat(item.items()).isEmpty();
            assertThat(item.badge()).isNull();
        }
    }

    // --- Busy buttons ---

    public static class ButtonsPage extends WebPage implements IMarkupResourceStreamProvider {
        public final List<String> clicks = new ArrayList<>();

        public ButtonsPage() {
            add(Oat.Components.button("oat", "Save", target -> clicks.add("oat")).setBusyIndicator(true));
            add(Oat.Components.button("quiet", "Save", target -> clicks.add("quiet")));
            add(new AjaxLink<Void>("plain") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    clicks.add("plain");
                }
            });
            add(new AjaxLink<Void>("marked") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    clicks.add("marked");
                }
            }.add(Oat.Behaviors.ajaxBusy()));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><button wicket:id='oat'></button><button wicket:id='quiet'></button>"
                    + "<a wicket:id='plain'>Plain</a><a wicket:id='marked'>Marked</a></body></html>");
        }
    }

    /** The Ajax attributes script rendered for a component, by its markup id. */
    private String ajaxScriptFor(Component component) {
        String html = tester.getLastResponseAsString();
        String marker = "\"c\":\"" + component.getMarkupId() + "\"";
        int at = html.indexOf(marker);
        assertThat(at).as("Ajax script for %s", component.getId()).isNotNegative();
        int end = html.indexOf("});", at);
        return html.substring(html.lastIndexOf("Wicket.Ajax.ajax(", at), end);
    }

    @Test
    void onlyButtonsThatOptInGetTheBusyHandling() {
        ButtonsPage page = tester.startPage(ButtonsPage.class);

        assertThat(ajaxScriptFor(page.get("oat"))).contains("aria-busy").contains("attrs.oatBusy");
        assertThat(ajaxScriptFor(page.get("marked"))).contains("aria-busy");
        assertThat(ajaxScriptFor(page.get("plain"))).doesNotContain("aria-busy");
        assertThat(ajaxScriptFor(page.get("quiet"))).doesNotContain("aria-busy");
    }

    @Test
    void busyButtonsStillRunTheirAction() {
        ButtonsPage page = tester.startPage(ButtonsPage.class);
        tester.executeAjaxEvent("oat", "click");
        tester.executeAjaxEvent("marked", "click");
        assertThat(page.clicks).containsExactly("oat", "marked");
    }

    @Test
    void busyIndicatorIsOffUntilTurnedOn() {
        OatButton button = Oat.Components.button("b", "Save", target -> { });
        assertThat(button.getBehaviors(AjaxBusyBehavior.class)).isEmpty();
        button.setBusyIndicator(true).setBusyIndicator(true);
        assertThat(button.getBehaviors(AjaxBusyBehavior.class)).hasSize(1);
        button.setBusyIndicator(false);
        assertThat(button.getBehaviors(AjaxBusyBehavior.class)).isEmpty();

        OatSubmitButton submit = new OatSubmitButton("s");
        assertThat(submit.getBehaviors(AjaxBusyBehavior.class)).isEmpty();
        assertThat(submit.setBusyIndicator(true).getBehaviors(AjaxBusyBehavior.class)).hasSize(1);
    }

    @Test
    void aDialogsConfirmButtonCanShowItIsBusy() {
        OatConfirmDialog dialog = new OatConfirmDialog("confirm");
        Component confirmButton = dialog.get("dialog:form:confirm");
        assertThat(confirmButton.getBehaviors(AjaxBusyBehavior.class)).isEmpty();

        assertThat(dialog.setBusyIndicator(true)).isSameAs(dialog);
        assertThat(confirmButton.getBehaviors(AjaxBusyBehavior.class)).hasSize(1);
        dialog.setBusyIndicator(false);
        assertThat(confirmButton.getBehaviors(AjaxBusyBehavior.class)).isEmpty();
    }

    @Test
    void installingTwiceAddsTheBusyListenerOnce() {
        WicketOats.install(tester.getApplication());
        long listeners = 0;
        for (AjaxRequestTarget.IListener listener : tester.getApplication().getAjaxRequestTargetListeners()) {
            listeners += listener instanceof AjaxBusyBehavior.Listener ? 1 : 0;
        }
        assertThat(listeners).isEqualTo(1);

        ButtonsPage page = tester.startPage(ButtonsPage.class);
        String script = ajaxScriptFor(page.get("oat"));
        assertThat(script.indexOf("attrs.oatBusy=true")).isEqualTo(script.lastIndexOf("attrs.oatBusy=true"));
    }

    // --- Confirm dialog ---

    public static class ConfirmPage extends WebPage implements IMarkupResourceStreamProvider {
        public final List<String> deleted = new ArrayList<>();
        public final OatConfirmDialog confirm = new OatConfirmDialog("confirm");

        public ConfirmPage() {
            add(confirm);
            add(Oat.Components.button("delete", "Delete", target ->
                    confirm.ask(target, "Delete invoice?", "This can't be undone.", t -> deleted.add("#12"))));
            add(Oat.Components.button("archive", "Archive", target ->
                    confirm.setConfirmLabel(Model.of("Archive")).ask(target, "Archive invoice?", null, t -> deleted.add("archived"))));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><div wicket:id='confirm'></div>"
                    + "<button wicket:id='delete'></button><button wicket:id='archive'></button></body></html>");
        }
    }

    @Test
    void askOpensTheDialogWithTheQuestion() {
        tester.startPage(ConfirmPage.class);
        tester.executeAjaxEvent("delete", "click");

        assertThat(tester.getLastResponseAsString()).contains("showModal()");
        tester.assertLabel("confirm:dialog:form:header", "Delete invoice?");
        tester.assertLabel("confirm:dialog:form:body", "This can&#039;t be undone.");
        assertThat(tester.getTagByWicketId("confirm").getAttribute("data-variant")).isEqualTo("danger");
    }

    @Test
    void confirmingRunsTheActionOnce() {
        ConfirmPage page = tester.startPage(ConfirmPage.class);
        tester.executeAjaxEvent("delete", "click");
        assertThat(page.deleted).isEmpty();

        tester.executeAjaxEvent("confirm:dialog:form:confirm", "click");
        assertThat(page.deleted).containsExactly("#12");
        assertThat(tester.getLastResponseAsString()).contains(".close()");

        // A replayed confirm without a new question does nothing
        tester.executeAjaxEvent("confirm:dialog:form:confirm", "click");
        assertThat(page.deleted).containsExactly("#12");
    }

    @Test
    void aNewQuestionReplacesTheOpenOne() {
        ConfirmPage page = tester.startPage(ConfirmPage.class);
        tester.executeAjaxEvent("delete", "click");
        tester.executeAjaxEvent("archive", "click");

        tester.assertLabel("confirm:dialog:form:confirm:confirmLabel", "Archive");
        tester.assertInvisible("confirm:dialog:form:body");
        tester.executeAjaxEvent("confirm:dialog:form:confirm", "click");
        assertThat(page.deleted).containsExactly("archived");
    }

    // --- Lazy loading ---

    @Test
    void lazyLoadShowsSkeletonsThenTheContent() {
        OatLazyLoadPanel<Component> panel = Oat.Components.lazyLoad("lazy", id -> new Label(id, "Revenue: 42"));
        tester.startComponentInPage(panel);

        List<TagTester> skeletons = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "status", false);
        assertThat(skeletons).hasSize(3).allMatch(s -> "skeleton line".equals(s.getAttribute("class")));
        assertThat(tester.getLastResponseAsString()).doesNotContain("Revenue");

        tester.executeAllTimerBehaviors(tester.getLastRenderedPage());
        assertThat(tester.getLastResponseAsString()).contains("Revenue: 42");
        assertThat(panel.isLoaded()).isTrue();
    }

    @Test
    void lazyLoadPlaceholderCanBeShaped() {
        tester.startComponentInPage(Oat.Components.lazyLoad("lazy", id -> new Label(id, "Chart"))
                .setPlaceholder(SkeletonBehavior.Shape.BOX, 1));

        List<TagTester> skeletons = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "status", false);
        assertThat(skeletons).hasSize(1).allMatch(s -> "skeleton box".equals(s.getAttribute("class")));
        assertThatIllegalArgumentException().isThrownBy(() -> Oat.Components.lazyLoad("x", id -> new Label(id))
                .setPlaceholder(SkeletonBehavior.Shape.LINE, 0));
    }
}

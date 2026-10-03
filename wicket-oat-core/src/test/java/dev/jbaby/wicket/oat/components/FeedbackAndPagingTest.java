package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackAndPagingTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    /** A form with an Oat field, an OatFeedbackPanel, and Ajax actions that report messages. */
    public static class FeedbackPage extends WebPage implements IMarkupResourceStreamProvider {

        public FeedbackPage(boolean toasts) {
            if (toasts) {
                add(Oat.Behaviors.feedbackToasts());
            }
            add(new OatFeedbackPanel("feedback").setVisible(!toasts));

            Form<Void> form = new Form<>("form");
            add(form);
            form.add(new OatTextField<String>("name", "Name", Model.of("")).setRequired(true));
            form.add(new AjaxButton("submit") {
                @Override
                protected void onError(AjaxRequestTarget target) {
                    form.error("Please fix the errors.");
                }
            });

            // Report messages without adding anything to the AjaxRequestTarget
            add(new AjaxLink<Void>("success") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    success("Saved.");
                }
            });
            add(new AjaxLink<Void>("nothing") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body>"
                    + "<div wicket:id='feedback'></div>"
                    + "<form wicket:id='form'><div wicket:id='name'></div><button wicket:id='submit'>Go</button></form>"
                    + "<a wicket:id='success'>success</a><a wicket:id='nothing'>nothing</a>"
                    + "</body></html>");
        }
    }

    @Test
    void feedbackPanelRendersEachMessageAsAnAlertWithTheLevelsVariant() {
        FeedbackPage page = new FeedbackPage(false);
        page.info("Info message");
        page.success("Success message");
        page.warn("Warning message");
        page.error("Error message");
        tester.startPage(page);

        List<TagTester> alerts = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "alert", false);
        assertThat(alerts).extracting(TagTester::getValue)
                .containsExactly("<span wicket:id=\"message\">Info message</span>",
                        "<span wicket:id=\"message\">Success message</span>",
                        "<span wicket:id=\"message\">Warning message</span>",
                        "<span wicket:id=\"message\">Error message</span>");
        assertThat(alerts).extracting(tag -> tag.getAttribute("data-variant"))
                .containsExactly(null, "success", "warning", "danger");
    }

    @Test
    void feedbackPanelLeavesOutErrorsTheFieldsShowInline() {
        tester.startPage(new FeedbackPage(false));

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:container:field", "");
        tester.executeAjaxEvent("form:submit", "click");

        tester.assertComponentOnAjaxResponse("feedback");
        String response = tester.getLastResponseAsString();
        assertThat(response).contains("Please fix the errors.");
        // The field isn't re-rendered here, so the required error must not appear at all
        assertThat(response).doesNotContain("is required");
    }

    @Test
    void feedbackPanelAddsItselfToAjaxResponsesWhileItHasMessagesToShowOrClear() {
        tester.startPage(new FeedbackPage(false));

        tester.clickLink("success");
        tester.assertComponentOnAjaxResponse("feedback");
        assertThat(tester.getLastResponseAsString()).contains("Saved.");

        // The next request has no messages, but the panel must clear the old one.
        // WicketTester keeps rendered messages so tests can assert them; drop them as a real request would
        tester.cleanupFeedbackMessages();
        tester.clickLink("nothing");
        tester.assertComponentOnAjaxResponse("feedback");
        assertThat(tester.getLastResponseAsString()).doesNotContain("Saved.");

        // Nothing shown and nothing to show: the panel stays out of the response
        tester.cleanupFeedbackMessages();
        tester.clickLink("nothing");
        assertThat(tester.getLastResponseAsString()).doesNotContain("id=\"feedback");
    }

    @Test
    void feedbackToastsShowMessagesOnAFullPageRender() {
        FeedbackPage page = new FeedbackPage(true);
        page.info("Info message");
        page.error("Error message");
        tester.startPage(page);

        assertThat(tester.getLastResponseAsString())
                .contains("ot.toast(\"Info message\", null, {})")
                .contains("ot.toast(\"Error message\", null, {variant: \"danger\"})");
    }

    @Test
    void feedbackToastsShowSessionMessages() {
        tester.getSession().success("Welcome back.");
        tester.startPage(new FeedbackPage(true));

        assertThat(tester.getLastResponseAsString()).contains("ot.toast(\"Welcome back.\", null, {variant: \"success\"})");
    }

    @Test
    void feedbackToastsShowAjaxMessagesOnceWithoutAddingAnythingToTheTarget() {
        tester.startPage(new FeedbackPage(true));

        tester.clickLink("success");
        String response = tester.getLastResponseAsString();
        assertThat(response.split("ot.toast\\(\"Saved.\"", -1)).hasSize(2);
        assertThat(response).contains("{variant: \"success\"}");

        tester.clickLink("nothing");
        assertThat(tester.getLastResponseAsString()).doesNotContain("ot.toast");
    }

    @Test
    void feedbackToastsLeaveOutErrorsTheFieldsShowInline() {
        tester.startPage(new FeedbackPage(true));

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("name:container:field", "");
        tester.executeAjaxEvent("form:submit", "click");

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("ot.toast(\"Please fix the errors.\", null, {variant: \"danger\"})");
        assertThat(response).doesNotContain("ot.toast(\"'Name' is required.\"");
    }

    /** 25 items, 10 per page, with a plain and an Ajax navigator. */
    public static class PagingPage extends WebPage implements IMarkupResourceStreamProvider {

        public final PageableListView<Integer> items;

        public PagingPage() {
            WebMarkupContainer list = new WebMarkupContainer("list");
            list.setOutputMarkupId(true);
            add(list);
            items = new PageableListView<>("items", IntStream.rangeClosed(1, 25).boxed().toList(), 10) {
                @Override
                protected void populateItem(ListItem<Integer> item) {
                    item.add(new Label("value", item.getModel()));
                }
            };
            list.add(items);
            add(new OatPagingNavigator("navigator", items));
            add(new OatAjaxPagingNavigator("ajaxNavigator", items));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body>"
                    + "<ul wicket:id='list'><li wicket:id='items'><span wicket:id='value'></span></li></ul>"
                    + "<div wicket:id='navigator'></div><div wicket:id='ajaxNavigator'></div>"
                    + "</body></html>");
        }
    }

    @Test
    void pagingNavigatorRendersOatPaginationWithTheCurrentPageMarked() {
        tester.startPage(PagingPage.class);

        TagTester nav = tester.getTagByWicketId("navigator").getChild("nav");
        assertThat(nav.getAttribute("aria-label")).isEqualTo("Pagination");

        TagTester first = tester.getTagByWicketId("first");
        assertThat(first.getAttribute("class")).isEqualTo("button outline small");
        assertThat(first.getAttribute("aria-disabled")).isEqualTo("true");
        assertThat(tester.getTagByWicketId("next").getAttribute("aria-disabled")).isNull();

        List<TagTester> pageLinks = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "pageLink", false)
                .subList(0, 3); // the plain navigator's three page links
        assertThat(pageLinks).extracting(tag -> tag.getAttribute("class"))
                .containsExactly("button small", "button outline small", "button outline small");
        assertThat(pageLinks).extracting(tag -> tag.getAttribute("aria-current"))
                .containsExactly("page", null, null);
    }

    @Test
    void pagingNavigatorLinksChangeThePage() {
        PagingPage page = tester.startPage(PagingPage.class);

        tester.clickLink("navigator:navigation:2:pageLink");
        assertThat(page.items.getCurrentPage()).isEqualTo(2);
        assertThat(tester.getTagByWicketId("last").getAttribute("aria-disabled")).isEqualTo("true");
        assertThat(tester.getTagByWicketId("first").getAttribute("aria-disabled")).isNull();
    }

    @Test
    void ajaxPagingNavigatorReRendersTheListAndItself() {
        PagingPage page = tester.startPage(PagingPage.class);

        tester.clickLink("ajaxNavigator:next");
        assertThat(page.items.getCurrentPage()).isEqualTo(1);
        tester.assertComponentOnAjaxResponse("list");
        tester.assertComponentOnAjaxResponse("ajaxNavigator");
        assertThat(tester.getLastResponseAsString()).contains(">11<").doesNotContain(">10<");
    }

    @Test
    void dataTableUsesTheOatPagingNavigator() {
        OatDataTable<String, String> table = new OatDataTable<>("table",
                List.of(new org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn<>(Model.of("Value"), "toString")),
                new org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider<>() {
                    @Override
                    public java.util.Iterator<? extends String> iterator(long first, long count) {
                        return IntStream.range((int) first, (int) Math.min(first + count, 25)).mapToObj(i -> "row" + i).iterator();
                    }

                    @Override
                    public long size() {
                        return 25;
                    }

                    @Override
                    public org.apache.wicket.model.IModel<String> model(String object) {
                        return Model.of(object);
                    }
                }, 10);
        tester.startComponentInPage(table);

        OatPagingNavigator navigator = table.visitChildren(OatPagingNavigator.class, (c, visit) -> visit.stop((OatPagingNavigator) c));
        assertThat(navigator).isNotNull();
        assertThat(tester.getLastResponseAsString()).contains("aria-current=\"page\"");
    }
}

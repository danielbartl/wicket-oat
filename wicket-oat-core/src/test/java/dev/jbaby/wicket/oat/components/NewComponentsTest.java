package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.components.form.OatFileDropzone;
import dev.jbaby.wicket.oat.components.form.OatTagInput;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NewComponentsTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void testOatBreadcrumb() {
        List<String> crumbs = List.of("Home", "Docs", "Breadcrumb");
        OatBreadcrumb<String> breadcrumb = new OatBreadcrumb<String>("breadcrumb", Model.ofList(crumbs)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                Link<Void> link = new Link<>("link") {
                    @Override
                    public void onClick() {}
                };
                link.add(new Label("label", item.getModelObject()));
                item.add(link);
            }
        };
        tester.startComponentInPage(breadcrumb, Markup.of(
                "<nav wicket:id=\"breadcrumb\"><ol wicket:id=\"list\"><li wicket:id=\"items\">"
                        + "<a wicket:id=\"link\"><span wicket:id=\"label\"></span></a>"
                        + "<span wicket:id=\"separator\"></span></li></ol></nav>"));

        TagTester nav = tester.getTagByWicketId("breadcrumb");
        assertThat(nav.getAttribute("aria-label")).isEqualTo("Breadcrumb");

        // First item: separator visible, not marked current
        tester.assertVisible("breadcrumb:list:items:0:separator");
        // Last item: separator hidden, link marked as the current page
        tester.assertInvisible("breadcrumb:list:items:2:separator");
        String html = tester.getLastResponseAsString();
        assertThat(html).contains("aria-current=\"page\"");
    }

    @Test
    void testOatPagination() {
        List<String> pages = List.of("1", "2", "3");
        OatPagination<String> pagination = new OatPagination<String>("pagination", Model.ofList(pages)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                Link<Void> link = new Link<>("link") {
                    @Override
                    public void onClick() {}
                };
                link.add(new Label("label", item.getModelObject()));
                item.add(link);
            }
        };
        tester.startComponentInPage(pagination, Markup.of(
                "<nav wicket:id=\"pagination\"><menu wicket:id=\"menu\"><li wicket:id=\"items\">"
                        + "<a wicket:id=\"link\" class=\"button outline small\"><span wicket:id=\"label\"></span></a>"
                        + "</li></menu></nav>"));

        TagTester nav = tester.getTagByWicketId("pagination");
        assertThat(nav.getAttribute("aria-label")).isEqualTo("Pagination");

        TagTester menu = tester.getTagByWicketId("menu");
        assertThat(menu.getName()).isEqualTo("menu");
        assertThat(menu.getAttribute("class")).contains("buttons");

        tester.assertVisible("pagination:menu:items:0:link");
        tester.assertVisible("pagination:menu:items:2:link");
    }

    @Test
    void testOatDialog() {
        OatDialog dialog = new OatDialog("dialog", Model.of("Open dialog"), Model.of("Title"));
        tester.startComponentInPage(dialog);

        TagTester dialogTag = tester.getTagByWicketId("dialog");
        assertThat(dialogTag.getName()).isEqualTo("dialog");
        assertThat(dialogTag.getAttribute("closedby")).isEqualTo("any");
        String dialogMarkupId = dialogTag.getAttribute("id");

        TagTester trigger = tester.getTagByWicketId("trigger");
        assertThat(trigger.getAttribute("commandfor")).isEqualTo(dialogMarkupId);

        TagTester cancel = tester.getTagByWicketId("cancel");
        assertThat(cancel.getAttribute("commandfor")).isEqualTo(dialogMarkupId);

        TagTester confirm = tester.getTagByWicketId("confirm");
        assertThat(confirm.getAttribute("commandfor")).isEqualTo(dialogMarkupId);
        assertThat(confirm.getAttribute("command")).isEqualTo("close");

        tester.assertLabel("dialog:dialog:header", "Title");
    }

    @Test
    void testOatDropdown() {
        List<String> items = List.of("Profile", "Logout");
        OatDropdown<String> dropdown = new OatDropdown<String>("dropdown", Model.of("Options"), Model.ofList(items)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("label", item.getModelObject()));
            }
        };
        tester.startComponentInPage(dropdown);

        TagTester menu = tester.getTagByWicketId("menu");
        String menuMarkupId = menu.getAttribute("id");

        TagTester trigger = tester.getTagByWicketId("trigger");
        assertThat(trigger.getAttribute("popovertarget")).isEqualTo(menuMarkupId);

        tester.assertLabel("dropdown:menu:items:0:label", "Profile");
    }

    @Test
    void testOatTabs() {
        List<String> tabs = List.of("Account", "Password");
        OatTabs<String> oatTabs = new OatTabs<String>("tabs", Model.ofList(tabs)) {
            @Override
            protected void populateTab(ListItem<String> item) {
                item.add(new Label("tabLabel", item.getModelObject()));
            }

            @Override
            protected void populatePanel(ListItem<String> item) {
                item.add(new Label("panelContent", "Content for " + item.getModelObject()));
            }
        };
        tester.startComponentInPage(oatTabs, Markup.of(
                "<ot-tabs wicket:id=\"tabs\"><div wicket:id=\"tablist\" role=\"tablist\">"
                        + "<button wicket:id=\"tabButtons\" role=\"tab\"><span wicket:id=\"tabLabel\"></span></button>"
                        + "</div><div wicket:id=\"tabPanels\" role=\"tabpanel\"><span wicket:id=\"panelContent\"></span></div></ot-tabs>"));

        TagTester tablist = tester.getTagByWicketId("tablist");
        assertThat(tablist.getAttribute("role")).isEqualTo("tablist");
        tester.assertLabel("tabs:tablist:tabButtons:0:tabLabel", "Account");
        tester.assertLabel("tabs:tabPanels:0:panelContent", "Content for Account");
    }

    @Test
    void testOatTagInput() {
        OatTagInput tagInput = new OatTagInput("tagInput", "Tags", Model.of("apple,mango"));
        tester.startComponentInPage(tagInput);

        TagTester field = tester.getTagByWicketId("field");
        assertThat(field.getAttribute("type")).isEqualTo("hidden");
        assertThat(field.getAttribute("value")).isEqualTo("apple,mango");

        TagTester taginput = tester.getTagByWicketId("taginput");
        assertThat(taginput.getName()).isEqualTo("ot-taginput");

        // The sync script must reference both elements' markup ids
        String html = tester.getLastResponseAsString();
        assertThat(html).contains(field.getAttribute("id"));
        assertThat(html).contains(taginput.getAttribute("id"));
    }

    @Test
    void testOatFileDropzone() {
        OatFileDropzone dropzone = new OatFileDropzone("dropzone", "Attachments", Model.ofList(List.<FileUpload>of()));
        tester.startComponentInPage(dropzone);

        TagTester field = tester.getTagByWicketId("field");
        assertThat(field.getAttribute("type")).isEqualTo("file");
        assertThat(field.getAttribute("multiple")).isEqualTo("multiple");
        assertThat(tester.getLastResponseAsString()).contains("<ot-upload>");
    }
}

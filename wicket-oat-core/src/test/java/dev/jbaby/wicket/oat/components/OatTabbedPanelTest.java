package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OatTabbedPanelTest {

    private WicketTester tester;
    private final List<String> created = new ArrayList<>();

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        created.clear();
    }

    /** A panel showing a text, recording that it was created. */
    public static class TextPanel extends Panel implements IMarkupResourceStreamProvider {
        public TextPanel(String id, String text) {
            super(id);
            add(new Label("text", text));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<wicket:panel><p wicket:id='text'></p></wicket:panel>");
        }
    }

    private ITab tab(String title) {
        return Oat.tab(title, id -> {
            created.add(title);
            return new TextPanel(id, title + " content");
        });
    }

    private List<TagTester> tabs() {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "tab", false);
    }

    @Test
    void rendersOatTabsAndCreatesOnlyTheSelectedPanel() {
        tester.startComponentInPage(new OatTabbedPanel<>("tabs", List.of(tab("Profile"), tab("Settings"))));

        assertThat(TagTester.createTagByAttribute(tester.getLastResponseAsString(), "role", "tablist")).isNotNull();
        assertThat(tabs()).extracting(TagTester::getValue)
                .containsExactly("<span wicket:id=\"title\">Profile</span>", "<span wicket:id=\"title\">Settings</span>");
        assertThat(tabs()).extracting(t -> t.getAttribute("aria-selected")).containsExactly("true", "false");

        TagTester panel = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "role", "tabpanel");
        assertThat(panel.getValue()).contains("Profile content");
        assertThat(tabs().get(0).getAttribute("aria-controls")).isEqualTo(panel.getAttribute("id"));
        assertThat(created).containsExactly("Profile");

        // TabbedPanel's own CSS classes don't leak into Oat's markup
        assertThat(tester.getLastResponseAsString()).doesNotContain("tab-row").doesNotContain("class=\"tab0");
    }

    @Test
    void clickingATabSwapsThePanelOverAjax() {
        tester.startComponentInPage(new OatTabbedPanel<>("tabs", List.of(tab("Profile"), tab("Settings"))));

        tester.clickLink("tabs:tabs-container:tabs:1:link");

        tester.assertComponentOnAjaxResponse("tabs");
        assertThat(created).containsExactly("Profile", "Settings");
        assertThat(tabs()).extracting(t -> t.getAttribute("aria-selected")).containsExactly("false", "true");
        assertThat(tester.getLastResponseAsString()).contains("Settings content").doesNotContain("Profile content");
    }

    @Test
    void theSelectedTabCanComeFromAModel() {
        Model<Integer> selected = Model.of(1);
        tester.startComponentInPage(new OatTabbedPanel<>("tabs", List.of(tab("Profile"), tab("Settings")), selected));

        assertThat(created).containsExactly("Settings");
        tester.clickLink("tabs:tabs-container:tabs:0:link");
        assertThat(selected.getObject()).isZero();
    }

    @Test
    void hiddenTabsAreNotRendered() {
        ITab hidden = new AbstractTab(Model.of("Admin")) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
                return new TextPanel(panelId, "Admin content");
            }

            @Override
            public boolean isVisible() {
                return false;
            }
        };
        tester.startComponentInPage(Oat.Components.tabbedPanel("tabs", List.of(tab("Profile"), hidden)));

        assertThat(tabs()).hasSize(1);
    }
}

package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.Component;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutAndNavigationTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getApplication().getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy() {
            @Override
            public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
                return componentClass != ParamLayoutPage.SecretPage.class;
            }
        });
    }

    private List<TagTester> menuLinks() {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "link", false);
    }

    @Test
    void bookmarkableLayoutPagesReceiveTheirParameters() {
        ParamLayoutPage page = tester.startPage(ParamLayoutPage.class, new PageParameters().add("id", 2));
        assertThat(page.getPageParameters().get("id").toInt()).isEqualTo(2);
    }

    @Test
    void parameterizedMenuItemsAreActiveOnlyForTheirParameters() {
        tester.startPage(ParamLayoutPage.class, new PageParameters().add("id", 2));

        assertThat(menuLinks()).extracting(l -> l.getAttribute("aria-current")).containsExactly(null, "page");
        assertThat(menuLinks().get(0).getAttribute("href")).contains("id=1");
    }

    @Test
    void menuItemsForPagesTheUserMayNotOpenAreLeftOut() {
        tester.startPage(ParamLayoutPage.class, new PageParameters().add("id", 1));

        assertThat(menuLinks()).hasSize(2);
        assertThat(tester.getLastResponseAsString()).doesNotContain("Secret");
    }

    @Test
    void layoutTextsComeFromResources() {
        tester.startPage(ParamLayoutPage.class, new PageParameters().add("id", 1));

        tester.assertLabel("appTitle", "Wicket Oat Application");
        assertThat(tester.getLastResponseAsString()).contains("aria-label=\"Toggle menu\"");
    }

    @Test
    void dropdownItemsRunTheirClickHandlerAndCloseTheMenu() {
        List<String> clicked = new ArrayList<>();
        OatDropdown<String> dropdown = Oat.Components.dropdown("dropdown", "Actions",
                Model.ofList(List.of("Edit", "Delete")), action -> action + "…", (target, action) -> clicked.add(action));
        tester.startComponentInPage(dropdown);

        assertThat(tester.getTagByWicketId("trigger").getAttribute("type")).isEqualTo("button");
        List<TagTester> items = TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "role", "menuitem", false);
        assertThat(items).extracting(TagTester::getValue)
                .containsExactly("<span wicket:id=\"label\">Edit…</span>", "<span wicket:id=\"label\">Delete…</span>");
        assertThat(items).allMatch(item -> "button".equals(item.getAttribute("type")));

        Component delete = dropdown.get("menu:items:1");
        tester.executeAjaxEvent(delete, "click");
        assertThat(clicked).containsExactly("Delete");
        assertThat(tester.getLastResponseAsString()).contains("hidePopover()");
    }
}

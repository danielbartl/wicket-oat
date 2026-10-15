package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class OatStepsTest {

    private static final List<String> ORDER = List.of("Ordered", "Paid", "Shipped", "Delivered");

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.ENGLISH);
    }

    private List<TagTester> steps() {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "items", false);
    }

    @Test
    void stepsBeforeTheCurrentOneAreDoneAndTheOnesAfterItToCome() {
        tester.startComponentInPage(new OatSteps("steps", ORDER, Model.of(2)));

        TagTester list = tester.getTagByWicketId("list");
        assertThat(list.getName()).isEqualTo("ol");
        assertThat(list.getAttribute("class")).isEqualTo("unstyled oat-steps");
        assertThat(list.getAttribute("aria-label")).isEqualTo("Steps");
        assertThat(steps()).extracting(step -> step.getAttribute("data-state")).containsExactly("done", "done", "current", "upcoming");
        assertThat(steps()).extracting(step -> step.getAttribute("aria-current")).containsExactly(null, null, "step", null);
        assertThat(TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "title", false))
                .extracting(TagTester::getValue).containsExactlyElementsOf(ORDER);
        // Without an action, no step is a link
        assertThat(TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", "link", false))
                .allMatch(link -> link.getAttribute("href") == null);
    }

    @Test
    void pastTheLastStepAllAreDoneAndWithoutOneNone() {
        tester.startComponentInPage(new OatSteps("steps", ORDER, Model.of(4)));
        assertThat(steps()).extracting(step -> step.getAttribute("data-state")).containsOnly("done");

        tester.startComponentInPage(new OatSteps("steps", ORDER, Model.of()));
        assertThat(steps()).extracting(step -> step.getAttribute("data-state")).containsOnly("upcoming");
    }

    @Test
    void stepsDoneCanBeSelected() {
        List<Integer> selected = new ArrayList<>();
        OatSteps steps = new OatSteps("steps", ORDER, Model.of(2)).setOnSelect((target, index) -> selected.add(index));
        tester.startComponentInPage(steps);

        List<Component> links = new ArrayList<>();
        steps.visitChildren(org.apache.wicket.ajax.markup.html.AjaxLink.class, (link, visit) -> links.add(link));
        assertThat(links).extracting(Component::isEnabledInHierarchy).containsExactly(true, true, false, false);

        tester.clickLink(links.get(1));
        assertThat(selected).containsExactly(1);
    }

    @Test
    void theListCanBeNamed() {
        tester.startComponentInPage(new OatSteps("steps", ORDER, Model.of(0)).setLabel(Model.of("Order status")));

        assertThat(tester.getTagByWicketId("list").getAttribute("aria-label")).isEqualTo("Order status");
    }
}

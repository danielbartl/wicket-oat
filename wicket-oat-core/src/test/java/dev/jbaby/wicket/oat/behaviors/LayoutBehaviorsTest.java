package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LayoutBehaviorsTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    private String classOf(Behavior behavior) {
        WebMarkupContainer container = new WebMarkupContainer("id");
        container.add(behavior);
        tester.startComponentInPage(container, Markup.of("<div wicket:id='id' class='mine'></div>"));
        return tester.getTagByWicketId("id").getAttribute("class");
    }

    @Test
    void gridBehaviorsAppendOatsClasses() {
        assertThat(classOf(Oat.Behaviors.row())).isEqualTo("mine row");
        assertThat(classOf(Oat.Behaviors.col(6))).isEqualTo("mine col-6");
        assertThat(classOf(Oat.Behaviors.col(4, 2))).isEqualTo("mine col-4 offset-2");
        assertThat(classOf(Oat.Behaviors.col(12, 0))).isEqualTo("mine col-12");
        assertThat(classOf(Oat.Behaviors.container())).isEqualTo("mine container");
    }

    @Test
    void columnsMustFitTheGrid() {
        assertThatIllegalArgumentException().isThrownBy(() -> GridBehavior.col(0));
        assertThatIllegalArgumentException().isThrownBy(() -> GridBehavior.col(13));
        assertThatIllegalArgumentException().isThrownBy(() -> GridBehavior.col(4, -1));
        assertThatIllegalArgumentException().isThrownBy(() -> GridBehavior.col(4, 7));
        assertThatIllegalArgumentException().isThrownBy(() -> GridBehavior.col(8, 6));
    }

    @Test
    void stackBehaviorsAppendOatsClasses() {
        assertThat(classOf(Oat.Behaviors.hstack())).isEqualTo("mine hstack");
        assertThat(classOf(Oat.Behaviors.vstack())).isEqualTo("mine vstack");
        assertThat(classOf(new StackBehavior())).isEqualTo("mine hstack");
    }

    @Test
    void inputGroupBehaviorAppendsGroupClass() {
        assertThat(classOf(Oat.Behaviors.inputGroup())).isEqualTo("mine group");
    }

    @Test
    void aColumnOnAnOatFieldGoesOnItsOwnTag() {
        var field = Oat.Components.textField("id", "Name", org.apache.wicket.model.Model.of("x"))
                .add(Oat.Behaviors.col(6));
        tester.startComponentInPage(field);
        assertThat(tester.getTagByWicketId("id").getAttribute("class")).isEqualTo("col-6");
        assertThat(tester.getTagByWicketId("field").getAttribute("class")).isNull();
    }
}

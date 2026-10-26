package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatSettings;
import dev.jbaby.wicket.oat.components.OatButton;
import dev.jbaby.wicket.oat.components.OatIcon;
import dev.jbaby.wicket.oat.components.OatSubmitButton;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class ButtonAccessibleNameTest {

    private static final Markup ICON_BUTTON = Markup.of("<button wicket:id=\"button\"><svg wicket:id=\"icon\"></svg></button>");

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    private static OatButton button(IModel<String> label) {
        return new OatButton("button", label) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(this);
            }
        };
    }

    @Test
    void anIconOnlyButtonWithoutAnAccessibleNameFailsToRender() {
        OatButton button = button(null).setIcon(true);
        button.add(new OatIcon("icon", "trash-2"));

        assertThatIllegalStateException()
                .isThrownBy(() -> tester.startComponentInPage(button, ICON_BUTTON))
                .withMessageContaining("The button \"button\" (OatButton on ")
                .withMessageContaining("has no accessible name")
                .withMessageContaining("aria-label")
                .withMessageContaining("OatSettings.setAccessibilityChecks(false)");
    }

    @Test
    void anAriaLabelAddedAfterTheButtonNamesIt() {
        OatButton button = button(null).setIcon(true);
        button.add(new OatIcon("icon", "trash-2"));
        button.add(AttributeModifier.replace("aria-label", "Delete"));

        tester.startComponentInPage(button, ICON_BUTTON);

        assertThat(tester.getTagByWicketId("button").getAttribute("aria-label")).isEqualTo("Delete");
    }

    @Test
    void aLabelledIconNamesTheButton() {
        OatButton button = button(null).setIcon(true);
        button.add(new OatIcon("icon", "trash-2").setLabel(Model.of("Delete")));

        tester.startComponentInPage(button, ICON_BUTTON);

        assertThat(tester.getTagByWicketId("icon").getAttribute("aria-label")).isEqualTo("Delete");
    }

    @Test
    void theButtonIsRenderedOnceAndUnchanged() {
        tester.startComponentInPage(button(Model.of("Save")), Markup.of("<a wicket:id=\"button\"></a>"));

        assertThat(tester.getLastResponseAsString()).containsOnlyOnce("wicket:id=\"button\"");
        assertThat(tester.getTagByWicketId("button").getValue()).isEqualTo("Save");
    }

    @Test
    void anAjaxUpdateIsCheckedToo() {
        IModel<String> label = Model.of("Save");
        OatButton button = button(label);
        button.setOutputMarkupId(true);
        tester.startComponentInPage(button, Markup.of("<a wicket:id=\"button\"></a>"));

        tester.clickLink("button");
        assertThat(tester.getLastResponseAsString()).containsOnlyOnce("Save</a>");

        label.setObject("");
        assertThatIllegalStateException().isThrownBy(() -> tester.clickLink("button"))
                .withMessageContaining("has no accessible name");
    }

    @Test
    void anyComponentStyledAsAButtonIsChecked() {
        WebMarkupContainer link = new WebMarkupContainer("link");
        link.add(Oat.Behaviors.button());

        assertThatIllegalStateException()
                .isThrownBy(() -> tester.startComponentInPage(link, Markup.of("<a wicket:id=\"link\" href=\"#\"><svg></svg></a>")))
                .withMessageContaining("The button \"link\" (WebMarkupContainer on ");
    }

    @Test
    void aSubmitInputIsNamedByItsValueOrTheBrowser() {
        Form<Void> form = new Form<>("form");
        form.add(new OatSubmitButton("save"));

        tester.startComponentInPage(form, Markup.of("<form wicket:id=\"form\"><input type=\"submit\" wicket:id=\"save\"></form>"));

        tester.assertComponent("form:save", OatSubmitButton.class);
    }

    @Test
    void theChecksCanBeTurnedOff() {
        OatSettings.get(tester.getApplication()).setAccessibilityChecks(false);

        tester.startComponentInPage(button(null), Markup.of("<a wicket:id=\"button\"></a>"));

        tester.assertComponent("button", OatButton.class);
    }

    @Test
    void theChecksAreOnInDevelopmentAndOffInDeployment() {
        assertThat(OatSettings.get(tester.getApplication()).isAccessibilityChecks()).isTrue();

        WicketTester deployment = new WicketTester(new MockApplication() {
            @Override
            public RuntimeConfigurationType getConfigurationType() {
                return RuntimeConfigurationType.DEPLOYMENT;
            }
        });
        assertThat(OatSettings.get(deployment.getApplication()).isAccessibilityChecks()).isFalse();
        deployment.startComponentInPage(button(null), Markup.of("<a wicket:id=\"button\"></a>"));
        deployment.destroy();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            <button>Save</button>                                                    | true
            <button><wicket:message key="save">Save</wicket:message></button>        | true
            <button aria-label="Delete"><svg></svg></button>                         | true
            <button aria-labelledby="delete-label"><svg></svg></button>              | true
            <button title="Delete"><svg></svg></button>                              | true
            <button><svg role="img" aria-label="Delete"></svg></button>              | true
            <button><span class="sr-only">Delete</span><svg></svg></button>          | true
            <button><img src="trash.png" alt="Delete"></button>                      | true
            <button><img src="trash.png" aria-hidden="true"> Delete</button>         | true
            <button><span aria-hidden="true"><span>×</span></span>Delete</button>    | true
            <input type="submit" wicket:id="save">                                   | true
            ''                                                                       | true
            <button></button>                                                        | false
            <button/>                                                                | false
            <button aria-label=" "><svg></svg></button>                              | false
            <button>&nbsp;</button>                                                  | false
            <button> <svg aria-hidden="true"></svg> </button>                        | false
            <button><svg aria-hidden="true"></svg></button>                          | false
            <button><svg aria-hidden="true"><title>Delete</title></svg></button>     | false
            <button><span aria-hidden="true">×</span></button>                       | false
            <button><span hidden>Delete</span></button>                              | false
            <button><img src="trash.png" alt=""></button>                            | false
            <a href="#"><span aria-hidden="true"><b>Delete</b></span></a> Next       | false
            """)
    void accessibleNames(String html, boolean named) {
        assertThat(AccessibleNames.hasName(html == null ? "" : html)).isEqualTo(named);
    }
}

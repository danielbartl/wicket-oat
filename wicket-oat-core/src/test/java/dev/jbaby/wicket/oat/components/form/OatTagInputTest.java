package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OatTagInputTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    private FormTester formWith(OatTagInput tags) {
        Form<Void> form = new Form<>("form");
        form.add(tags);
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='tags'></div></form>"));
        return tester.newFormTester("form");
    }

    @Test
    void submittedTagsAreTrimmedAndDeduplicated() {
        IModel<List<String>> tags = Model.ofList(new ArrayList<>());
        FormTester formTester = formWith(new OatTagInput("tags", "Tags", tags));

        formTester.setValue("tags:input", " apple, mango,,apple ,kiwi");
        formTester.submit();

        tester.assertNoErrorMessage();
        assertThat(tags.getObject()).containsExactly("apple", "mango", "kiwi");
    }

    @Test
    void noTagsIsAnEmptyListAndFailsRequired() {
        IModel<List<String>> tags = Model.ofList(new ArrayList<>(List.of("old")));
        FormTester formTester = formWith(new OatTagInput("tags", "Tags", tags));
        formTester.setValue("tags:input", "");
        formTester.submit();
        assertThat(tags.getObject()).isEmpty();

        tester = new WicketTester();
        formTester = formWith(new OatTagInput("tags", "Tags", tags).setRequired(true));
        formTester.setValue("tags:input", "");
        formTester.submit();
        tester.assertErrorMessages("'Tags' is required.");
    }

    @Test
    void theLabelHintAndErrorBelongToTheBoxTheUserTypesIn() {
        IModel<List<String>> tags = Model.ofList(new ArrayList<>());
        FormTester formTester = formWith(new OatTagInput("tags", Model.of("Tags"), tags, Model.of("Up to five")).setRequired(true));
        TagTester text = tester.getTagByWicketId("text");
        String feedbackId = tester.getTagByWicketId("feedback").getAttribute("id");

        assertThat(text.getAttribute("type")).isEqualTo("text");
        assertThat(tester.getTagByWicketId("label").getAttribute("for")).isEqualTo(text.getAttribute("id"));
        assertThat(text.getAttribute("aria-describedby")).isEqualTo(feedbackId);
        assertThat(text.getAttribute("aria-required")).isEqualTo("true");
        assertThat(text.getAttribute("aria-invalid")).isEqualTo("false");

        formTester.setValue("tags:input", "");
        formTester.submit();

        assertThat(tester.getTagByWicketId("text").getAttribute("aria-invalid")).isEqualTo("true");
    }

    @Test
    void thePlaceholderIsTheTextBoxs() {
        formWith(new OatTagInput("tags", "Tags", Model.ofList(new ArrayList<>())).setPlaceholder("Add a topic"));

        assertThat(tester.getTagByWicketId("text").getAttribute("placeholder")).isEqualTo("Add a topic");
        assertThat(tester.getTagByWicketId("input").getAttribute("placeholder")).isNull();
    }

    public static class Article implements Serializable {
        public List<String> tags = List.of("java", "wicket");
    }

    @Test
    void bindsToAListPropertyThroughACompoundPropertyModel() {
        Article article = new Article();
        Form<Article> form = new Form<>("form", new CompoundPropertyModel<>(article));
        form.add(new OatTagInput("tags"));
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='tags'></div></form>"));

        assertThat(tester.getTagByWicketId("input").getAttribute("value")).isEqualTo("java,wicket");

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("tags:input", "java,oat");
        formTester.submit();
        assertThat(article.tags).containsExactly("java", "oat");
    }
}

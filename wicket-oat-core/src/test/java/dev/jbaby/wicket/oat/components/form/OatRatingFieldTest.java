package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OatRatingFieldTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.ENGLISH);
    }

    private FormTester startInForm(OatRatingField field) {
        Form<Void> form = new Form<>("form");
        form.add(field);
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='rating'></div></form>"));
        return tester.newFormTester("form");
    }

    private List<TagTester> tags(String tag) {
        return TagTester.createTags(tester.getLastResponseAsString(), t -> t.getName().equals(tag), false);
    }

    @Test
    void theStarsAreRadioButtonsNamedForScreenReaders() {
        startInForm(new OatRatingField("rating", "How did we do?", Model.of()));

        assertThat(tester.getTagByWicketId("label").getName()).isEqualTo("legend");
        assertThat(tester.getTagByWicketId("label").getValue()).isEqualTo("How did we do?");
        assertThat(tester.getTagByWicketId("input").getAttribute("class")).isEqualTo("oat-rating-input");
        assertThat(tags("input")).extracting(t -> t.getAttribute("type")).containsOnly("radio");
        assertThat(tags("input")).extracting(t -> t.getAttribute("value")).containsExactly("1", "2", "3", "4", "5");
        assertThat(tags("label")).extracting(TagTester::getValue).containsExactly("1 star", "2 stars", "3 stars", "4 stars", "5 stars");
        // Each label names the radio button after it
        assertThat(tags("label").get(2).getAttribute("for")).isEqualTo(tags("input").get(2).getAttribute("id"));
        assertThat(tags("input")).allMatch(t -> t.getAttribute("checked") == null);
    }

    @Test
    void choosingAStarSetsTheNumberOfStars() {
        Model<Integer> rating = Model.of();
        FormTester form = startInForm(new OatRatingField("rating", "Rating", rating));
        form.setValue("rating:input", "4");
        form.submit();

        assertThat(rating.getObject()).isEqualTo(4);
        // The submit renders the form again, with the star chosen
        assertThat(tags("input")).extracting(t -> t.getAttribute("checked")).containsExactly(null, null, null, "checked", null);
    }

    @Test
    void requiredMeansAStarMustBeChosen() {
        Model<Integer> rating = Model.of();
        FormTester form = startInForm(new OatRatingField("rating", "Rating", rating).setRequired(true));
        form.submit();

        assertThat(rating.getObject()).isNull();
        assertThat(tester.getTagByWicketId("container").getAttribute("data-field")).isEqualTo("error");
        assertThat(tester.getTagByWicketId("feedback").getValue()).contains("Rating");
    }

    @Test
    void thereCanBeUpToTenStars() {
        startInForm(new OatRatingField("rating", "Rating", Model.of(7)).setMax(10));

        assertThat(tags("input")).hasSize(10);
        assertThat(tags("input").get(6).getAttribute("checked")).isEqualTo("checked");
        assertThatIllegalArgumentException().isThrownBy(() -> new OatRatingField("rating").setMax(0));
        assertThatIllegalArgumentException().isThrownBy(() -> new OatRatingField("rating").setMax(11));
    }

    @Test
    void theStarsAreNamedInTheUsersLanguage() {
        tester.getSession().setLocale(Locale.GERMAN);
        startInForm(new OatRatingField("rating", "Bewertung", Model.of()));

        assertThat(tags("label")).extracting(TagTester::getValue).startsWith("1 Stern", "2 Sterne");
    }
}

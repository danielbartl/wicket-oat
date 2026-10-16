package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.Markup;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OatRatingTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.ENGLISH);
    }

    private void start(OatRating rating) {
        tester.startComponentInPage(rating, Markup.of("<span wicket:id='score'></span>"));
    }

    private List<String> fills() {
        return Pattern.compile("data-fill=\"(\\w+)\"").matcher(tester.getLastResponseAsString()).results()
                .map(match -> match.group(1)).toList();
    }

    @Test
    void aRatingIsRoundedToHalfAStar() {
        start(new OatRating("score", 4.3));

        TagTester tag = tester.getTagByWicketId("score");
        assertThat(tag.getAttribute("class")).isEqualTo("oat-rating");
        assertThat(tag.getAttribute("role")).isEqualTo("img");
        assertThat(tag.getAttribute("aria-label")).isEqualTo("4.5 out of 5 stars");
        assertThat(fills()).containsExactly("full", "full", "full", "full", "half");

        start(new OatRating("score", 3));
        assertThat(tester.getTagByWicketId("score").getAttribute("aria-label")).isEqualTo("3 out of 5 stars");
        assertThat(fills()).containsExactly("full", "full", "full", "empty", "empty");
    }

    @Test
    void noRatingIsAllEmpty() {
        start(new OatRating("score", Model.of()));

        assertThat(tester.getTagByWicketId("score").getAttribute("aria-label")).isEqualTo("Not rated yet");
        assertThat(fills()).containsOnly("empty").hasSize(5);
    }

    @Test
    void aRatingOutsideTheStarsIsKeptInside() {
        start(new OatRating("score", 7));
        assertThat(tester.getTagByWicketId("score").getAttribute("aria-label")).isEqualTo("5 out of 5 stars");

        start(new OatRating("score", -1));
        assertThat(fills()).containsOnly("empty");
    }

    @Test
    void thereCanBeUpToTenStars() {
        start(new OatRating("score", 7.5).setMax(10));

        assertThat(fills()).hasSize(10).containsSequence("full", "half", "empty");
        assertThat(tester.getTagByWicketId("score").getAttribute("aria-label")).isEqualTo("7.5 out of 10 stars");
        assertThatIllegalArgumentException().isThrownBy(() -> new OatRating("score", 1).setMax(0));
    }

    @Test
    void theRatingIsReadInTheUsersLanguage() {
        tester.getSession().setLocale(Locale.GERMAN);
        start(new OatRating("score", 3.5));

        assertThat(tester.getTagByWicketId("score").getAttribute("aria-label")).isEqualTo("3,5 von 5 Sternen");
    }
}

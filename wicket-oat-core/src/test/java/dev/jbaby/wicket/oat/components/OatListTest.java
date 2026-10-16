package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OatListTest {

    record Person(String name, String role) implements Serializable {
    }

    private static final List<Person> TEAM = List.of(new Person("Ada Lovelace", "Engineer"), new Person("Alan Turing", null));

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    private String html() {
        return tester.getLastResponseAsString();
    }

    private List<TagTester> tags(String wicketId) {
        return TagTester.createTagsByAttribute(html(), "wicket:id", wicketId, false);
    }

    @Test
    void rowsHaveATitleALineUnderItAndPartsInFrontAndAtTheEnd() {
        tester.startComponentInPage(new OatList<>("team", Model.ofList(TEAM), Person::name)
                .setSubtitle(Person::role)
                .setLeading((id, person) -> new OatAvatar(id, person.getObject().name().substring(0, 1)))
                .setTrailing((id, person) -> new OatBadge(id, "Team")));

        TagTester list = tester.getTagByWicketId("list");
        assertThat(list.getName()).isEqualTo("ul");
        assertThat(list.getAttribute("class")).isEqualTo("unstyled oat-list");
        assertThat(tags("rows")).hasSize(2).allMatch(row -> "oat-list-row".equals(row.getAttribute("class")));
        assertThat(tags("title")).extracting(TagTester::getValue).containsExactly("Ada Lovelace", "Alan Turing");
        // A row without a subtitle leaves the line out
        assertThat(tags("subtitle")).extracting(TagTester::getValue).containsExactly("Engineer");
        assertThat(html()).contains("data-variant=\"avatar\"").contains(">Team<");
        // Without an action, the title is no link
        assertThat(tags("link")).isEmpty();
        // Nothing to show instead
        assertThat(tags("empty")).isEmpty();
    }

    @Test
    void withoutPartsTheirPlacesStayEmpty() {
        tester.startComponentInPage(new OatList<>("team", Model.ofList(TEAM), Person::name));

        // Empty, so the CSS hides them (:empty) and the row has no gap for them
        assertThat(html()).contains("<div class=\"oat-list-leading\"></div>").contains("<div class=\"oat-list-trailing\"></div>");
    }

    @Test
    void aRowWithAnActionIsALinkCoveringIt() {
        List<String> clicked = new ArrayList<>();
        OatList<Person> list = new OatList<>("team", Model.ofList(TEAM), Person::name)
                .setOnClick((target, person) -> clicked.add(person.name()));
        tester.startComponentInPage(list);

        assertThat(tags("link")).hasSize(2).allMatch(link -> "unstyled oat-list-link".equals(link.getAttribute("class")));
        List<Component> links = new ArrayList<>();
        list.visitChildren(AjaxLink.class, (link, visit) -> links.add(link));
        tester.clickLink(links.get(1));
        assertThat(clicked).containsExactly("Alan Turing");
    }

    @Test
    void withoutRowsTheEmptyStateShowsInstead() {
        IModel<List<Person>> people = Model.ofList(new ArrayList<>());
        OatList<Person> list = new OatList<>("team", people, Person::name)
                .setEmptyState(id -> new Label(id, "Nobody here"));
        tester.startComponentInPage(list);

        assertThat(tags("list")).isEmpty();
        assertThat(tester.getTagByWicketId("empty").getValue()).isEqualTo("Nobody here");

        people.getObject().add(TEAM.get(0));
        tester.startComponentInPage(list);
        assertThat(tags("rows")).hasSize(1);
        assertThat(tags("empty")).isEmpty();
    }

    /** A row type that can't be serialized, such as a JPA entity. */
    static final class Entity {
        final String name;

        Entity(String name) {
            this.name = name;
        }
    }

    @Test
    void rowsAreNotKeptWithThePage() {
        List<String> clicked = new ArrayList<>();
        OatList<Entity> list = new OatList<>("things", () -> List.of(new Entity("First"), new Entity("Second")), entity -> entity.name)
                .setOnClick((target, entity) -> clicked.add(entity.name));
        tester.startComponentInPage(list);

        // Read again through the row's model on click, not kept by the link
        WicketObjects.cloneObject(tester.getLastRenderedPage());
        List<Component> links = new ArrayList<>();
        list.visitChildren(AjaxLink.class, (link, visit) -> links.add(link));
        tester.clickLink(links.get(0));
        assertThat(clicked).containsExactly("First");
    }

    @Test
    void theRowsAreReadOncePerRequest() {
        AtomicInteger reads = new AtomicInteger();
        tester.startComponentInPage(new OatList<>("team", () -> {
                    reads.incrementAndGet();
                    return TEAM;
                }, Person::name)
                .setSubtitle(Person::role)
                .setTrailing((id, person) -> new Label(id, person.map(Person::role)))
                .setEmptyState(id -> new Label(id, "Nobody here")));

        assertThat(tags("rows")).hasSize(2);
        assertThat(reads).hasValue(1);
    }

    @Test
    void withoutAnActionTheRowsHaveNoLinkAtAll() {
        OatList<Person> list = new OatList<>("team", Model.ofList(TEAM), Person::name);
        tester.startComponentInPage(list);

        // Not a disabled AjaxLink, which would want a markup id it can't render
        List<Component> links = new ArrayList<>();
        list.visitChildren(AjaxLink.class, (link, visit) -> links.add(link));
        assertThat(links).isEmpty();
    }

    @Test
    void aMissingTitleIsEmptyAndMissingRowsAreNone() {
        tester.startComponentInPage(new OatList<>("team", Model.ofList(List.of(new Person(null, "Guest"))), Person::name));
        assertThat(tester.getTagByWicketId("title").getValue()).isEmpty();

        tester.startComponentInPage(new OatList<Person>("team", () -> null, Person::name)
                .setEmptyState(id -> new Label(id, "Nobody here")));
        assertThat(tags("list")).isEmpty();
        assertThat(tester.getTagByWicketId("empty").getValue()).isEqualTo("Nobody here");
    }

    @Test
    void theListCanBeUpdatedOverAjax() {
        tester.startComponentInPage(new OatList<>("team", Model.ofList(TEAM), Person::name));

        assertThat(tester.getTagByWicketId("team").getAttribute("id")).isNotBlank();
    }

    @Test
    void reusedRowsShowTheirRowsAfterTheListChanges() {
        IModel<List<Person>> people = Model.ofList(new ArrayList<>(TEAM));
        OatList<Person> list = new OatList<>("team", people, Person::name)
                .setTrailing((id, person) -> new Label(id, person.map(Person::name)))
                .setReuseItems(true);
        tester.startComponentInPage(list);

        people.getObject().remove(0);
        tester.startComponentInPage(list);
        assertThat(tags("title")).extracting(TagTester::getValue).containsExactly("Alan Turing");
        assertThat(tags("trailing")).extracting(TagTester::getValue).containsExactly("Alan Turing");
    }

    @Test
    void factoriesMustUseTheIdTheyAreGiven() {
        OatList<Person> list = new OatList<>("team", Model.ofList(TEAM), Person::name)
                .setTrailing((id, person) -> new Label("badge", "Team"));
        // Thrown while the rows render, so Wicket wraps it
        assertThatThrownBy(() -> tester.startComponentInPage(list)).hasStackTraceContaining("passed to the factory (\"trailing\")");

        assertThatIllegalArgumentException().isThrownBy(() -> new OatList<>("team", Model.ofList(TEAM), Person::name)
                .setEmptyState(id -> new Label("nobody", "Nobody here"))).withMessageContaining("\"empty\"");
    }
}

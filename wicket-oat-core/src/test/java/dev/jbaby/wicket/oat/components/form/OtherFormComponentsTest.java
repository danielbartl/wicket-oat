package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class OtherFormComponentsTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    @Test
    void testOatColorField() {
        OatColorField field = new OatColorField("id", "Color", Model.of("#ff0000"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("color");
    }

    @Test
    void testOatDateField() {
        OatDateField field = new OatDateField("id", "Date", Model.of(LocalDate.of(2023, 10, 27)));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("date");
    }

    @Test
    void testOatDateFieldFactoryWithModelLabelAndHelper() {
        OatDateField field = Oat.Components.dateField("id", Model.of("Date"), Model.of(LocalDate.of(2023, 10, 27)), Model.of("Pick a date"));
        tester.startComponentInPage(field);
        tester.assertLabel("id:container:label", "Date");
        tester.assertLabel("id:container:feedback", "Pick a date");
    }

    @Test
    void testOatDateTimeLocalField() {
        OatDateTimeLocalField field = new OatDateTimeLocalField("id", "DateTime", Model.of(LocalDateTime.of(2023, 10, 27, 10, 30)));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("datetime-local");
    }

    @Test
    void testOatEmailField() {
        OatEmailField field = new OatEmailField("id", "Email", Model.of("test@example.com"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("email");
    }

    @Test
    void testOatFileUpload() {
        OatFileUpload field = new OatFileUpload("id", "File", new ListModel<>());
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("file");
    }

    @Test
    void testOatNumberField() {
        OatNumberField field = new OatNumberField("id", "Number", Model.of(42));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("number");
    }

    @Test
    void testOatPasswordField() {
        OatPasswordField field = new OatPasswordField("id", "Password", Model.of("secret"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("password");
    }

    @Test
    void testOatRangeField() {
        OatRangeField<Integer> field = new OatRangeField<>("id", "Range", Model.of(10));
        field.setMin(0).setMax(100);
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("range");
    }

    @Test
    void testOatSearchField() {
        OatSearchField field = new OatSearchField("id", "Search", Model.of("query"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("search");
    }

    @Test
    void testOatTelField() {
        OatTelField field = new OatTelField("id", "Tel", Model.of("12345678"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("tel");
    }

    @Test
    void testOatTimeField() {
        OatTimeField field = new OatTimeField("id", "Time", Model.of(LocalTime.of(10, 30)));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("time");
    }

    @Test
    void testOatUrlField() {
        OatUrlField field = new OatUrlField("id", "URL", Model.of("https://example.com"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("url");
    }

    @Test
    void testOatDropdownChoice() {
        OatDropdownChoice<String> field = new OatDropdownChoice<String>("id", "Select", Model.of("A"), Model.ofList(Arrays.asList("A", "B")));
        tester.startComponentInPage(field);
        TagTester select = tester.getTagByWicketId("field");
        assertThat(select.getName()).isEqualTo("select");
    }

    @Test
    void testOatDropdownChoiceFactoryWithRenderer() {
        OatDropdownChoice<String> field = Oat.Components.dropdownChoice("id", Model.of("Select"), Model.of("A"),
                Model.ofList(Arrays.asList("A", "B")), new ChoiceRenderer<>());
        tester.startComponentInPage(field);
        TagTester select = tester.getTagByWicketId("field");
        assertThat(select.getName()).isEqualTo("select");
    }

    @Test
    void testOatSwitch() {
        OatSwitch field = new OatSwitch("id", "Switch", Model.of(true));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("checkbox");
        assertThat(input.getAttribute("role")).isEqualTo("switch");
    }

    @Test
    void testOatMonthField() {
        OatMonthField field = new OatMonthField("id", "Month", Model.of("2023-10"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("month");
    }

    @Test
    void testOatWeekField() {
        OatWeekField field = new OatWeekField("id", "Week", Model.of("2023-W43"));
        tester.startComponentInPage(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("type")).isEqualTo("week");
    }
}

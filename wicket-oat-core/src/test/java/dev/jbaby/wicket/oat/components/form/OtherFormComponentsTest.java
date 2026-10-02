package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.Component;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Locale;

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

    private String renderedValue(Component field) {
        tester.startComponentInPage(field);
        return tester.getTagByWicketId("field").getAttribute("value");
    }

    @Test
    void html5FieldsRenderTheirModelValue() {
        assertThat(renderedValue(new OatColorField("id", "Color", Model.of("#ff0000")))).isEqualTo("#ff0000");
        assertThat(renderedValue(new OatSearchField("id", "Search", Model.of("query")))).isEqualTo("query");
        assertThat(renderedValue(new OatTelField("id", "Tel", Model.of("12345678")))).isEqualTo("12345678");
        assertThat(renderedValue(new OatMonthField("id", "Month", Model.of("2023-10")))).isEqualTo("2023-10");
        assertThat(renderedValue(new OatWeekField("id", "Week", Model.of("2023-W43")))).isEqualTo("2023-W43");
    }

    @Test
    void dateAndTimeFieldsRenderIsoValuesRegardlessOfLocale() {
        tester.getSession().setLocale(Locale.US);

        assertThat(renderedValue(new OatDateField("id", "Date", Model.of(LocalDate.of(2023, 10, 27)))))
                .isEqualTo("2023-10-27");
        assertThat(renderedValue(new OatTimeField("id", "Time", Model.of(LocalTime.of(10, 30)))))
                .isEqualTo("10:30");
        assertThat(renderedValue(new OatDateTimeLocalField("id", "DateTime", Model.of(LocalDateTime.of(2023, 10, 27, 10, 30)))))
                .isEqualTo("2023-10-27T10:30");
    }

    @Test
    void timeFieldsTruncateToMillisecondsWhichIsTheMostHtmlAllows() {
        assertThat(renderedValue(new OatTimeField("id", "Time", Model.of(LocalTime.of(10, 30, 15, 123_456_789)))))
                .isEqualTo("10:30:15.123");
        assertThat(renderedValue(new OatDateTimeLocalField("id", "DateTime", Model.of(LocalDateTime.of(2023, 10, 27, 10, 30, 15, 123_456_789)))))
                .isEqualTo("2023-10-27T10:30:15.123");
    }

    @Test
    void dateAndTimeFieldsParseTheIsoValuesBrowsersSubmit() {
        tester.getSession().setLocale(Locale.US);
        Model<LocalDate> date = Model.of(LocalDate.of(2023, 10, 27));
        Model<LocalTime> time = Model.of(LocalTime.of(10, 30));
        Model<LocalDateTime> dateTime = Model.of(LocalDateTime.of(2023, 10, 27, 10, 30));

        Form<Void> form = new Form<>("form");
        form.add(new OatDateField("date", "Date", date),
                new OatTimeField("time", "Time", time),
                new OatDateTimeLocalField("dateTime", "DateTime", dateTime));
        tester.startComponentInPage(form, Markup.of(
                "<form wicket:id='form'><div wicket:id='date'></div><div wicket:id='time'></div><div wicket:id='dateTime'></div></form>"));

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("date:container:field", "2026-12-24");
        formTester.setValue("time:container:field", "14:05");
        formTester.setValue("dateTime:container:field", "2026-12-24T14:05:30");
        formTester.submit();

        tester.assertNoErrorMessage();
        assertThat(date.getObject()).isEqualTo(LocalDate.of(2026, 12, 24));
        assertThat(time.getObject()).isEqualTo(LocalTime.of(14, 5));
        assertThat(dateTime.getObject()).isEqualTo(LocalDateTime.of(2026, 12, 24, 14, 5, 30));
    }
}

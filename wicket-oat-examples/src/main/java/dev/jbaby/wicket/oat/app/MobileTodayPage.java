package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.app.DemoAbsences.Absence;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/** The "Today" tab: who's out today, and who's out next. */
public class MobileTodayPage extends MobileBasePage {

    public MobileTodayPage() {
        LocalDate today = DemoAbsences.today();
        add(new Label("date", () -> DateTimeFormatter.ofPattern("EEEE, d MMMM", getLocale()).format(today)));

        IModel<List<Absence>> out = () -> DemoAbsences.on(today);
        add(new Label("inCount", () -> DemoAbsences.TEAM.size() - out.getObject().size()));
        add(new Label("teamSize", DemoAbsences.TEAM.size()));
        add(new ListView<Absence>("out", out) {
            @Override
            protected void populateItem(ListItem<Absence> item) {
                Absence absence = item.getModelObject();
                populateAbsence(item, absence, absence.end().equals(today)
                        ? (absence.halfDayEnd() ? "Half day today" : "Back tomorrow")
                        : "Until " + shortDate(absence.end()));
            }
        });
        add(new WebMarkupContainer("nobody") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(out.getObject().isEmpty());
            }
        });

        IModel<List<Absence>> next = () -> DemoAbsences.get().stream()
                .filter(absence -> absence.start().isAfter(today))
                .sorted(Comparator.comparing(Absence::start))
                .limit(4)
                .toList();
        add(new ListView<Absence>("next", next) {
            @Override
            protected void populateItem(ListItem<Absence> item) {
                Absence absence = item.getModelObject();
                populateAbsence(item, absence, absence.start().equals(absence.end())
                        ? shortDate(absence.start())
                        : shortDate(absence.start()) + " – " + shortDate(absence.end()));
            }
        });
    }

    private void populateAbsence(ListItem<Absence> item, Absence absence, String when) {
        item.add(new Label("name", absence.person()));
        item.add(new Label("when", when + (absence.tentative() ? " · tentative" : "")));
        item.add(new Label("type", absence.type().label)
                .add(AttributeModifier.replace("data-variant", absence.type() == DemoAbsences.Type.AWAY ? "secondary" : null)));
    }
}

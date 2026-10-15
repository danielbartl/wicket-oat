package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatMeter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** The "Capacity" tab: how many of the team are in on each of the next working days. */
public class MobileCapacityPage extends MobileBasePage {

    private static final int WORKING_DAYS = 10;

    public MobileCapacityPage() {
        int team = DemoAbsences.TEAM.size();
        add(new ListView<LocalDate>("days", Model.ofList(workingDays(DemoAbsences.today(), WORKING_DAYS))) {
            @Override
            protected void populateItem(ListItem<LocalDate> item) {
                LocalDate day = item.getModelObject();
                int in = DemoAbsences.inOn(day);
                item.add(new Label("day", shortDate(day)));
                item.add(new Label("count", in + " of " + team + " in"));
                // Green from 5 in, amber from 3, red below
                item.add(new OatMeter("meter", Model.of(in), Model.of(0), Model.of(team), Model.of(3), Model.of(5), Model.of(team)));
            }
        });
    }

    private static List<LocalDate> workingDays(LocalDate from, int count) {
        List<LocalDate> days = new ArrayList<>();
        for (LocalDate day = from; days.size() < count; day = day.plusDays(1)) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days.add(day);
            }
        }
        return days;
    }
}

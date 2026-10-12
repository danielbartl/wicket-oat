package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.app.DemoAbsences.Absence;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * The "Team" tab: tentative absences to confirm, and each member's vacation days left.
 * Its tab's badge counts the tentative absences; confirming one re-renders the bar.
 */
public class MobileTeamPage extends MobileBasePage {

    private static final int VACATION_DAYS = 25;

    public MobileTeamPage() {
        WebMarkupContainer team = new WebMarkupContainer("team");
        team.setOutputMarkupId(true);
        add(team);

        IModel<List<Absence>> tentative = DemoAbsences::tentative;
        team.add(new ListView<Absence>("tentative", tentative) {
            @Override
            protected void populateItem(ListItem<Absence> item) {
                Absence absence = item.getModelObject();
                item.add(new Label("name", absence.person()));
                item.add(new Label("what", absence.type().label + ", " + (absence.start().equals(absence.end())
                        ? shortDate(absence.start())
                        : shortDate(absence.start()) + " – " + shortDate(absence.end()))));
                item.add(Oat.Components.button("confirm", "Confirm", target -> {
                    DemoAbsences.confirm(absence);
                    success("Confirmed " + absence.person() + "'s " + absence.type().label.toLowerCase() + ".");
                    // The list, the days left, and the Team tab's badge
                    target.add(team, bottomNav);
                }).setSize(ButtonBehavior.Size.SMALL).setStyle(ButtonBehavior.Style.OUTLINE));
            }
        });
        team.add(new WebMarkupContainer("allConfirmed") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(tentative.getObject().isEmpty());
            }
        });

        team.add(new ListView<String>("members", Model.ofList(DemoAbsences.TEAM)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String member = item.getModelObject();
                item.add(new Label("name", member));
                item.add(new Label("left", () -> (VACATION_DAYS - vacationDays(member)) + " days left"));
            }
        });
    }

    private static long vacationDays(String member) {
        return DemoAbsences.get().stream()
                .filter(absence -> absence.person().equals(member) && absence.type() == DemoAbsences.Type.VACATION)
                .mapToLong(absence -> ChronoUnit.DAYS.between(absence.start(), absence.end()) + 1)
                .sum();
    }
}

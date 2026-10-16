package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.app.DemoAbsences.Absence;
import dev.jbaby.wicket.oat.app.DemoAbsences.Type;
import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.components.OatList;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/** The "Today" tab: who's out today, and who's out next. */
public class MobileTodayPage extends MobileBasePage {

    public MobileTodayPage() {
        LocalDate today = today();
        add(new Label("date", () -> DateTimeFormatter.ofPattern("EEEE, d MMMM", getLocale()).format(today)));

        add(new Label("inCount", () -> DemoAbsences.inOn(today)));
        add(new Label("teamSize", DemoAbsences.TEAM.size()));
        add(new OatList<>("out", () -> DemoAbsences.on(today), Absence::person)
                .setSubtitle(absence -> when(absence, absence.end().equals(today)
                        ? (absence.halfDayEnd() ? "Half day today" : "Back tomorrow")
                        : "Until " + shortDate(absence.end())))
                .setTrailing(MobileTodayPage::typeBadge)
                .setEmptyState(id -> new Label(id, "Everyone's in.").add(AttributeModifier.replace("class", "text-light"))));

        add(new OatList<>("next", () -> DemoAbsences.get().stream()
                .filter(absence -> absence.start().isAfter(today))
                .sorted(Comparator.comparing(Absence::start))
                .limit(4)
                .toList(), Absence::person)
                .setSubtitle(absence -> when(absence, absence.start().equals(absence.end())
                        ? shortDate(absence.start())
                        : shortDate(absence.start()) + " – " + shortDate(absence.end())))
                .setTrailing(MobileTodayPage::typeBadge));
    }

    private static String when(Absence absence, String dates) {
        return dates + (absence.tentative() ? " · tentative" : "");
    }

    private static OatBadge typeBadge(String id, IModel<Absence> absence) {
        Type type = absence.getObject().type();
        return new OatBadge(id, type.label, type == Type.AWAY ? OatVariant.SECONDARY : OatVariant.DEFAULT);
    }
}

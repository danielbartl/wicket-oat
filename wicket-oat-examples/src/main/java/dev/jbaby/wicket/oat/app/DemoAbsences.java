package dev.jbaby.wicket.oat.app;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** The mobile "Time off" demo's team and their absences, kept per session. */
final class DemoAbsences {

    static final List<String> TEAM = List.of("You", "Mira Novak", "Jonas Berger", "Lea Hofer", "Samir Haddad", "Anna Weiss");

    enum Type {
        VACATION("Vacation"), AWAY("Away");

        final String label;

        Type(String label) {
            this.label = label;
        }
    }

    record Absence(String person, Type type, LocalDate start, LocalDate end,
                   boolean tentative, boolean halfDayStart, boolean halfDayEnd) implements Serializable {

        boolean covers(LocalDate day) {
            return !day.isBefore(start) && !day.isAfter(end);
        }

        Absence confirmed() {
            return new Absence(person, type, start, end, false, halfDayStart, halfDayEnd);
        }
    }

    private static final MetaDataKey<ArrayList<Absence>> ABSENCES = new MetaDataKey<>() {
    };

    private DemoAbsences() {
    }

    /** Today in the app's time zone (its {@link Clock} bean), not the server's. */
    static LocalDate today() {
        AppClock appClock = new AppClock();
        Injector.get().inject(appClock);
        return LocalDate.now(appClock.clock);
    }

    /** How many of the team are in on a day: people, as one can have overlapping absences. */
    static int inOn(LocalDate day) {
        return TEAM.size() - (int) on(day).stream().map(Absence::person).distinct().count();
    }

    static List<Absence> get() {
        ArrayList<Absence> absences = Session.get().getMetaData(ABSENCES);
        if (absences == null) {
            LocalDate today = today();
            absences = new ArrayList<>(List.of(
                    new Absence("Mira Novak", Type.VACATION, today.minusDays(2), today.plusDays(4), false, false, false),
                    new Absence("Jonas Berger", Type.AWAY, today, today, false, false, true),
                    new Absence("Lea Hofer", Type.AWAY, today.plusDays(1), today.plusDays(1), true, false, false),
                    new Absence("Samir Haddad", Type.VACATION, today.plusDays(2), today.plusDays(6), true, false, false),
                    new Absence("Anna Weiss", Type.VACATION, today.plusDays(10), today.plusDays(14), false, false, false)));
            Session.get().setMetaData(ABSENCES, absences);
        }
        return absences;
    }

    static List<Absence> on(LocalDate day) {
        return get().stream().filter(a -> a.covers(day)).toList();
    }

    static List<Absence> tentative() {
        return get().stream().filter(Absence::tentative).toList();
    }

    static void add(Absence absence) {
        get().add(absence);
    }

    static void confirm(Absence absence) {
        List<Absence> absences = get();
        int index = absences.indexOf(absence);
        if (index >= 0) {
            absences.set(index, absence.confirmed());
        }
    }

    private static final class AppClock {
        @SpringBean
        Clock clock;
    }
}

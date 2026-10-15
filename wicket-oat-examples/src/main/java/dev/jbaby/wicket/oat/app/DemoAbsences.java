package dev.jbaby.wicket.oat.app;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /** How many of the team are in on a day: people, as one can have overlapping absences. */
    static int inOn(LocalDate day) {
        return TEAM.size() - (int) on(day).stream().map(Absence::person).distinct().count();
    }

    /** Gives the session its demo absences, around the given day, unless it has them already. */
    static void seed(LocalDate today) {
        if (Session.get().getMetaData(ABSENCES) == null) {
            Session.get().setMetaData(ABSENCES, new ArrayList<>(List.of(
                    new Absence("Mira Novak", Type.VACATION, today.minusDays(2), today.plusDays(4), false, false, false),
                    new Absence("Jonas Berger", Type.AWAY, today, today, false, false, true),
                    new Absence("Lea Hofer", Type.AWAY, today.plusDays(1), today.plusDays(1), true, false, false),
                    new Absence("Samir Haddad", Type.VACATION, today.plusDays(2), today.plusDays(6), true, false, false),
                    new Absence("Anna Weiss", Type.VACATION, today.plusDays(10), today.plusDays(14), false, false, false))));
        }
    }

    static List<Absence> get() {
        return Objects.requireNonNull(Session.get().getMetaData(ABSENCES), "MobileBasePage seeds the absences");
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
}

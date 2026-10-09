package dev.jbaby.wicket.oat.app;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

import java.util.ArrayList;
import java.util.List;

/** The App Shell demo's to-do list, kept per session, so the sidebar can show its count. */
final class DemoTasks {

    private static final MetaDataKey<ArrayList<String>> TASKS = new MetaDataKey<>() {
    };

    private DemoTasks() {
    }

    static List<String> get() {
        ArrayList<String> tasks = Session.get().getMetaData(TASKS);
        if (tasks == null) {
            tasks = new ArrayList<>(List.of("Send the Q3 invoices", "Renew the TLS certificate",
                    "Review the pull request", "Book the team offsite"));
            Session.get().setMetaData(TASKS, tasks);
        }
        return tasks;
    }

    static void reset() {
        Session.get().setMetaData(TASKS, null);
    }
}

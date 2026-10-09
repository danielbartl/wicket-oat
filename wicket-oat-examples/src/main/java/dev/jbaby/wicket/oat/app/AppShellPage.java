package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import dev.jbaby.wicket.oat.components.OatConfirmDialog;
import dev.jbaby.wicket.oat.components.OatEmptyState;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * Busy buttons, confirming an action, lazy loading, and a sidebar badge that follows
 * the data (the task count next to "App Shell").
 */
public class AppShellPage extends BasePage {

    public AppShellPage() {
        add(Oat.Behaviors.feedbackToasts());

        // A slow action: the button shows a spinner and ignores clicks until it's done
        add(Oat.Components.button("slowSave", "Save (takes 2 seconds)", target -> {
            pause(2000);
            success("Saved once, however often you clicked.");
        }).setBusyIndicator(true));

        // Confirm before deleting; one dialog serves every row
        OatConfirmDialog confirm = Oat.Components.confirmDialog("confirm");
        add(confirm);

        WebMarkupContainer tasks = new WebMarkupContainer("tasks");
        tasks.setOutputMarkupId(true);
        add(tasks);
        tasks.add(new ListView<>("task", (IModel<List<String>>) DemoTasks::get) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String task = item.getModelObject();
                item.add(new Label("name", task));
                item.add(Oat.Components.button("delete", "Delete", target ->
                        confirm.ask(target, "Delete “" + task + "”?", "This can't be undone.", t -> {
                            DemoTasks.get().remove(task);
                            success("Deleted “" + task + "”.");
                            t.add(tasks, sidebar); // the sidebar's badge shows the new count
                        }))
                        .setSize(ButtonBehavior.Size.SMALL)
                        .setStyle(ButtonBehavior.Style.OUTLINE)
                        .setVariant(OatVariant.DANGER));
            }
        });
        tasks.add(new OatEmptyState("empty", Model.of("All done"), Model.of("Restore the list to try again.")) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(DemoTasks.get().isEmpty());
            }
        });
        add(Oat.Components.button("restore", "Restore tasks", target -> {
            DemoTasks.reset();
            target.add(tasks, sidebar);
        }).setStyle(ButtonBehavior.Style.OUTLINE));

        // Slow content, loaded after the page is shown
        add(Oat.Components.lazyLoad("revenue", id -> {
            pause(1500);
            return new Fragment(id, "revenueFragment", this);
        }).setPlaceholder(SkeletonBehavior.Shape.LINE, 2));
        add(Oat.Components.lazyLoad("activity", id -> {
            pause(2500);
            return new Fragment(id, "activityFragment", this);
        }).setPlaceholder(SkeletonBehavior.Shape.LINE, 4));
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

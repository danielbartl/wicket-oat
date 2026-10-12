package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.components.BottomNavItem.Icon;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

/**
 * An OatMobileLayout page whose subclasses are its bottom bar's tabs: the markup is
 * inherited, so each renders the same bar with a different current page.
 */
public class MobileTestPage extends OatMobileLayout {

    public static class Today extends MobileTestPage {
    }

    public static class Team extends MobileTestPage {
    }

    /** Not a tab's target, but part of the Team tab. */
    public static class Member extends MobileTestPage {
    }

    public static class Add extends MobileTestPage {
    }

    /** A page the test's authorization strategy denies; never rendered. */
    public static class Secret extends MobileTestPage {
    }

    /** A bookmarkable page whose tabs differ only in their parameters. */
    public static class Project extends MobileTestPage {

        public Project(PageParameters parameters) {
            super(parameters);
        }

        @Override
        protected IModel<List<BottomNavItem>> bottomNavItemsModel() {
            return Model.ofList(List.of(
                    BottomNavItem.of("Project 1", Project.class, new PageParameters().add("id", 1)),
                    BottomNavItem.of("Project 2", Project.class, new PageParameters().add("id", 2))));
        }
    }

    public MobileTestPage() {
    }

    public MobileTestPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected IModel<List<BottomNavItem>> bottomNavItemsModel() {
        return Model.ofList(List.of(
                BottomNavItem.of("Today", Today.class).withIcon(Icon.sprite("calendar")),
                BottomNavItem.of("Add absence", Add.class).withIcon(Icon.named("plus")).asPrimary(),
                BottomNavItem.of("Team", Team.class).withIcon(Icon.named("users"))
                        .withActive(page -> page instanceof Team || page instanceof Member),
                BottomNavItem.of("Secret", Secret.class).withIcon(Icon.named("lock"))));
    }
}

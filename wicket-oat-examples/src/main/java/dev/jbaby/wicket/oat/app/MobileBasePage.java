package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.BottomNavItem;
import dev.jbaby.wicket.oat.components.BottomNavItem.Icon;
import dev.jbaby.wicket.oat.components.OatBottomNav;
import dev.jbaby.wicket.oat.components.OatMobileLayout;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The mobile "Time off" demo: a phone-sized app on {@code OatMobileLayout}, with a bottom
 * tab bar - Today, Capacity, Team - and a round "+" for adding an absence.
 */
public abstract class MobileBasePage extends OatMobileLayout {

    private static final CssResourceReference EXAMPLES_CSS = new CssResourceReference(BasePage.class, "examples.css");

    /** The "+" of the primary item, as inline SVG: markup the app trusts, never user input. */
    private static final String PLUS = "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\""
            + " stroke-linecap=\"round\"><path d=\"M5 12h14\"/><path d=\"M12 5v14\"/></svg>";

    protected MobileBasePage() {
        // Feedback as toasts, including "Absence added" on the page after the form
        add(Oat.Behaviors.feedbackToasts());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(EXAMPLES_CSS));
    }

    @Override
    protected @NonNull IModel<String> appTitleModel() {
        return Model.of("Time off - Wicket Oat Demo");
    }

    @Override
    protected @NonNull IModel<List<BottomNavItem>> bottomNavItemsModel() {
        // The tabs' icons are symbols of the sprite in MobileBasePage.html
        return Model.ofList(List.of(
                BottomNavItem.of("Today", MobileTodayPage.class).withIcon(Icon.sprite("tab-today")),
                BottomNavItem.of("Capacity", MobileCapacityPage.class).withIcon(Icon.sprite("tab-capacity")),
                // The tentative absences waiting to be confirmed; MobileTeamPage re-renders the bar after a confirm
                BottomNavItem.of("Team", MobileTeamPage.class).withIcon(Icon.sprite("tab-team"))
                        .withBadge(() -> DemoAbsences.tentative().size()),
                BottomNavItem.of("Add absence", MobileAddAbsencePage.class).withIcon(Icon.svg(PLUS)).asPrimary()));
    }

    @Override
    protected OatBottomNav newBottomNav(String id, IModel<List<BottomNavItem>> items) {
        // A demo visited on desktops too: keep the bar under the phone-sized column
        return super.newBottomNav(id, items).setAlwaysVisible(true);
    }

    /** A short date in the page's language, e.g. "Mon 12 Oct". */
    protected String shortDate(LocalDate date) {
        return DateTimeFormatter.ofPattern("EEE d MMM", getLocale()).format(date);
    }
}

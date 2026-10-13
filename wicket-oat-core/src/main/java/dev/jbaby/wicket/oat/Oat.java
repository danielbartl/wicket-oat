package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.behaviors.*;
import dev.jbaby.wicket.oat.components.*;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * Shortcuts for Oat behaviors ({@link Behaviors}), for the components driven by a callback
 * ({@link Components}, also available as static factories on the classes themselves, e.g.
 * {@link OatButton#onClick(String, String, SerializableConsumer)}), for tabs
 * ({@link #tab}) and for toasts ({@link #toast}). Every other component is created with
 * its constructor, like any Wicket component.
 */
public final class Oat {

    private Oat() {}

    /**
     * Factory for Oat behaviors.
     */
    public static final class Behaviors {
        private Behaviors() {}

        public static AccordionBehavior accordion() {
            return new AccordionBehavior();
        }

        public static AccordionBehavior accordion(boolean exclusive) {
            return new AccordionBehavior(exclusive);
        }

        public static AlertBehavior alert() {
            return new AlertBehavior();
        }

        public static AlertBehavior alert(OatVariant variant) {
            return new AlertBehavior(variant);
        }

        public static BadgeBehavior badge() {
            return new BadgeBehavior();
        }

        public static BadgeBehavior badge(OatVariant variant) {
            return new BadgeBehavior(variant);
        }

        public static ButtonBehavior button() {
            return new ButtonBehavior();
        }

        public static ButtonBehavior button(OatVariant variant) {
            return new ButtonBehavior(variant);
        }

        public static ButtonGroupBehavior buttonGroup() {
            return new ButtonGroupBehavior();
        }

        public static CardBehavior card() {
            return new CardBehavior();
        }

        public static FieldBehavior field() {
            return new FieldBehavior();
        }

        /**
         * Shows a spinner on an Ajax link or button while its request runs, and ignores
         * further clicks meanwhile. Oat's own buttons turn it on with {@code setBusyIndicator(true)}.
         */
        public static AjaxBusyBehavior ajaxBusy() {
            return new AjaxBusyBehavior();
        }

        /** A menu of actions opened by right-click (or Shift+F10) on the component; add them with {@code addAction}. */
        public static ContextMenuBehavior contextMenu() {
            return new ContextMenuBehavior();
        }

        /** Joins a {@code <fieldset>}'s inputs, buttons and label addons into one control. */
        public static InputGroupBehavior inputGroup() {
            return new InputGroupBehavior();
        }

        /** A row of Oat's 12-column grid; give its children a {@link #col(int)}. */
        public static GridBehavior row() {
            return GridBehavior.row();
        }

        /** A grid column spanning {@code span} of 12 columns. */
        public static GridBehavior col(int span) {
            return GridBehavior.col(span);
        }

        /** A grid column spanning {@code span} of 12 columns, shifted right by {@code offset} columns. */
        public static GridBehavior col(int span, int offset) {
            return GridBehavior.col(span, offset);
        }

        /** Centers content at Oat's maximum page width. */
        public static GridBehavior container() {
            return GridBehavior.container();
        }

        /** Lays out children in a wrapping row with a gap. */
        public static StackBehavior hstack() {
            return new StackBehavior(StackBehavior.Direction.HORIZONTAL);
        }

        /** Lays out children in a column with a gap. */
        public static StackBehavior vstack() {
            return new StackBehavior(StackBehavior.Direction.VERTICAL);
        }

        public static HintBehavior hint() {
            return new HintBehavior();
        }

        public static SwitchBehavior oatSwitch() {
            return new SwitchBehavior();
        }

        public static SkeletonBehavior skeleton() {
            return new SkeletonBehavior();
        }

        public static SkeletonBehavior skeleton(SkeletonBehavior.Shape shape) {
            return new SkeletonBehavior(shape);
        }

        public static SpinnerBehavior spinner() {
            return new SpinnerBehavior();
        }

        public static SpinnerBehavior spinner(SpinnerBehavior.Size size) {
            return new SpinnerBehavior(size);
        }

        public static SpinnerBehavior spinner(IModel<SpinnerBehavior.Size> sizeModel) {
            return new SpinnerBehavior(sizeModel);
        }

        public static TooltipBehavior tooltip(String text) {
            return new TooltipBehavior(text);
        }

        public static TooltipBehavior tooltip(IModel<String> textModel) {
            return new TooltipBehavior(textModel);
        }

        public static ClientSideClickBehavior clientSideClick(String javascript) {
            return new ClientSideClickBehavior(javascript);
        }

        /** Applies the current user's theme ({@code data-theme}); put it on the {@code <html>} tag. */
        public static OatThemeBehavior theme() {
            return new OatThemeBehavior();
        }

        /** Pins a component to the given theme, whatever the user chose. */
        public static OatThemeBehavior theme(OatTheme theme) {
            return new OatThemeBehavior(theme);
        }

        /** Applies the configured density ({@code data-density}); put it on the {@code <html>} tag. */
        public static OatDensityBehavior density() {
            return new OatDensityBehavior();
        }

        /** Pins a component to the given density, e.g. a compact table on a default page. */
        public static OatDensityBehavior density(OatDensity density) {
            return new OatDensityBehavior(density);
        }

        public static FeedbackToastsBehavior feedbackToasts() {
            return new FeedbackToastsBehavior();
        }

        public static FeedbackToastsBehavior feedbackToasts(IFeedbackMessageFilter filter) {
            return new FeedbackToastsBehavior(filter);
        }
    }

    /**
     * Factories for the Oat components whose behavior comes from a callback - a click
     * handler, a list item's content - so that a lambda can stand in for a subclass. Each
     * delegates to a static factory on the component's own class, e.g.
     * {@link OatButton#onClick(String, String, SerializableConsumer)}; every other
     * component is created with its constructor.
     */
    public static final class Components {
        private Components() {}

        /** @see OatButton#onClick(String, String, SerializableConsumer) */
        public static OatButton button(String id, String label, SerializableConsumer<AjaxRequestTarget> onClick) {
            return OatButton.onClick(id, label, onClick);
        }

        /** @see OatButton#onClick(String, IModel, SerializableConsumer) */
        public static OatButton button(String id, IModel<String> label, SerializableConsumer<AjaxRequestTarget> onClick) {
            return OatButton.onClick(id, label, onClick);
        }

        /** @see OatSubmitButton#onSubmit(String, String, SerializableConsumer) */
        public static OatSubmitButton submitButton(String id, String label, SerializableConsumer<AjaxRequestTarget> onSubmit) {
            return OatSubmitButton.onSubmit(id, label, onSubmit);
        }

        /** @see OatSubmitButton#onSubmit(String, IModel, SerializableConsumer) */
        public static OatSubmitButton submitButton(String id, IModel<String> label, SerializableConsumer<AjaxRequestTarget> onSubmit) {
            return OatSubmitButton.onSubmit(id, label, onSubmit);
        }

        /** @see OatSubmitButton#onSubmit(String, String, SerializableConsumer, SerializableConsumer) */
        public static OatSubmitButton submitButton(String id, String label, SerializableConsumer<AjaxRequestTarget> onSubmit,
                                                   SerializableConsumer<AjaxRequestTarget> onError) {
            return OatSubmitButton.onSubmit(id, label, onSubmit, onError);
        }

        /** @see OatSubmitButton#onSubmit(String, IModel, SerializableConsumer, SerializableConsumer) */
        public static OatSubmitButton submitButton(String id, IModel<String> label, SerializableConsumer<AjaxRequestTarget> onSubmit,
                                                   SerializableConsumer<AjaxRequestTarget> onError) {
            return OatSubmitButton.onSubmit(id, label, onSubmit, onError);
        }

        /** @see OatAccordion#of(String, IModel, SerializableBiConsumer) */
        public static <T> OatAccordion<T> accordion(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatAccordion.of(id, model, populateItem);
        }

        /** @see OatAccordion#of(String, IModel, boolean, SerializableBiConsumer) */
        public static <T> OatAccordion<T> accordion(String id, IModel<List<T>> model, boolean exclusive,
                                                    SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatAccordion.of(id, model, exclusive, populateItem);
        }

        /** @see OatTabs#of(String, IModel, SerializableBiConsumer, SerializableBiConsumer) */
        public static <T> OatTabs<T> tabs(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateTab,
                                          SerializableBiConsumer<ListItem<T>, T> populatePanel) {
            return OatTabs.of(id, model, populateTab, populatePanel);
        }

        /** @see OatDropdown#of(String, String, IModel, SerializableBiConsumer) */
        public static <T> OatDropdown<T> dropdown(String id, String triggerLabel, IModel<List<T>> model,
                                                  SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatDropdown.of(id, triggerLabel, model, populateItem);
        }

        /** @see OatDropdown#of(String, IModel, IModel, SerializableBiConsumer) */
        public static <T> OatDropdown<T> dropdown(String id, IModel<String> triggerLabel, IModel<List<T>> model,
                                                  SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatDropdown.of(id, triggerLabel, model, populateItem);
        }

        /** @see OatDropdown#of(String, String, IModel, SerializableFunction, SerializableBiConsumer) */
        public static <T> OatDropdown<T> dropdown(String id, String triggerLabel, IModel<List<T>> model,
                                                  SerializableFunction<T, String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
            return OatDropdown.of(id, triggerLabel, model, label, onClick);
        }

        /** @see OatDropdown#of(String, IModel, IModel, SerializableFunction, SerializableBiConsumer) */
        public static <T> OatDropdown<T> dropdown(String id, IModel<String> triggerLabel, IModel<List<T>> model,
                                                  SerializableFunction<T, String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
            return OatDropdown.of(id, triggerLabel, model, label, onClick);
        }

        /** @see OatButtonGroup#of(String, IModel, SerializableBiConsumer) */
        public static <T> OatButtonGroup<T> buttonGroup(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatButtonGroup.of(id, model, populateItem);
        }

        /** @see OatBreadcrumb#of(String, IModel, SerializableBiConsumer) */
        public static <T> OatBreadcrumb<T> breadcrumb(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatBreadcrumb.of(id, model, populateItem);
        }

        /** @see OatPagination#of(String, IModel, SerializableBiConsumer) */
        public static <T> OatPagination<T> pagination(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatPagination.of(id, model, populateItem);
        }

        /** @see OatAvatarGroup#of(String, IModel, SerializableBiConsumer) */
        public static <T> OatAvatarGroup<T> avatarGroup(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatAvatarGroup.of(id, model, populateItem);
        }

        /** @see OatAvatarGroup#of(String, IModel, OatAvatarGroup.Size, SerializableBiConsumer) */
        public static <T> OatAvatarGroup<T> avatarGroup(String id, IModel<List<T>> model, OatAvatarGroup.Size size,
                                                        SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return OatAvatarGroup.of(id, model, size, populateItem);
        }

        /** @see OatWizard#onFinish(String, SerializableConsumer) */
        public static OatWizard wizard(String id, SerializableConsumer<AjaxRequestTarget> onFinish) {
            return OatWizard.onFinish(id, onFinish);
        }

        /** @see OatLoginForm#signIn(String, SerializableBiFunction) */
        public static OatLoginForm loginForm(String id, SerializableBiFunction<String, String, Boolean> signIn) {
            return OatLoginForm.signIn(id, signIn);
        }

        /** @see OatLazyLoadPanel#of(String, SerializableFunction) */
        public static OatLazyLoadPanel<Component> lazyLoad(String id, SerializableFunction<String, ? extends Component> content) {
            return OatLazyLoadPanel.of(id, content);
        }
    }

    // --- Tabs ---

    /**
     * A tab for {@link OatTabbedPanel}: {@code Oat.tab("Profile", ProfilePanel::new)}.
     * The factory receives the panel id to use and is called only when the tab is shown.
     */
    public static ITab tab(String title, SerializableFunction<String, ? extends WebMarkupContainer> panel) {
        return tab(Model.of(title), panel);
    }

    public static ITab tab(IModel<String> title, SerializableFunction<String, ? extends WebMarkupContainer> panel) {
        return new AbstractTab(title) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
                return panel.apply(panelId);
            }
        };
    }

    // --- Actions ---

    public static void toast(AjaxRequestTarget target, String message) {
        OatToastBehavior.toast(target, message);
    }

    public static void toast(AjaxRequestTarget target, String message, OatVariant variant) {
        OatToastBehavior.toast(target, message, variant);
    }

    public static void toast(AjaxRequestTarget target, String message, OatVariant variant, String title) {
        OatToastBehavior.toast(target, message, variant, title);
    }
}

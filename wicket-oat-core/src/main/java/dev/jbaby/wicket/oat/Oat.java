package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.behaviors.*;
import dev.jbaby.wicket.oat.components.*;
import dev.jbaby.wicket.oat.components.form.*;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Collection;
import java.util.List;

/**
 * Entry point for creating Oat components and behaviors.
 * <p>
 * This factory provides a "Fast Path" for the most common UI tasks. 
 * For complex customization that requires subclassing or custom class hierarchies, 
 * you are encouraged to use the standard component constructors and Oat behaviors 
 * directly (Composition over Inheritance).
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
     * Factory for Oat components.
     */
    public static final class Components {
        private Components() {}

        public static OatAlert alert(String id) {
            return new OatAlert(id, (String) null);
        }

        public static OatAlert alert(String id, String message) {
            return new OatAlert(id, message);
        }

        public static OatAlert alert(String id, OatVariant variant) {
            return new OatAlert(id, (String) null, variant);
        }

        public static OatAlert alert(String id, String message, OatVariant variant) {
            return new OatAlert(id, message, variant);
        }

        public static OatAlert alert(String id, IModel<?> model) {
            return new OatAlert(id, model);
        }

        public static OatAlert alert(String id, IModel<?> model, OatVariant variant) {
            return new OatAlert(id, model, variant);
        }

        public static OatBadge badge(String id, String label) {
            return new OatBadge(id, label);
        }

        public static OatBadge badge(String id, String label, OatVariant variant) {
            return new OatBadge(id, label, variant);
        }

        public static OatBadge badge(String id, IModel<?> model) {
            return new OatBadge(id, model);
        }

        public static OatBadge badge(String id, IModel<?> model, OatVariant variant) {
            return new OatBadge(id, model, variant);
        }

        public static OatCard card(String id) {
            return new OatCard(id);
        }

        /** A titled form section on a {@code <fieldset>}; the legend is looked up by {@code id}. */
        public static OatFieldset fieldset(String id) {
            return new OatFieldset(id);
        }

        public static OatFieldset fieldset(String id, String legend) {
            return new OatFieldset(id, legend);
        }

        public static OatFieldset fieldset(String id, IModel<String> legend) {
            return new OatFieldset(id, legend);
        }

        public static OatAvatar avatar(String id, IModel<String> imageUrl) {
            return new OatAvatar(id, imageUrl);
        }

        public static OatAvatar avatar(String id, String initials) {
            return new OatAvatar(id, initials);
        }

        public static OatAvatar avatar(String id, IModel<String> imageUrl, IModel<String> initials, OatAvatar.Size size) {
            return new OatAvatar(id, imageUrl, initials, size);
        }

        public static <T> OatAvatarGroup<T> avatarGroup(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatAvatarGroup<T>(id, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        public static <T> OatAvatarGroup<T> avatarGroup(String id, IModel<List<T>> model, OatAvatarGroup.Size size, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatAvatarGroup<T>(id, model, size) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        public static OatProgress progress(String id, Number value) {
            return new OatProgress(id, value);
        }

        public static OatProgress progress(String id, IModel<? extends Number> valueModel) {
            return new OatProgress(id, valueModel);
        }

        public static OatProgress progress(String id, IModel<? extends Number> valueModel, IModel<? extends Number> maxModel) {
            return new OatProgress(id, valueModel, maxModel);
        }

        public static OatMeter meter(String id, Number value) {
            return new OatMeter(id, value);
        }

        public static OatMeter meter(String id, IModel<? extends Number> valueModel, IModel<? extends Number> minModel, IModel<? extends Number> maxModel,
                                      IModel<? extends Number> lowModel, IModel<? extends Number> highModel, IModel<? extends Number> optimumModel) {
            return new OatMeter(id, valueModel, minModel, maxModel, lowModel, highModel, optimumModel);
        }

        public static OatSkeleton skeleton(String id) {
            return new OatSkeleton(id);
        }

        public static OatSkeleton skeleton(String id, SkeletonBehavior.Shape shape) {
            return new OatSkeleton(id, shape);
        }

        public static OatSpinner spinner(String id) {
            return new OatSpinner(id);
        }

        public static OatSpinner spinner(String id, SpinnerBehavior.Size size) {
            return new OatSpinner(id, size);
        }

        public static OatSpinner spinner(String id, IModel<SpinnerBehavior.Size> sizeModel) {
            return new OatSpinner(id, sizeModel);
        }

        public static <T, S> OatDataTable<T, S> dataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider, long rowsPerPage) {
            return new OatDataTable<>(id, columns, dataProvider, rowsPerPage);
        }

        public static OatFeedbackPanel feedbackPanel(String id) {
            return new OatFeedbackPanel(id);
        }

        public static OatFeedbackPanel feedbackPanel(String id, IFeedbackMessageFilter filter) {
            return new OatFeedbackPanel(id, filter);
        }

        public static OatPagingNavigator pagingNavigator(String id, IPageable pageable) {
            return new OatPagingNavigator(id, pageable);
        }

        public static OatAjaxPagingNavigator ajaxPagingNavigator(String id, IPageable pageable) {
            return new OatAjaxPagingNavigator(id, pageable);
        }

        public static <T extends ITab> OatTabbedPanel<T> tabbedPanel(String id, List<T> tabs) {
            return new OatTabbedPanel<>(id, tabs);
        }

        public static OatEmptyState emptyState(String id, String title) {
            return new OatEmptyState(id, Model.of(title), null);
        }

        public static OatEmptyState emptyState(String id, String title, String message) {
            return new OatEmptyState(id, Model.of(title), Model.of(message));
        }

        public static OatEmptyState emptyState(String id, IModel<String> titleModel, IModel<String> messageModel) {
            return new OatEmptyState(id, titleModel, messageModel);
        }

        public static <T> OatButtonGroup<T> buttonGroup(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatButtonGroup<T>(id, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        public static <T> OatBreadcrumb<T> breadcrumb(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatBreadcrumb<T>(id, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        public static <T> OatPagination<T> pagination(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatPagination<T>(id, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        /** The top of a page, on a {@code <header>}: title, subtitle, breadcrumb, and the tag's content as actions. */
        public static OatPageHeader pageHeader(String id, String title) {
            return new OatPageHeader(id, title);
        }

        public static OatPageHeader pageHeader(String id, IModel<String> title) {
            return new OatPageHeader(id, title);
        }

        /** A dashboard tile for one key figure; add {@code setChange(...)} for how it changed. */
        public static OatStatCard statCard(String id, String label, IModel<?> value) {
            return new OatStatCard(id, label, value);
        }

        public static OatStatCard statCard(String id, IModel<String> label, IModel<?> value) {
            return new OatStatCard(id, label, value);
        }

        /** A read-only view of a record's fields, on a {@code <dl>}; add items with {@code addItem}/{@code addProperty}. */
        public static OatDescriptionList descriptionList(String id) {
            return new OatDescriptionList(id);
        }

        public static OatDescriptionList descriptionList(String id, IModel<?> model) {
            return new OatDescriptionList(id, model);
        }

        /** A button for the usual action joined to a menu of related ones; add them with {@code addAction}. */
        public static OatSplitButton splitButton(String id, String label, SerializableConsumer<AjaxRequestTarget> onClick) {
            return new OatSplitButton(id, label, onClick);
        }

        public static OatSplitButton splitButton(String id, IModel<String> label, SerializableConsumer<AjaxRequestTarget> onClick) {
            return new OatSplitButton(id, label, onClick);
        }

        /** A button opening a panel with any content; the factory receives the id the content must use. */
        public static OatPopover popover(String id, String triggerLabel, SerializableFunction<String, ? extends Component> content) {
            return new OatPopover(id, triggerLabel, content);
        }

        public static OatPopover popover(String id, IModel<String> triggerLabel, SerializableFunction<String, ? extends Component> content) {
            return new OatPopover(id, triggerLabel, content);
        }

        /** Events in order - a history, an audit trail - each with a title and its time. */
        public static <T> OatTimeline<T> timeline(String id, IModel<? extends List<T>> events, SerializableFunction<T, ?> title,
                                                  SerializableFunction<T, ? extends java.time.temporal.TemporalAccessor> time) {
            return new OatTimeline<>(id, events, title, time);
        }

        /** A form filled in over several steps; add them with {@code addStep}. {@code onFinish} runs after the last. */
        public static OatWizard wizard(String id, SerializableConsumer<AjaxRequestTarget> onFinish) {
            return new OatWizard(id) {
                @Override
                protected void onFinish(AjaxRequestTarget target) {
                    onFinish.accept(target);
                }
            };
        }

        /**
         * A sign-in form; {@code signIn} checks the username and password and signs the user
         * in. Subclass {@link OatLoginForm} to use the remember-me value or change what happens after.
         */
        public static OatLoginForm loginForm(String id, SerializableBiFunction<String, String, Boolean> signIn) {
            return new OatLoginForm(id) {
                @Override
                protected boolean signIn(String username, String password, boolean rememberMe) {
                    return Boolean.TRUE.equals(signIn.apply(username, password));
                }
            };
        }

        /** A list beside the selected item's details; set them with {@code setMaster}/{@code setDetail}. */
        public static <T> OatMasterDetail<T> masterDetail(String id, IModel<T> selection) {
            return new OatMasterDetail<>(id, selection);
        }

        /** Messages with author, time and text, e.g. comments or notes. */
        public static <T> OatMessageList<T> messageList(String id, IModel<? extends List<T>> messages, SerializableFunction<T, String> author,
                                                        SerializableFunction<T, ?> text,
                                                        SerializableFunction<T, ? extends java.time.temporal.TemporalAccessor> time) {
            return new OatMessageList<>(id, messages, author, text, time);
        }

        /** A box for writing a message, with a Send button; {@code onSend} gets the text. */
        public static OatMessageInput messageInput(String id, SerializableBiConsumer<AjaxRequestTarget, String> onSend) {
            return new OatMessageInput(id, onSend);
        }

        /** A long list that appends a batch of items at a time ("Load more"). */
        public static <T> OatLoadMoreList<T> loadMoreList(String id, org.apache.wicket.markup.repeater.data.IDataProvider<T> provider, long batchSize,
                                                         SerializableBiFunction<String, IModel<T>, ? extends Component> item) {
            return new OatLoadMoreList<>(id, provider, batchSize, item);
        }

        /** A row of commands: buttons, links and menus; add them with {@code addAction}/{@code addLink}/{@code addMenu}. */
        public static OatMenuBar menuBar(String id) {
            return new OatMenuBar(id);
        }

        /** Two panes side by side, the first resizable by dragging its corner. */
        public static OatSplitLayout splitLayout(String id, SerializableFunction<String, ? extends Component> first,
                                                 SerializableFunction<String, ? extends Component> second) {
            return new OatSplitLayout(id, first, second);
        }

        /** A banner asking for consent to optional cookies; check it with {@link OatCookieConsent#isAccepted()}. */
        public static OatCookieConsent cookieConsent(String id) {
            return new OatCookieConsent(id);
        }

        /** An icon by name, e.g. {@code "pencil"}; see {@link OatIcon#names()}. */
        public static OatIcon icon(String id, String name) {
            return new OatIcon(id, name);
        }

        /** Horizontal bars comparing a value across categories. */
        public static <T> dev.jbaby.wicket.oat.components.chart.OatBarChart<T> barChart(String id, IModel<? extends List<T>> data,
                SerializableFunction<T, String> category, SerializableFunction<T, ? extends Number> value) {
            return new dev.jbaby.wicket.oat.components.chart.OatBarChart<>(id, data, category, value);
        }

        /** Columns comparing a value across ordered categories, e.g. months. */
        public static <T> dev.jbaby.wicket.oat.components.chart.OatColumnChart<T> columnChart(String id, IModel<? extends List<T>> data,
                SerializableFunction<T, String> category, SerializableFunction<T, ? extends Number> value) {
            return new dev.jbaby.wicket.oat.components.chart.OatColumnChart<>(id, data, category, value);
        }

        /** Lines over time; add up to three with {@code addSeries}. */
        public static <T> dev.jbaby.wicket.oat.components.chart.OatLineChart<T> lineChart(String id, IModel<? extends List<T>> data,
                SerializableFunction<T, String> category) {
            return new dev.jbaby.wicket.oat.components.chart.OatLineChart<>(id, data, category);
        }

        /** A whole divided into a few parts, as a ring with a legend of values and shares. */
        public static <T> dev.jbaby.wicket.oat.components.chart.OatDonutChart<T> donutChart(String id, IModel<? extends List<T>> data,
                SerializableFunction<T, String> category, SerializableFunction<T, ? extends Number> value) {
            return new dev.jbaby.wicket.oat.components.chart.OatDonutChart<>(id, data, category, value);
        }

        /** A small trend line without axes. */
        public static dev.jbaby.wicket.oat.components.chart.OatSparkline sparkline(String id, IModel<? extends List<? extends Number>> values) {
            return new dev.jbaby.wicket.oat.components.chart.OatSparkline(id, values);
        }

        /** A dialog that asks to confirm an action before it runs; see {@link OatConfirmDialog#ask}. */
        public static OatConfirmDialog confirmDialog(String id) {
            return new OatConfirmDialog(id);
        }

        /**
         * Loads slow content over Ajax right after the page is shown, with skeleton
         * placeholders until then. The factory receives the id the content must use.
         */
        public static OatLazyLoadPanel<Component> lazyLoad(String id, SerializableFunction<String, ? extends Component> content) {
            return new OatLazyLoadPanel<>(id) {
                @Override
                public Component getLazyLoadComponent(String markupId) {
                    return content.apply(markupId);
                }
            };
        }

        public static OatDialog dialog(String id, String header) {
            return new OatDialog(id, header);
        }

        public static OatDialog dialog(String id, IModel<String> header) {
            return new OatDialog(id, header);
        }

        public static OatDialog dialog(String id, String triggerLabel, String header) {
            return new OatDialog(id, triggerLabel, header);
        }

        public static OatDialog dialog(String id, IModel<String> triggerLabel, IModel<String> header) {
            return new OatDialog(id, triggerLabel, header);
        }

        public static <T> OatDropdown<T> dropdown(String id, String triggerLabel, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatDropdown<T>(id, triggerLabel, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        public static <T> OatDropdown<T> dropdown(String id, IModel<String> triggerLabel, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatDropdown<T>(id, triggerLabel, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        /**
         * A dropdown whose items show {@code label} and run {@code onClick} over Ajax when
         * chosen; the menu closes afterwards.
         */
        public static <T> OatDropdown<T> dropdown(String id, String triggerLabel, IModel<List<T>> model,
                                                  SerializableFunction<T, String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
            return dropdown(id, Model.of(triggerLabel), model, label, onClick);
        }

        public static <T> OatDropdown<T> dropdown(String id, IModel<String> triggerLabel, IModel<List<T>> model,
                                                  SerializableFunction<T, String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
            return new OatDropdown<T>(id, triggerLabel, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    item.add(new Label("label", label.apply(item.getModelObject())));
                    item.add(AjaxEventBehavior.onEvent("click", target -> {
                        onClick.accept(target, item.getModelObject());
                        close(target);
                    }));
                }
            };
        }

        public static <T> OatTabs<T> tabs(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateTab, SerializableBiConsumer<ListItem<T>, T> populatePanel) {
            return new OatTabs<T>(id, model) {
                @Override
                protected void populateTab(ListItem<T> item) {
                    populateTab.accept(item, item.getModelObject());
                }

                @Override
                protected void populatePanel(ListItem<T> item) {
                    populatePanel.accept(item, item.getModelObject());
                }
            };
        }

        public static OatButton button(String id, String label, SerializableConsumer<AjaxRequestTarget> onClick) {
            return new OatButton(id, label) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    onClick.accept(target);
                }
            };
        }

        public static OatButton button(String id, IModel<String> labelModel, SerializableConsumer<AjaxRequestTarget> onClick) {
            return new OatButton(id, labelModel) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    onClick.accept(target);
                }
            };
        }

        public static OatSubmitButton submitButton(String id, String label, SerializableConsumer<AjaxRequestTarget> onSubmit) {
            return submitButton(id, Model.of(label), onSubmit, null);
        }

        public static OatSubmitButton submitButton(String id, IModel<String> labelModel, SerializableConsumer<AjaxRequestTarget> onSubmit) {
            return submitButton(id, labelModel, onSubmit, null);
        }

        public static OatSubmitButton submitButton(String id, String label, SerializableConsumer<AjaxRequestTarget> onSubmit, SerializableConsumer<AjaxRequestTarget> onError) {
            return submitButton(id, Model.of(label), onSubmit, onError);
        }

        /**
         * @param onError called when the form doesn't validate, or {@code null} to do nothing
         */
        public static OatSubmitButton submitButton(String id, IModel<String> labelModel, SerializableConsumer<AjaxRequestTarget> onSubmit, SerializableConsumer<AjaxRequestTarget> onError) {
            return new OatSubmitButton(id, labelModel) {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    onSubmit.accept(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target) {
                    if (onError != null) {
                        onError.accept(target);
                    }
                }
            };
        }

        public static <T> OatAccordion<T> accordion(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatAccordion<T>(id, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        public static <T> OatAccordion<T> accordion(String id, IModel<List<T>> model, boolean exclusive, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatAccordion<T>(id, model, exclusive) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        // --- Form Components ---

        public static <T> OatTextField<T> textField(String id) {
            return new OatTextField<>(id);
        }

        public static <T> OatTextField<T> textField(String id, IModel<T> model) {
            return new OatTextField<>(id, model);
        }

        public static <T> OatTextField<T> textField(String id, String label, IModel<T> model) {
            return new OatTextField<>(id, label, model);
        }

        public static <T> OatTextField<T> textField(String id, IModel<String> labelModel, IModel<T> model) {
            return new OatTextField<>(id, labelModel, model);
        }

        public static <T> OatTextField<T> textField(String id, IModel<String> labelModel, IModel<T> model, IModel<String> hintModel) {
            return new OatTextField<>(id, labelModel, model, hintModel);
        }

        public static OatPasswordField passwordField(String id) {
            return new OatPasswordField(id);
        }

        public static OatPasswordField passwordField(String id, IModel<String> model) {
            return new OatPasswordField(id, model);
        }

        public static OatPasswordField passwordField(String id, String label, IModel<String> model) {
            return new OatPasswordField(id, label, model);
        }

        public static OatPasswordField passwordField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatPasswordField(id, labelModel, model);
        }

        public static OatPasswordField passwordField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatPasswordField(id, labelModel, model, hintModel);
        }

        public static OatEmailField emailField(String id) {
            return new OatEmailField(id);
        }

        public static OatEmailField emailField(String id, IModel<String> model) {
            return new OatEmailField(id, model);
        }

        public static OatEmailField emailField(String id, String label, IModel<String> model) {
            return new OatEmailField(id, label, model);
        }

        public static OatEmailField emailField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatEmailField(id, labelModel, model);
        }

        public static OatEmailField emailField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatEmailField(id, labelModel, model, hintModel);
        }

        public static <N extends Number & Comparable<N>> OatNumberField<N> numberField(String id) {
            return new OatNumberField<>(id);
        }

        public static <N extends Number & Comparable<N>> OatNumberField<N> numberField(String id, IModel<N> model) {
            return new OatNumberField<>(id, model);
        }

        public static <N extends Number & Comparable<N>> OatNumberField<N> numberField(String id, String label, IModel<N> model) {
            return new OatNumberField<>(id, label, model);
        }

        public static <N extends Number & Comparable<N>> OatNumberField<N> numberField(String id, IModel<String> labelModel, IModel<N> model) {
            return new OatNumberField<>(id, labelModel, model);
        }

        public static <N extends Number & Comparable<N>> OatNumberField<N> numberField(String id, IModel<String> labelModel, IModel<N> model, IModel<String> hintModel) {
            return new OatNumberField<>(id, labelModel, model, hintModel);
        }

        public static <T> OatTextArea<T> textArea(String id) {
            return new OatTextArea<>(id);
        }

        public static <T> OatTextArea<T> textArea(String id, IModel<T> model) {
            return new OatTextArea<>(id, model);
        }

        public static <T> OatTextArea<T> textArea(String id, String label, IModel<T> model) {
            return new OatTextArea<>(id, label, model);
        }

        public static <T> OatTextArea<T> textArea(String id, IModel<String> labelModel, IModel<T> model) {
            return new OatTextArea<>(id, labelModel, model);
        }

        public static <T> OatTextArea<T> textArea(String id, IModel<String> labelModel, IModel<T> model, IModel<String> hintModel) {
            return new OatTextArea<>(id, labelModel, model, hintModel);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<? extends List<? extends T>> choices) {
            return new OatDropdownChoice<>(id, choices);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatDropdownChoice<>(id, choices, renderer);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatDropdownChoice<>(id, model, choices);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatDropdownChoice<>(id, model, choices, renderer);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, String label, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatDropdownChoice<>(id, label, model, choices);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<String> labelModel, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatDropdownChoice<>(id, labelModel, model, choices);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<String> labelModel, IModel<T> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatDropdownChoice<>(id, labelModel, model, choices, renderer);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<String> labelModel, IModel<T> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer, IModel<String> hintModel) {
            return new OatDropdownChoice<>(id, labelModel, model, choices, renderer, hintModel);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, IModel<? extends List<? extends T>> choices) {
            return new OatRadioChoice<>(id, choices);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatRadioChoice<>(id, choices, renderer);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatRadioChoice<>(id, model, choices);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatRadioChoice<>(id, model, choices, renderer);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, String label, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatRadioChoice<>(id, label, model, choices);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, IModel<String> labelModel, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatRadioChoice<>(id, labelModel, model, choices);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, IModel<String> labelModel, IModel<T> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatRadioChoice<>(id, labelModel, model, choices, renderer);
        }

        public static <T> OatRadioChoice<T> radioChoice(String id, IModel<String> labelModel, IModel<T> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer, IModel<String> hintModel) {
            return new OatRadioChoice<>(id, labelModel, model, choices, renderer, hintModel);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, IModel<? extends List<? extends T>> choices) {
            return new OatCheckBoxMultipleChoice<>(id, choices);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatCheckBoxMultipleChoice<>(id, choices, renderer);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
            return new OatCheckBoxMultipleChoice<>(id, model, choices);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatCheckBoxMultipleChoice<>(id, model, choices, renderer);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, String label, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
            return new OatCheckBoxMultipleChoice<>(id, label, model, choices);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, IModel<String> labelModel, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
            return new OatCheckBoxMultipleChoice<>(id, labelModel, model, choices);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, IModel<String> labelModel, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatCheckBoxMultipleChoice<>(id, labelModel, model, choices, renderer);
        }

        public static <T> OatCheckBoxMultipleChoice<T> checkBoxMultipleChoice(String id, IModel<String> labelModel, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer, IModel<String> hintModel) {
            return new OatCheckBoxMultipleChoice<>(id, labelModel, model, choices, renderer, hintModel);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, IModel<? extends List<? extends T>> choices) {
            return new OatListMultipleChoice<>(id, choices);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatListMultipleChoice<>(id, choices, renderer);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
            return new OatListMultipleChoice<>(id, model, choices);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatListMultipleChoice<>(id, model, choices, renderer);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, String label, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
            return new OatListMultipleChoice<>(id, label, model, choices);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, IModel<String> labelModel, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
            return new OatListMultipleChoice<>(id, labelModel, model, choices);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, IModel<String> labelModel, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer) {
            return new OatListMultipleChoice<>(id, labelModel, model, choices, renderer);
        }

        public static <T> OatListMultipleChoice<T> listMultipleChoice(String id, IModel<String> labelModel, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, org.apache.wicket.markup.html.form.IChoiceRenderer<? super T> renderer, IModel<String> hintModel) {
            return new OatListMultipleChoice<>(id, labelModel, model, choices, renderer, hintModel);
        }

        public static OatCheckBox checkBox(String id) {
            return new OatCheckBox(id);
        }

        public static OatCheckBox checkBox(String id, IModel<Boolean> model) {
            return new OatCheckBox(id, model);
        }

        public static OatCheckBox checkBox(String id, String label, IModel<Boolean> model) {
            return new OatCheckBox(id, label, model);
        }

        public static OatCheckBox checkBox(String id, IModel<String> labelModel, IModel<Boolean> model) {
            return new OatCheckBox(id, labelModel, model);
        }

        public static OatCheckBox checkBox(String id, IModel<String> labelModel, IModel<Boolean> model, IModel<String> hintModel) {
            return new OatCheckBox(id, labelModel, model, hintModel);
        }

        public static OatSwitch oatSwitch(String id) {
            return new OatSwitch(id);
        }

        public static OatSwitch oatSwitch(String id, IModel<Boolean> model) {
            return new OatSwitch(id, model);
        }

        public static OatSwitch oatSwitch(String id, String label, IModel<Boolean> model) {
            return new OatSwitch(id, label, model);
        }

        public static OatSwitch oatSwitch(String id, IModel<String> labelModel, IModel<Boolean> model) {
            return new OatSwitch(id, labelModel, model);
        }

        public static OatSwitch oatSwitch(String id, IModel<String> labelModel, IModel<Boolean> model, IModel<String> hintModel) {
            return new OatSwitch(id, labelModel, model, hintModel);
        }

        public static OatDateField dateField(String id) {
            return new OatDateField(id);
        }

        public static OatDateField dateField(String id, IModel<java.time.LocalDate> model) {
            return new OatDateField(id, model);
        }

        public static OatDateField dateField(String id, String label, IModel<java.time.LocalDate> model) {
            return new OatDateField(id, label, model);
        }

        public static OatDateField dateField(String id, IModel<String> labelModel, IModel<java.time.LocalDate> model) {
            return new OatDateField(id, labelModel, model);
        }

        public static OatDateField dateField(String id, IModel<String> labelModel, IModel<java.time.LocalDate> model, IModel<String> hintModel) {
            return new OatDateField(id, labelModel, model, hintModel);
        }

        public static OatDateTimeLocalField dateTimeLocalField(String id) {
            return new OatDateTimeLocalField(id);
        }

        public static OatDateTimeLocalField dateTimeLocalField(String id, IModel<java.time.LocalDateTime> model) {
            return new OatDateTimeLocalField(id, model);
        }

        public static OatDateTimeLocalField dateTimeLocalField(String id, String label, IModel<java.time.LocalDateTime> model) {
            return new OatDateTimeLocalField(id, label, model);
        }

        public static OatDateTimeLocalField dateTimeLocalField(String id, IModel<String> labelModel, IModel<java.time.LocalDateTime> model) {
            return new OatDateTimeLocalField(id, labelModel, model);
        }

        public static OatDateTimeLocalField dateTimeLocalField(String id, IModel<String> labelModel, IModel<java.time.LocalDateTime> model, IModel<String> hintModel) {
            return new OatDateTimeLocalField(id, labelModel, model, hintModel);
        }

        public static OatTimeField timeField(String id) {
            return new OatTimeField(id);
        }

        public static OatTimeField timeField(String id, IModel<java.time.LocalTime> model) {
            return new OatTimeField(id, model);
        }

        public static OatTimeField timeField(String id, String label, IModel<java.time.LocalTime> model) {
            return new OatTimeField(id, label, model);
        }

        public static OatTimeField timeField(String id, IModel<String> labelModel, IModel<java.time.LocalTime> model) {
            return new OatTimeField(id, labelModel, model);
        }

        public static OatTimeField timeField(String id, IModel<String> labelModel, IModel<java.time.LocalTime> model, IModel<String> hintModel) {
            return new OatTimeField(id, labelModel, model, hintModel);
        }

        public static OatColorField colorField(String id) {
            return new OatColorField(id);
        }

        public static OatColorField colorField(String id, IModel<String> model) {
            return new OatColorField(id, model);
        }

        public static OatColorField colorField(String id, String label, IModel<String> model) {
            return new OatColorField(id, label, model);
        }

        public static OatColorField colorField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatColorField(id, labelModel, model);
        }

        public static OatColorField colorField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatColorField(id, labelModel, model, hintModel);
        }

        public static OatUrlField urlField(String id) {
            return new OatUrlField(id);
        }

        public static OatUrlField urlField(String id, IModel<String> model) {
            return new OatUrlField(id, model);
        }

        public static OatUrlField urlField(String id, String label, IModel<String> model) {
            return new OatUrlField(id, label, model);
        }

        public static OatUrlField urlField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatUrlField(id, labelModel, model);
        }

        public static OatUrlField urlField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatUrlField(id, labelModel, model, hintModel);
        }

        public static OatSearchField searchField(String id) {
            return new OatSearchField(id);
        }

        public static OatSearchField searchField(String id, IModel<String> model) {
            return new OatSearchField(id, model);
        }

        public static OatSearchField searchField(String id, String label, IModel<String> model) {
            return new OatSearchField(id, label, model);
        }

        public static OatSearchField searchField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatSearchField(id, labelModel, model);
        }

        public static OatSearchField searchField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatSearchField(id, labelModel, model, hintModel);
        }

        public static OatTelField telField(String id) {
            return new OatTelField(id);
        }

        public static OatTelField telField(String id, IModel<String> model) {
            return new OatTelField(id, model);
        }

        public static OatTelField telField(String id, String label, IModel<String> model) {
            return new OatTelField(id, label, model);
        }

        public static OatTelField telField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatTelField(id, labelModel, model);
        }

        public static OatTelField telField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatTelField(id, labelModel, model, hintModel);
        }

        public static OatMonthField monthField(String id) {
            return new OatMonthField(id);
        }

        public static OatMonthField monthField(String id, IModel<String> model) {
            return new OatMonthField(id, model);
        }

        public static OatMonthField monthField(String id, String label, IModel<String> model) {
            return new OatMonthField(id, label, model);
        }

        public static OatMonthField monthField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatMonthField(id, labelModel, model);
        }

        public static OatMonthField monthField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatMonthField(id, labelModel, model, hintModel);
        }

        public static OatWeekField weekField(String id) {
            return new OatWeekField(id);
        }

        public static OatWeekField weekField(String id, IModel<String> model) {
            return new OatWeekField(id, model);
        }

        public static OatWeekField weekField(String id, String label, IModel<String> model) {
            return new OatWeekField(id, label, model);
        }

        public static OatWeekField weekField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatWeekField(id, labelModel, model);
        }

        public static OatWeekField weekField(String id, IModel<String> labelModel, IModel<String> model, IModel<String> hintModel) {
            return new OatWeekField(id, labelModel, model, hintModel);
        }

        public static <N extends Number & Comparable<N>> OatRangeField<N> rangeField(String id) {
            return new OatRangeField<>(id);
        }

        public static <N extends Number & Comparable<N>> OatRangeField<N> rangeField(String id, IModel<N> model) {
            return new OatRangeField<>(id, model);
        }

        public static <N extends Number & Comparable<N>> OatRangeField<N> rangeField(String id, String label, IModel<N> model) {
            return new OatRangeField<>(id, label, model);
        }

        public static <N extends Number & Comparable<N>> OatRangeField<N> rangeField(String id, IModel<String> labelModel, IModel<N> model) {
            return new OatRangeField<>(id, labelModel, model);
        }

        public static <N extends Number & Comparable<N>> OatRangeField<N> rangeField(String id, IModel<String> labelModel, IModel<N> model, IModel<String> hintModel) {
            return new OatRangeField<>(id, labelModel, model, hintModel);
        }

        public static OatFileUpload fileUpload(String id) {
            return new OatFileUpload(id);
        }

        public static OatFileUpload fileUpload(String id, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileUpload(id, model);
        }

        public static OatFileUpload fileUpload(String id, String label, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileUpload(id, label, model);
        }

        public static OatFileUpload fileUpload(String id, IModel<String> labelModel, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileUpload(id, labelModel, model);
        }

        public static OatFileUpload fileUpload(String id, IModel<String> labelModel, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model, IModel<String> hintModel) {
            return new OatFileUpload(id, labelModel, model, hintModel);
        }

        public static OatFileDropzone fileDropzone(String id) {
            return new OatFileDropzone(id);
        }

        public static OatFileDropzone fileDropzone(String id, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileDropzone(id, model);
        }

        public static OatFileDropzone fileDropzone(String id, String label, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileDropzone(id, label, model);
        }

        public static OatFileDropzone fileDropzone(String id, IModel<String> labelModel, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileDropzone(id, labelModel, model);
        }

        public static OatFileDropzone fileDropzone(String id, IModel<String> labelModel, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model, IModel<String> hintModel) {
            return new OatFileDropzone(id, labelModel, model, hintModel);
        }

        /** A range of days (from/to) bound to a {@link DateRange}. */
        public static OatDateRangeField dateRangeField(String id) {
            return new OatDateRangeField(id);
        }

        public static OatDateRangeField dateRangeField(String id, IModel<DateRange> model) {
            return new OatDateRangeField(id, model);
        }

        public static OatDateRangeField dateRangeField(String id, String label, IModel<DateRange> model) {
            return new OatDateRangeField(id, label, model);
        }

        public static OatDateRangeField dateRangeField(String id, IModel<String> labelModel, IModel<DateRange> model) {
            return new OatDateRangeField(id, labelModel, model);
        }

        public static OatDateRangeField dateRangeField(String id, IModel<String> labelModel, IModel<DateRange> model, IModel<String> hintModel) {
            return new OatDateRangeField(id, labelModel, model, hintModel);
        }

        /** A field looking up its value on the server as the user types; set its {@code setChoices(...)}. */
        public static <T> OatAutoCompleteField<T> autoCompleteField(String id) {
            return new OatAutoCompleteField<>(id);
        }

        public static <T> OatAutoCompleteField<T> autoCompleteField(String id, IModel<T> model) {
            return new OatAutoCompleteField<>(id, model);
        }

        public static <T> OatAutoCompleteField<T> autoCompleteField(String id, String label, IModel<T> model) {
            return new OatAutoCompleteField<>(id, label, model);
        }

        public static <T> OatAutoCompleteField<T> autoCompleteField(String id, IModel<String> labelModel, IModel<T> model) {
            return new OatAutoCompleteField<>(id, labelModel, model);
        }

        public static <T> OatAutoCompleteField<T> autoCompleteField(String id, IModel<String> labelModel, IModel<T> model, IModel<String> hintModel) {
            return new OatAutoCompleteField<>(id, labelModel, model, hintModel);
        }

        /** An Oat field around an input of your own made of several inputs, e.g. a {@code FormComponentPanel}. */
        public static <T> OatCustomField<T> customField(String id, SerializableBiFunction<String, IModel<T>, ? extends org.apache.wicket.markup.html.form.FormComponent<T>> input) {
            return new OatCustomField<>(id, input);
        }

        public static <T> OatCustomField<T> customField(String id, String label, IModel<T> model,
                                                       SerializableBiFunction<String, IModel<T>, ? extends org.apache.wicket.markup.html.form.FormComponent<T>> input) {
            return new OatCustomField<>(id, label, model, input);
        }

        public static <T> OatCustomField<T> customField(String id, IModel<String> labelModel, IModel<T> model, IModel<String> hintModel,
                                                       SerializableBiFunction<String, IModel<T>, ? extends org.apache.wicket.markup.html.form.FormComponent<T>> input) {
            return new OatCustomField<>(id, labelModel, model, hintModel, input);
        }

        public static OatTagInput tagInput(String id) {
            return new OatTagInput(id);
        }

        public static OatTagInput tagInput(String id, IModel<List<String>> model) {
            return new OatTagInput(id, model);
        }

        public static OatTagInput tagInput(String id, String label, IModel<List<String>> model) {
            return new OatTagInput(id, label, model);
        }

        public static OatTagInput tagInput(String id, IModel<String> labelModel, IModel<List<String>> model) {
            return new OatTagInput(id, labelModel, model);
        }

        public static OatTagInput tagInput(String id, IModel<String> labelModel, IModel<List<String>> model, IModel<String> hintModel) {
            return new OatTagInput(id, labelModel, model, hintModel);
        }

        /** An amount of money; set its currency with {@link OatMoneyField#setCurrency}. */
        public static OatMoneyField moneyField(String id) {
            return new OatMoneyField(id);
        }

        public static OatMoneyField moneyField(String id, IModel<java.math.BigDecimal> model) {
            return new OatMoneyField(id, model);
        }

        public static OatMoneyField moneyField(String id, String label, IModel<java.math.BigDecimal> model) {
            return new OatMoneyField(id, label, model);
        }

        public static OatMoneyField moneyField(String id, IModel<String> labelModel, IModel<java.math.BigDecimal> model) {
            return new OatMoneyField(id, labelModel, model);
        }

        public static OatMoneyField moneyField(String id, IModel<String> labelModel, IModel<java.math.BigDecimal> model, IModel<String> hintModel) {
            return new OatMoneyField(id, labelModel, model, hintModel);
        }

        public static OatPercentField percentField(String id) {
            return new OatPercentField(id);
        }

        public static OatPercentField percentField(String id, IModel<java.math.BigDecimal> model) {
            return new OatPercentField(id, model);
        }

        public static OatPercentField percentField(String id, String label, IModel<java.math.BigDecimal> model) {
            return new OatPercentField(id, label, model);
        }

        public static OatPercentField percentField(String id, IModel<String> labelModel, IModel<java.math.BigDecimal> model) {
            return new OatPercentField(id, labelModel, model);
        }

        public static OatPercentField percentField(String id, IModel<String> labelModel, IModel<java.math.BigDecimal> model, IModel<String> hintModel) {
            return new OatPercentField(id, labelModel, model, hintModel);
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

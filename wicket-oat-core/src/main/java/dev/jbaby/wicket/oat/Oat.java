package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.behaviors.*;
import dev.jbaby.wicket.oat.components.*;
import dev.jbaby.wicket.oat.components.form.*;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

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

        public static AlertBehavior alert(AlertBehavior.Variant variant) {
            return new AlertBehavior(variant);
        }

        public static BadgeBehavior badge() {
            return new BadgeBehavior();
        }

        public static BadgeBehavior badge(BadgeBehavior.Variant variant) {
            return new BadgeBehavior(variant);
        }

        public static ButtonBehavior button() {
            return new ButtonBehavior();
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

        public static HintBehavior hint() {
            return new HintBehavior();
        }

        public static SwitchBehavior switchBehavior() {
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

        public static TooltipBehavior tooltip(String text) {
            return new TooltipBehavior(text);
        }

        public static TooltipBehavior tooltip(IModel<String> textModel) {
            return new TooltipBehavior(textModel);
        }

        public static ClientSideClickBehavior clientSideClick(String script) {
            return new ClientSideClickBehavior(script);
        }

        public static OatThemeBehavior theme() {
            return new OatThemeBehavior();
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

        public static OatAlert alert(String id, AlertBehavior.Variant variant) {
            return new OatAlert(id, (String) null, variant);
        }

        public static OatAlert alert(String id, String message, AlertBehavior.Variant variant) {
            return new OatAlert(id, message, variant);
        }

        public static OatAlert alert(String id, IModel<?> model) {
            return new OatAlert(id, model);
        }

        public static OatAlert alert(String id, IModel<?> model, AlertBehavior.Variant variant) {
            return new OatAlert(id, model, variant);
        }

        public static OatBadge badge(String id, String label) {
            return new OatBadge(id, label);
        }

        public static OatBadge badge(String id, String label, BadgeBehavior.Variant variant) {
            return new OatBadge(id, label, variant);
        }

        public static OatBadge badge(String id, IModel<?> model) {
            return new OatBadge(id, model);
        }

        public static OatBadge badge(String id, IModel<?> model, BadgeBehavior.Variant variant) {
            return new OatBadge(id, model, variant);
        }

        public static OatCard card(String id) {
            return new OatCard(id);
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

        public static OatProgress progress(String id, Number value) {
            return new OatProgress(id, value);
        }

        public static OatProgress progress(String id, IModel<? extends Number> valueModel) {
            return new OatProgress(id, valueModel);
        }

        public static OatMeter meter(String id, Number value) {
            return new OatMeter(id, value);
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

        public static <T, S> OatDataTable<T, S> dataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider, long rowsPerPage) {
            return new OatDataTable<>(id, columns, dataProvider, rowsPerPage);
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

        public static <T> OatAccordion<T> accordion(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
            return new OatAccordion<T>(id, model) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    populateItem.accept(item, item.getModelObject());
                }
            };
        }

        // --- Form Components ---

        public static <T> OatTextField<T> textField(String id, String label, IModel<T> model) {
            return new OatTextField<>(id, label, model);
        }

        public static <T> OatTextField<T> textField(String id, IModel<String> labelModel, IModel<T> model) {
            return new OatTextField<>(id, labelModel, model);
        }

        public static <T> OatTextField<T> textField(String id, IModel<String> labelModel, IModel<T> model, IModel<String> hintModel) {
            return new OatTextField<>(id, labelModel, model, hintModel);
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

        public static OatEmailField emailField(String id, String label, IModel<String> model) {
            return new OatEmailField(id, label, model);
        }

        public static OatEmailField emailField(String id, IModel<String> labelModel, IModel<String> model) {
            return new OatEmailField(id, labelModel, model);
        }

        public static <N extends Number & Comparable<N>> OatNumberField<N> numberField(String id, String label, IModel<N> model) {
            return new OatNumberField<>(id, label, model);
        }

        public static <N extends Number & Comparable<N>> OatNumberField<N> numberField(String id, IModel<String> labelModel, IModel<N> model) {
            return new OatNumberField<>(id, labelModel, model);
        }

        public static <T> OatTextArea<T> textArea(String id, String label, IModel<T> model) {
            return new OatTextArea<>(id, label, model);
        }

        public static <T> OatTextArea<T> textArea(String id, IModel<String> labelModel, IModel<T> model) {
            return new OatTextArea<>(id, labelModel, model);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, String label, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatDropdownChoice<>(id, label, model, choices);
        }

        public static <T> OatDropdownChoice<T> dropdownChoice(String id, IModel<String> labelModel, IModel<T> model, IModel<? extends List<? extends T>> choices) {
            return new OatDropdownChoice<>(id, labelModel, model, choices);
        }

        public static OatCheckBox checkBox(String id, String label, IModel<Boolean> model) {
            return new OatCheckBox(id, label, model);
        }

        public static OatCheckBox checkBox(String id, IModel<String> labelModel, IModel<Boolean> model) {
            return new OatCheckBox(id, labelModel, model);
        }

        public static OatSwitch oatSwitch(String id, String label, IModel<Boolean> model) {
            return new OatSwitch(id, label, model);
        }

        public static OatSwitch oatSwitch(String id, IModel<String> labelModel, IModel<Boolean> model) {
            return new OatSwitch(id, labelModel, model);
        }

        public static OatDateField dateField(String id, String label, IModel<java.time.LocalDate> model) {
            return new OatDateField(id, label, model);
        }

        public static OatDateTimeLocalField dateTimeLocalField(String id, String label, IModel<java.time.LocalDateTime> model) {
            return new OatDateTimeLocalField(id, label, model);
        }

        public static OatTimeField timeField(String id, String label, IModel<java.time.LocalTime> model) {
            return new OatTimeField(id, label, model);
        }

        public static OatColorField colorField(String id, String label, IModel<String> model) {
            return new OatColorField(id, label, model);
        }

        public static OatUrlField urlField(String id, String label, IModel<String> model) {
            return new OatUrlField(id, label, model);
        }

        public static OatSearchField searchField(String id, String label, IModel<String> model) {
            return new OatSearchField(id, label, model);
        }

        public static OatTelField telField(String id, String label, IModel<String> model) {
            return new OatTelField(id, label, model);
        }

        public static OatMonthField monthField(String id, String label, IModel<String> model) {
            return new OatMonthField(id, label, model);
        }

        public static OatWeekField weekField(String id, String label, IModel<String> model) {
            return new OatWeekField(id, label, model);
        }

        public static <N extends Number & Comparable<N>> OatRangeField<N> rangeField(String id, String label, IModel<N> model) {
            return new OatRangeField<>(id, label, model);
        }

        public static OatFileUpload fileUpload(String id, String label, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileUpload(id, label, model);
        }

        public static OatFileDropzone fileDropzone(String id, String label, IModel<List<org.apache.wicket.markup.html.form.upload.FileUpload>> model) {
            return new OatFileDropzone(id, label, model);
        }

        public static OatTagInput tagInput(String id, String label, IModel<String> model) {
            return new OatTagInput(id, label, model);
        }
    }

    // --- Actions ---

    public static void toast(AjaxRequestTarget target, String message) {
        OatToastBehavior.toast(target, message);
    }

    public static void toast(AjaxRequestTarget target, String message, OatToastBehavior.Variant variant) {
        OatToastBehavior.toast(target, message, variant);
    }

    public static void toast(AjaxRequestTarget target, String message, OatToastBehavior.Variant variant, String title) {
        OatToastBehavior.toast(target, message, variant, title);
    }
}

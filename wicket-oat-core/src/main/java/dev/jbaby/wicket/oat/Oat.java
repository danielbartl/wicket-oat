package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.behaviors.*;
import dev.jbaby.wicket.oat.components.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * Entry point for creating Oat components and behaviors.
 */
public final class Oat {

    private Oat() {}

    // --- Behaviors ---

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

    public static void toast(AjaxRequestTarget target, String message) {
        OatToastBehavior.toast(target, message);
    }

    public static void toast(AjaxRequestTarget target, String message, OatToastBehavior.Variant variant) {
        OatToastBehavior.toast(target, message, variant);
    }

    public static void toast(AjaxRequestTarget target, String message, OatToastBehavior.Variant variant, String title) {
        OatToastBehavior.toast(target, message, variant, title);
    }

    // --- Components ---

    public static OatAlert alert(String id) {
        return new OatAlert(id);
    }

    public static OatAlert alert(String id, AlertBehavior.Variant variant) {
        return new OatAlert(id, variant);
    }

    public static OatBadge badge(String id, String label) {
        return new OatBadge(id, label);
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

    public static OatButtonGroup buttonGroup(String id) {
        return new OatButtonGroup(id);
    }

    public static OatButton button(String id, String label, java.util.function.Consumer<AjaxRequestTarget> onClick) {
        return new OatButton(id, label) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onClick.accept(target);
            }
        };
    }

    public static <T> OatAccordion<T> accordion(String id, IModel<List<T>> model, java.util.function.BiConsumer<ListItem<T>, T> populateItem) {
        return new OatAccordion<T>(id, model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }
}

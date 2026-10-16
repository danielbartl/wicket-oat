package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import java.util.List;
import java.util.Objects;

/**
 * Where a process stands, as numbered steps joined by a line - an order going from
 * "Ordered" to "Delivered", an application under review, the parts of a sign-up:
 * <pre>{@code
 * add(new OatSteps("progress", List.of("Ordered", "Paid", "Shipped", "Delivered"), () -> order.stage().ordinal()));
 * }</pre>
 * on any tag, e.g. {@code <div wicket:id="progress"></div>}. The steps before the current
 * one are done, the current one is highlighted and marked for screen readers
 * ({@code aria-current="step"}), and the ones after it are still to come. A current step
 * past the last marks all of them done, and {@code null} none.
 * <p>
 * With {@link #setOnSelect} the steps already done are links back to them, as in
 * {@link OatWizard}. The list is named "Steps" for screen readers ({@code OatSteps.label},
 * translated); {@link #setLabel} names it otherwise. The steps have a markup id, so
 * {@code target.add(steps)} shows a new current step.
 */
public class OatSteps extends Panel {

    private final IModel<? extends List<String>> titles;
    private final IModel<Integer> current;
    private IModel<String> label;
    private SerializableBiConsumer<AjaxRequestTarget, Integer> onSelect;

    public OatSteps(String id, List<String> titles, IModel<Integer> current) {
        this(id, Model.ofList(titles), current);
    }

    /**
     * @param titles the steps' titles, in order, read once per request
     * @param current the index of the current step, from 0
     */
    public OatSteps(String id, IModel<? extends List<String>> titles, IModel<Integer> current) {
        super(id);
        this.titles = Objects.requireNonNull(titles);
        this.current = Objects.requireNonNull(current);

        setOutputMarkupId(true);

        WebMarkupContainer list = new WebMarkupContainer("list");
        list.add(AttributeModifier.replace("aria-label",
                (IModel<String>) () -> label != null ? label.getObject() : getString("OatSteps.label")));
        add(list);
        // The titles read once per request, also for OatWizard's, which are models of their own
        IModel<List<String>> loaded = LoadableDetachableModel.of(() -> {
            List<String> all = titles.getObject();
            return all != null ? all : List.of();
        });
        list.add(new ListView<String>("items", loaded) {
            @Override
            protected void populateItem(ListItem<String> item) {
                int index = item.getIndex();
                int now = currentStep();
                item.add(AttributeModifier.replace("data-state", index < now ? "done" : index == now ? "current" : "upcoming"));
                item.add(AttributeModifier.replace("aria-current", index == now ? "step" : null));
                AjaxLink<Void> link = new AjaxLink<>("link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        onSelect.accept(target, index);
                    }
                };
                link.setEnabled(onSelect != null && index < now);
                link.add(new Label("number", String.valueOf(index + 1)));
                link.add(new Label("title", item.getModel()));
                item.add(link);
            }
        });
    }

    /** The current step's index, or -1 if there is none yet. */
    private int currentStep() {
        Integer index = current.getObject();
        return index == null ? -1 : index;
    }

    /** The list's name for screen readers; {@code null} for "Steps". */
    public OatSteps setLabel(IModel<String> label) {
        this.label = label;
        return this;
    }

    /** Makes the steps already done links that run this with the step's index, e.g. to show it again. */
    public OatSteps setOnSelect(SerializableBiConsumer<AjaxRequestTarget, Integer> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    @Override
    protected void onDetach() {
        titles.detach();
        current.detach();
        if (label != null) {
            label.detach();
        }
        super.onDetach();
    }
}

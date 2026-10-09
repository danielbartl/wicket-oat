package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.Objects;

/**
 * A list of items next to the details of the one selected - customers and a customer,
 * tickets and a ticket, messages and a message. On narrow screens it shows one at a
 * time: choosing an item shows its details, with a Back button to the list.
 * <pre>{@code
 * OatMasterDetail<Customer> customers = new OatMasterDetail<>("customers", new Model<>());
 * customers.setMaster(id -> new CustomerList(id, customers));      // its items call customers.select(target, c)
 * customers.setDetail((id, customer) -> new CustomerPanel(id, customer));
 * add(customers);
 * }</pre>
 * The master is any component - a list, cards, a table - that calls
 * {@link #select(AjaxRequestTarget, Object) select(target, item)} when an item is
 * chosen, and can use {@link #isSelected} to mark it. The detail is created for the
 * selected item, with an empty state ({@code OatMasterDetail.empty}) while there is
 * none. The Back button's text is the {@code OatMasterDetail.back} resource.
 *
 * @param <T> the item type
 */
public class OatMasterDetail<T> extends Panel {

    private static final String DETAIL_ID = "detail";

    private final IModel<T> selection;
    private final WebMarkupContainer detailArea;
    private SerializableBiFunction<String, IModel<T>, ? extends Component> detail;
    private boolean showingDetail;

    /** @param selection the selected item; {@code null} for none */
    public OatMasterDetail(String id, IModel<T> selection) {
        super(id);
        this.selection = Objects.requireNonNull(selection);
        setOutputMarkupId(true);

        add(new WebMarkupContainer("master").setVisible(false));

        detailArea = new WebMarkupContainer("detailArea");
        detailArea.setOutputMarkupId(true);
        add(detailArea);
        detailArea.add(new AjaxLink<Void>("back") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                showList(target);
            }
        }.setBody(new StringResourceModel("OatMasterDetail.back", this).setDefaultValue("Back"))
                .add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE).setSize(ButtonBehavior.Size.SMALL)));
        detailArea.add(newEmptyState(DETAIL_ID));
    }

    /** The list of items; the factory receives the id to use. */
    public OatMasterDetail<T> setMaster(SerializableFunction<String, ? extends Component> master) {
        Component component = master.apply("master");
        if (component == null || !"master".equals(component.getId())) {
            throw new IllegalArgumentException("The master must use the id passed to the factory (\"master\"), but was "
                    + (component == null ? "null" : "\"" + component.getId() + "\""));
        }
        addOrReplace(component);
        return this;
    }

    /**
     * The details of the selected item: the factory receives the id to use and the
     * selection model, and is called whenever the selection changes.
     */
    public OatMasterDetail<T> setDetail(SerializableBiFunction<String, IModel<T>, ? extends Component> detail) {
        this.detail = detail;
        updateDetail();
        return this;
    }

    /**
     * Selects an item and shows its details (on narrow screens, instead of the list).
     * Re-renders the detail; re-render the master yourself if it marks the selection.
     */
    public OatMasterDetail<T> select(AjaxRequestTarget target, T item) {
        selection.setObject(item);
        showingDetail = item != null;
        updateDetail();
        target.add(detailArea);
        setShowing(target);
        return this;
    }

    /** On narrow screens, goes back from the details to the list; the selection stays. */
    public OatMasterDetail<T> showList(AjaxRequestTarget target) {
        showingDetail = false;
        setShowing(target);
        return this;
    }

    /** Whether this item is the selected one, e.g. to mark it in the list. */
    public boolean isSelected(T item) {
        return item != null && item.equals(selection.getObject());
    }

    /** The selected item. */
    public IModel<T> getSelection() {
        return selection;
    }

    /** The empty state shown while nothing is selected; override to change it. */
    protected Component newEmptyState(String id) {
        return new OatEmptyState(id, new StringResourceModel("OatMasterDetail.empty", this)
                .setDefaultValue("Select an item to see its details."), new Model<>());
    }

    private void updateDetail() {
        Component component = selection.getObject() == null || detail == null
                ? newEmptyState(DETAIL_ID) : detail.apply(DETAIL_ID, selection);
        if (component == null || !DETAIL_ID.equals(component.getId())) {
            throw new IllegalArgumentException("The detail must use the id passed to the factory (\"" + DETAIL_ID
                    + "\"), but was " + (component == null ? "null" : "\"" + component.getId() + "\""));
        }
        detailArea.addOrReplace(component);
    }

    /** Switches the narrow-screen view without re-rendering the list (keeping its scroll position). */
    private void setShowing(AjaxRequestTarget target) {
        target.appendJavaScript("(function(e){if(e){e.dataset.showing='" + (showingDetail ? "detail" : "master")
                + "';e.scrollIntoView({block:'nearest'});}})(document.getElementById('" + getMarkupId() + "'))");
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", "oat-master-detail", " ");
        tag.put("data-showing", showingDetail ? "detail" : "master");
    }

    @Override
    protected void onDetach() {
        selection.detach();
        super.onDetach();
    }
}

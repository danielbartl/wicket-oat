package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A data table column showing a value as a link - to a page about the row, or running
 * an Ajax action:
 * <pre>{@code
 * OatLinkColumn.toPage(Model.of("Number"), "number", Invoice::number,
 *         InvoicePage.class, invoice -> new PageParameters().add("id", invoice.id()))
 *
 * OatLinkColumn.onClick(Model.of("Customer"), "customer", Invoice::customer,
 *         (target, invoice) -> customerDialog.open(target, invoice.customer()))
 * }</pre>
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public abstract class OatLinkColumn<T, S> extends LambdaColumn<T, S> {

    /**
     * @param sortProperty the property to sort by, or {@code null} if unsortable
     * @param label the link's text for a row
     */
    protected OatLinkColumn(IModel<String> displayModel, S sortProperty, SerializableFunction<T, ?> label) {
        super(displayModel, sortProperty, label::apply);
    }

    /** A column linking each row to a bookmarkable page, with parameters for the row. */
    public static <T, S> OatLinkColumn<T, S> toPage(IModel<String> displayModel, S sortProperty,
                                                    SerializableFunction<T, ?> label, Class<? extends Page> page,
                                                    SerializableFunction<T, PageParameters> parameters) {
        return new OatLinkColumn<>(displayModel, sortProperty, label) {
            @Override
            protected AbstractLink newLink(String id, IModel<T> rowModel) {
                return new BookmarkablePageLink<>(id, page, parameters.apply(rowModel.getObject()));
            }
        };
    }

    /** A column whose links run {@code onClick} over Ajax with the row. */
    public static <T, S> OatLinkColumn<T, S> onClick(IModel<String> displayModel, S sortProperty,
                                                     SerializableFunction<T, ?> label,
                                                     SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
        return new OatLinkColumn<>(displayModel, sortProperty, label) {
            @Override
            protected AbstractLink newLink(String id, IModel<T> rowModel) {
                return new AjaxLink<Void>(id) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        onClick.accept(target, rowModel.getObject());
                    }
                };
            }
        };
    }

    /** Creates the link for a row; it is rendered on an {@code <a>} tag. */
    protected abstract AbstractLink newLink(String id, IModel<T> rowModel);

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new LinkCell(componentId, newLink(LinkCell.LINK_ID, rowModel), getDataModel(rowModel)));
    }

    /** A link with a label, on an {@code <a>} tag (a link on the cell's own tag would need inline JS). */
    static final class LinkCell extends Panel {

        static final String LINK_ID = "link";

        LinkCell(String id, AbstractLink link, IModel<?> label) {
            super(id);
            link.add(new Label("label", label));
            add(link);
        }
    }
}

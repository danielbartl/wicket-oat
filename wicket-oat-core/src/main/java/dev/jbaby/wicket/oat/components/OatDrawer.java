package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Locale;
import java.util.Objects;

/**
 * A side panel that slides in from an edge of the screen over the page, for what
 * supports it: a record's details, filters, a form to edit a row. It is an
 * {@link OatDialog} - opened with {@link #open}, closed with {@link #close}, its content
 * set with {@link #setContent}, Cancel and Confirm in its footer - shown as tall as the
 * screen at its end, or at its start or bottom ({@link #setSide}). An "×" in its corner,
 * the Escape key and a click on the page beside it close it.
 * <pre>{@code
 * OatDrawer filters = new OatDrawer("filters", "Filters", "Filter orders") {
 *     @Override
 *     protected void onConfirm(AjaxRequestTarget target) {
 *         target.add(table); // the fields in the content have updated the filter's models
 *     }
 * };
 * filters.setContent(new OrderFilterPanel(OatDialog.CONTENT_ID, filter));
 * filters.setConfirmLabel(Model.of("Apply"));
 * add(filters);
 * }</pre>
 * on {@code <div wicket:id="filters"></div>}. For a drawer that only shows something,
 * {@link #setFooterVisible setFooterVisible(false)} leaves out Cancel and Confirm. On a
 * phone a side drawer leaves a strip of the page in view; a bottom one is as wide as the
 * screen and as tall as its content, up to most of the screen. Start and end follow the
 * text direction.
 */
public class OatDrawer extends OatDialog {

    /** The edge a drawer slides in from. */
    public enum Side {
        /** The left in a left-to-right language. */
        START,
        /** The right in a left-to-right language; the default. */
        END,
        /** The bottom, as wide as the screen. */
        BOTTOM
    }

    private Side side = Side.END;

    /** A drawer without a trigger button; open it with {@link #open}. */
    public OatDrawer(String id, String header) {
        this(id, Model.of(header));
    }

    /** A drawer without a trigger button; open it with {@link #open}. */
    public OatDrawer(String id, IModel<String> header) {
        this(id, null, header);
    }

    /**
     * @param triggerLabel the label of a button that opens the drawer, or {@code null} for none
     */
    public OatDrawer(String id, String triggerLabel, String header) {
        this(id, triggerLabel != null ? Model.of(triggerLabel) : null, Model.of(header));
    }

    /**
     * @param triggerLabel the label of a button that opens the drawer, or {@code null} for none
     */
    public OatDrawer(String id, IModel<String> triggerLabel, IModel<String> header) {
        super(id, triggerLabel, header);
        getDialogElement().add(AttributeModifier.append("class", "oat-drawer"));
        getDialogElement().add(AttributeModifier.replace("data-side", (IModel<String>) () -> side.name().toLowerCase(Locale.ROOT)));
        setCloseButton(true);
    }

    /** The edge the drawer slides in from; {@link Side#END} by default. */
    public OatDrawer setSide(Side side) {
        this.side = Objects.requireNonNull(side);
        return this;
    }

    // Fluent setters typed to this class, so they chain with setSide()

    @Override
    public OatDrawer setContent(Component content) {
        super.setContent(content);
        return this;
    }

    @Override
    public OatDrawer setCloseButton(boolean shown) {
        super.setCloseButton(shown);
        return this;
    }

    @Override
    public OatDrawer setFooterVisible(boolean visible) {
        super.setFooterVisible(visible);
        return this;
    }

    @Override
    public OatDrawer setHeader(IModel<String> header) {
        super.setHeader(header);
        return this;
    }

    @Override
    public OatDrawer setCancelLabel(IModel<String> label) {
        super.setCancelLabel(label);
        return this;
    }

    @Override
    public OatDrawer setConfirmLabel(IModel<String> label) {
        super.setConfirmLabel(label);
        return this;
    }

    @Override
    public OatDrawer setConfirmVariant(OatVariant variant) {
        super.setConfirmVariant(variant);
        return this;
    }

    @Override
    public OatDrawer setBusyIndicator(boolean busyIndicator) {
        super.setBusyIndicator(busyIndicator);
        return this;
    }
}

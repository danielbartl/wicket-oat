package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * How far something has got, as a ring that fills clockwise with the percentage in
 * the middle - storage used, a profile filled in, a goal reached:
 * <pre>{@code
 * add(new OatRadialProgress("storage", () -> account.usedGigabytes(), () -> account.quotaGigabytes())
 *         .setVariant(() -> account.isNearlyFull() ? OatVariant.DANGER : OatVariant.DEFAULT));
 * }</pre>
 * on any tag, named in the markup for screen readers, e.g.
 * {@code <div wicket:id="storage" aria-label="Storage used"></div>}. It is a
 * {@code progressbar} for them, with its value and maximum, and reads out the text in
 * the middle; {@link #setText} replaces the percentage there, e.g. with "3/5". The ring
 * has the variant's color: the primary color by default, gray for {@code SECONDARY},
 * green, amber or red for {@code SUCCESS}, {@code WARNING} and {@code DANGER}. A
 * {@code null} value leaves the ring empty and the value unknown. It is 4rem wide;
 * {@code --oat-radial-progress-size} in your CSS changes that. {@link OatProgress} is
 * the bar.
 */
public class OatRadialProgress extends Panel {

    private final IModel<? extends Number> value;
    private final IModel<? extends Number> max;
    private IModel<OatVariant> variant = Model.of(OatVariant.DEFAULT);
    private IModel<String> text;

    /** @param value the percentage, from 0 to 100 */
    public OatRadialProgress(String id, Number value) {
        this(id, Model.of(value));
    }

    /** @param value the percentage, from 0 to 100, read on every render */
    public OatRadialProgress(String id, IModel<? extends Number> value) {
        this(id, value, Model.of(100));
    }

    /**
     * @param value how far it has got, from 0 to {@code max}, read on every render
     * @param max where it is complete, read on every render
     */
    public OatRadialProgress(String id, IModel<? extends Number> value, IModel<? extends Number> max) {
        super(id);
        this.value = Objects.requireNonNull(value);
        this.max = Objects.requireNonNull(max);

        // The ring's filled part: a dash as long as the share, of a circle 100 long
        WebMarkupContainer bar = new WebMarkupContainer("bar") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                Double percent = percent();
                // No dash for 0, which its round ends would still draw as a dot
                setVisible(percent != null && percent > 0);
            }
        };
        bar.add(AttributeModifier.replace("stroke-dasharray", (IModel<String>) () -> {
            Double percent = percent();
            return percent == null ? null : num(percent) + " 100";
        }));
        add(bar);
        add(new Label("text", this::currentText));
        setOutputMarkupId(true);
    }

    /** The share done, from 0 to 100, or {@code null} while the value is unknown. */
    private Double percent() {
        Number done = value.getObject();
        Number all = max.getObject();
        if (done == null || all == null || all.doubleValue() <= 0) {
            return null;
        }
        return Math.max(0, Math.min(100, done.doubleValue() * 100 / all.doubleValue()));
    }

    private String currentText() {
        if (text != null) {
            return text.getObject();
        }
        Double percent = percent();
        return percent == null ? null : NumberFormat.getPercentInstance(getLocale()).format(Math.floor(percent) / 100);
    }

    private static String num(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.2f", value);
    }

    /** The ring's color; the primary color by default. */
    public OatRadialProgress setVariant(OatVariant variant) {
        return setVariant(Model.of(Objects.requireNonNull(variant)));
    }

    /** The ring's color, read on every render, e.g. red once nearly full. */
    public OatRadialProgress setVariant(IModel<OatVariant> variant) {
        this.variant = Objects.requireNonNull(variant);
        return this;
    }

    /** The text in the middle instead of the percentage, e.g. "3/5"; {@code null} for the percentage. */
    public OatRadialProgress setText(IModel<String> text) {
        this.text = text;
        return this;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", "oat-radial-progress", " ");
        tag.put("role", "progressbar");
        tag.put("aria-valuemin", "0");
        Number all = max.getObject();
        Number done = value.getObject();
        if (all != null) {
            tag.put("aria-valuemax", all.toString());
        }
        if (done != null) {
            tag.put("aria-valuenow", done.toString());
        } else {
            tag.remove("aria-valuenow");
        }
        String shown = currentText();
        if (shown != null && !shown.isBlank()) {
            tag.put("aria-valuetext", shown);
        } else {
            tag.remove("aria-valuetext");
        }
        OatVariant color = variant.getObject();
        if (color != null && color.getValue() != null) {
            tag.put("data-variant", color.getValue());
        } else {
            tag.remove("data-variant");
        }
    }

    @Override
    protected void onDetach() {
        value.detach();
        max.detach();
        variant.detach();
        if (text != null) {
            text.detach();
        }
        super.onDetach();
    }
}

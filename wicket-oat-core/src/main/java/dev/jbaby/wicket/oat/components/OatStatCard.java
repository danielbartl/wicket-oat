package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import dev.jbaby.wicket.oat.components.chart.OatSparkline;

import java.text.NumberFormat;
import java.util.List;

/**
 * A dashboard tile for one key figure: a label, the value in large type, and
 * optionally how it changed - a badge such as {@code +12.5%}, green when that's good -
 * with a hint like "vs. last month":
 * <pre>{@code
 * add(new OatStatCard("revenue", Model.of("Revenue"), () -> reports.revenueThisMonth())
 *         .setChange(() -> reports.revenueChangePercent())
 *         .setHint(Model.of("vs. last month")));
 * }</pre>
 * It is a Wicket {@link Border}, so it goes on a tag such as {@code <article>} and
 * anything inside the tag - a progress bar, a link to the report - is shown below the
 * figure. Lay several out with {@code Oat.Behaviors.row()} and {@code col(3)}.
 * Numbers are shown in the user's locale with grouping ({@code 48,250}); pass a
 * {@code String} model for a value formatted your own way, e.g. an amount.
 */
public class OatStatCard extends Border {

    private IModel<? extends Number> change;
    private IModel<? extends List<? extends Number>> trend;
    private boolean higherIsBetter = true;
    private final Label hint;

    public OatStatCard(String id, String label, IModel<?> value) {
        this(id, Model.of(label), value);
    }

    public OatStatCard(String id, IModel<String> label, IModel<?> value) {
        super(id);
        addToBorder(new Label("label", label));
        addToBorder(new ValueLabel("value", value));
        addToBorder(new OatSparkline("trend", (IModel<List<? extends Number>>) () -> trend != null ? trend.getObject() : null) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(trend != null && trend.getObject() != null && trend.getObject().size() > 1);
            }
        });

        WebMarkupContainer footer = new WebMarkupContainer("footer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(change != null || hint.getDefaultModelObject() != null);
            }
        };
        addToBorder(footer);

        footer.add(new OatBadge("change", (IModel<String>) this::formatChange, (IModel<OatVariant>) this::changeVariant) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(change != null && change.getObject() != null);
            }
        });
        // An explicit (empty) model, so the label never inherits one from a parent
        // CompoundPropertyModel by its id
        hint = new Label("hint", new Model<String>()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDefaultModelObject() != null);
            }
        };
        footer.add(hint);
    }

    /**
     * How the value changed, in percent: {@code 12.5} shows as {@code +12.5%} (in the
     * user's locale) on a green badge, a negative change on a red one.
     */
    public OatStatCard setChange(IModel<? extends Number> percent) {
        return setChange(percent, true);
    }

    /**
     * How the value changed, in percent. With {@code higherIsBetter} false - e.g. for
     * costs or response times - a rise is shown red and a fall green.
     */
    public OatStatCard setChange(IModel<? extends Number> percent, boolean higherIsBetter) {
        this.change = percent;
        this.higherIsBetter = higherIsBetter;
        return this;
    }

    /**
     * The value's recent history, oldest first, drawn as a sparkline under it - e.g. the
     * last 12 months. The latest point is marked; {@code null} hides it.
     */
    public OatStatCard setTrend(IModel<? extends List<? extends Number>> trend) {
        this.trend = trend;
        return this;
    }

    /** A note next to the change, e.g. "vs. last month"; {@code null} hides it. */
    public OatStatCard setHint(IModel<String> hint) {
        this.hint.setDefaultModel(hint != null ? hint : new Model<String>());
        return this;
    }

    private String formatChange() {
        Number value = change != null ? change.getObject() : null;
        if (value == null) {
            return null;
        }
        NumberFormat format = NumberFormat.getPercentInstance(getLocale());
        format.setMaximumFractionDigits(1);
        String formatted = format.format(value.doubleValue() / 100);
        return value.doubleValue() > 0 ? "+" + formatted : formatted;
    }

    private OatVariant changeVariant() {
        double value = change != null && change.getObject() != null ? change.getObject().doubleValue() : 0;
        if (value == 0) {
            return OatVariant.SECONDARY;
        }
        return (value > 0) == higherIsBetter ? OatVariant.SUCCESS : OatVariant.DANGER;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", "card oat-stat", " ");
    }

    @Override
    protected void onDetach() {
        if (change != null) {
            change.detach();
        }
        if (trend != null) {
            trend.detach();
        }
        super.onDetach();
    }
}

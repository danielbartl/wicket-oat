package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Objects;

/**
 * A rating shown as stars, e.g. a product's average from its reviews:
 * <pre>{@code
 * add(new OatRating("score", () -> reviews.averageFor(product)));
 * }</pre>
 * on any tag, e.g. a {@code <span>}. The value is rounded to half a star; screen readers
 * read it as "4.5 out of 5 stars" ({@code OatRating.label}, translated), and
 * {@code null} as "Not rated yet" ({@code OatRating.none}) with all stars empty. The
 * stars are as tall as the text around them, so a {@code <small>} or a heading sizes
 * them. {@code OatRatingField} lets the user choose a rating.
 */
public class OatRating extends WebComponent {

    private final IModel<? extends Number> value;
    private int max = 5;

    public OatRating(String id, Number value) {
        this(id, Model.of(value));
    }

    /** @param value the rating, from 0 to the number of stars, read on every render */
    public OatRating(String id, IModel<? extends Number> value) {
        super(id);
        this.value = Objects.requireNonNull(value);
    }

    /** How many stars there are, from 1 to 10; 5 by default. */
    public OatRating setMax(int max) {
        if (max < 1 || max > 10) {
            throw new IllegalArgumentException("A rating has 1 to 10 stars, not " + max);
        }
        this.max = max;
        return this;
    }

    public int getMax() {
        return max;
    }

    /** The rating in halves of a star, between 0 and twice the number of stars, or -1 for none. */
    private int halves() {
        Number rating = value.getObject();
        if (rating == null) {
            return -1;
        }
        return (int) Math.max(0, Math.min(2L * max, Math.round(rating.doubleValue() * 2)));
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (tag.isOpenClose()) {
            tag.setType(TagType.OPEN);
        }
        tag.append("class", "oat-rating", " ");
        tag.put("role", "img");
        int halves = halves();
        if (halves < 0) {
            tag.put("aria-label", getString("OatRating.none"));
        } else {
            NumberFormat number = NumberFormat.getNumberInstance(getLocale());
            number.setMaximumFractionDigits(1);
            tag.put("aria-label", new MessageFormat(getString("OatRating.label"), getLocale())
                    .format(new Object[] {number.format(halves / 2.0), max}));
        }
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        int halves = Math.max(0, halves());
        StringBuilder stars = new StringBuilder();
        for (int star = 1; star <= max; star++) {
            String fill = halves >= 2 * star ? "full" : halves == 2 * star - 1 ? "half" : "empty";
            stars.append("<span data-fill=\"").append(fill).append("\"></span>");
        }
        replaceComponentTagBody(markupStream, openTag, stars);
    }

    @Override
    protected void onDetach() {
        value.detach();
        super.onDetach();
    }
}

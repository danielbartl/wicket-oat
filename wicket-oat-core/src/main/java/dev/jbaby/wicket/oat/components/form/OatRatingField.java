package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A rating from one to five stars - for a product, an answer from support, how a
 * sprint went - chosen by clicking a star:
 * <pre>{@code
 * form.add(new OatRatingField("rating", "How did we do?", Model.of()).setRequired(true));
 * }</pre>
 * on a {@code <div>}, like the other fields. The stars are radio buttons, one per
 * number, in a {@code <fieldset>} labelled by its {@code <legend>}: the arrow keys move
 * between them, screen readers read each as "3 stars" ({@code OatRatingField.stars},
 * translated), and a test chooses one with {@code formTester.setValue("rating:input", "4")}.
 * The chosen star and the ones before it are filled, and hovering shows what a click
 * would choose. The model holds the number of stars, {@code null} while none is
 * chosen; {@link #setMax} sets how many there are. {@code OatRating} shows a rating
 * without changing it.
 */
public class OatRatingField extends BaseOatField<Integer, RadioChoice<Integer>, OatRatingField> {

    private int max = 5;

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatRatingField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatRatingField(String id, IModel<Integer> model) {
        this(id, null, model, null);
    }

    public OatRatingField(String id, String label, IModel<Integer> model) {
        this(id, Model.of(label), model, null);
    }

    public OatRatingField(String id, IModel<String> label, IModel<Integer> model) {
        this(id, label, model, null);
    }

    public OatRatingField(String id, IModel<String> label, IModel<Integer> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    /** How many stars there are, from 1 to 10; 5 by default. */
    public OatRatingField setMax(int max) {
        if (max < 1 || max > 10) {
            throw new IllegalArgumentException("A rating has 1 to 10 stars, not " + max);
        }
        this.max = max;
        return this;
    }

    public int getMax() {
        return max;
    }

    @Override
    protected RadioChoice<Integer> newInput(String id, IModel<Integer> model) {
        RadioChoice<Integer> stars = new RadioChoice<>(id, model,
                (IModel<List<Integer>>) () -> IntStream.rangeClosed(1, max).boxed().toList(), new StarRenderer());
        // <input type="radio"><label for>3 stars</label> per star: the CSS draws the star on
        // the input and shows the label to screen readers only
        stars.setLabelPosition(RadioChoice.LabelPosition.AFTER);
        stars.add(AttributeAppender.append("class", "oat-rating-input"));
        return stars;
    }

    /** Each star's number as its value, named "3 stars". */
    private final class StarRenderer extends ChoiceRenderer<Integer> {

        @Override
        public Object getDisplayValue(Integer stars) {
            return new MessageFormat(getString("OatRatingField.stars"), getLocale()).format(new Object[] {stars});
        }

        @Override
        public String getIdValue(Integer stars, int index) {
            return String.valueOf(stars);
        }
    }
}

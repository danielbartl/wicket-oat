package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Behavior that places a component in Oat's 12-column grid. A {@link #row()} lays out
 * its children in the grid, and each child takes a {@link #col(int) column span}; on
 * narrow screens the grid drops to 4 columns and every column takes a full row.
 * {@link #container()} centers content at Oat's maximum page width.
 * <p>
 * It adds class="row", "col-N" (plus "offset-N") or "container".
 */
public class GridBehavior extends Behavior {

    /** The number of columns in Oat's grid. */
    public static final int COLUMNS = 12;

    /** The largest offset Oat has a class for. */
    public static final int MAX_OFFSET = 6;

    private final String cssClass;

    private GridBehavior(String cssClass) {
        this.cssClass = cssClass;
    }

    /** A grid row: class="row". */
    public static GridBehavior row() {
        return new GridBehavior("row");
    }

    /** A column spanning {@code span} of the 12 grid columns: class="col-N". */
    public static GridBehavior col(int span) {
        return col(span, 0);
    }

    /**
     * A column spanning {@code span} of the 12 grid columns, with an offset of
     * {@code offset} columns (0 for none): class="col-N offset-M".
     */
    public static GridBehavior col(int span, int offset) {
        if (span < 1 || span > COLUMNS) {
            throw new IllegalArgumentException("span must be between 1 and " + COLUMNS + ": " + span);
        }
        if (offset < 0 || offset > MAX_OFFSET || span + offset > COLUMNS) {
            throw new IllegalArgumentException("offset must be between 0 and " + MAX_OFFSET
                    + ", and span + offset at most " + COLUMNS + ": " + offset);
        }
        return new GridBehavior(offset == 0 ? "col-" + span : "col-" + span + " offset-" + offset);
    }

    /** A centered container with Oat's maximum page width: class="container". */
    public static GridBehavior container() {
        return new GridBehavior("container");
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.append("class", cssClass, " ");
    }
}

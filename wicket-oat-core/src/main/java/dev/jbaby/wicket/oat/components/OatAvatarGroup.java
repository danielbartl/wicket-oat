package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.util.List;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import java.util.Objects;

/**
 * A container component that renders its items as a grouped/overlapping cluster of
 * Oat Avatars, e.g. for a "team members" indicator.
 *
 * <pre>
 * &lt;figure wicket:id="group" data-variant="avatar" role="group"&gt;
 *     &lt;figure wicket:id="avatars" data-variant="avatar"&gt;
 *         &lt;img wicket:id="img"/&gt;
 *         &lt;abbr wicket:id="initials"&gt;&lt;/abbr&gt;
 *     &lt;/figure&gt;
 * &lt;/figure&gt;
 * </pre>
 *
 * @param <T> the type of the list items
 */
public abstract class OatAvatarGroup<T> extends WebMarkupContainer {

    public enum Size {
        SMALL("small"),
        DEFAULT(null),
        LARGE("large");

        private final String className;
        Size(String className) { this.className = className; }
        public String getClassName() { return className; }
    }

    public OatAvatarGroup(String id, IModel<List<T>> model) {
        this(id, model, Size.DEFAULT);
    }

    public OatAvatarGroup(String id, IModel<List<T>> model, Size size) {
        super(id);
        if (size != Size.DEFAULT) {
            add(AttributeModifier.append("class", size.getClassName()));
        }
        add(new ListView<>("avatars", model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OatAvatarGroup.this.populateItem(item);
            }
        });
    }

    protected abstract void populateItem(ListItem<T> item);

    /**
     * A group of overlapping avatars, without a subclass: {@code populateItem} receives each list item and its
     * object.
     */
    public static <T> OatAvatarGroup<T> of(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
        Objects.requireNonNull(populateItem);
        return new OatAvatarGroup<T>(id, model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }

    /**
     * A group of overlapping avatars of the given size, without a subclass: {@code populateItem} receives each list item and its
     * object.
     */
    public static <T> OatAvatarGroup<T> of(String id, IModel<List<T>> model, Size size, SerializableBiConsumer<ListItem<T>, T> populateItem) {
        Objects.requireNonNull(populateItem);
        return new OatAvatarGroup<T>(id, model, size) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }
}

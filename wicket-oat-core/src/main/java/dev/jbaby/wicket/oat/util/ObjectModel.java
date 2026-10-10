package dev.jbaby.wicket.oat.util;

import org.apache.wicket.model.IModel;

/**
 * A model holding any object, unlike {@code Model}, which needs a {@code Serializable}
 * type. The object is serialized with the page, so it must be serializable at runtime.
 */
public class ObjectModel<T> implements IModel<T> {

    private T object;

    public ObjectModel() {
    }

    public ObjectModel(T object) {
        this.object = object;
    }

    @Override
    public T getObject() {
        return object;
    }

    @Override
    public void setObject(T object) {
        this.object = object;
    }
}

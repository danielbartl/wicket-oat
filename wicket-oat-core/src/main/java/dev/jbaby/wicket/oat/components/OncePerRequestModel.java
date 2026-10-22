package dev.jbaby.wicket.oat.components;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Reads a model once per request, for a component that reads it in several places, e.g.
 * a name backed by a lookup: the object is kept until the end of the request, when the
 * model is detached too.
 */
final class OncePerRequestModel<T> extends LoadableDetachableModel<T> {

    private final IModel<T> model;

    private OncePerRequestModel(IModel<T> model) {
        this.model = model;
    }

    /** @return the model read once per request, or {@code null} for {@code null} */
    static <T> IModel<T> of(IModel<T> model) {
        return model != null ? new OncePerRequestModel<>(model) : null;
    }

    @Override
    protected T load() {
        return model.getObject();
    }

    @Override
    protected void onDetach() {
        model.detach();
    }
}

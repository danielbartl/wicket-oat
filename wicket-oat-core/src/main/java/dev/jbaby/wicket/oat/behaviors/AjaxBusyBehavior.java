package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.WicketOats;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.behavior.Behavior;

/**
 * Shows that a button's Ajax request is running, and ignores further clicks until it
 * is done. While the request runs, the component's tag gets {@code aria-busy="true"},
 * which Oat draws as a spinner and screen readers announce as busy; a second click
 * meanwhile sends no second request, so a slow save can't be submitted twice.
 * <p>
 * Turn it on for {@code OatButton}, {@code OatSubmitButton} and {@code OatDialog}'s
 * confirm button with their {@code setBusyIndicator(true)}, or add it to any other Ajax
 * link or button - a plain Wicket {@code AjaxLink}, {@code AjaxButton}, or a component
 * with an {@code AjaxEventBehavior} - with {@code Oat.Behaviors.ajaxBusy()}. It needs
 * {@link WicketOats#install}, which hooks it into the component's Ajax requests.
 */
public class AjaxBusyBehavior extends Behavior {

    /**
     * Marks the component busy when its Ajax event fires (before the request is queued,
     * so a second click finds it busy), drops requests fired while it is, and clears the
     * mark when the request is done - after success, failure or a stopped precondition.
     */
    private static final class BusyListener extends AjaxCallListener {
        BusyListener() {
            onInit("var e=Wicket.$(attrs.c);if(e){if(e.getAttribute('aria-busy')==='true'){attrs.oatBusy=true;}"
                    + "else{e.setAttribute('aria-busy','true');}}");
            onPrecondition("return !attrs.oatBusy;");
            onDone("if(!attrs.oatBusy){var e=Wicket.$(attrs.c);if(e){e.removeAttribute('aria-busy');}}");
        }
    }

    /**
     * Adds the busy handling to an Ajax behavior's requests when its component has an
     * {@link AjaxBusyBehavior}. {@link WicketOats#install} registers it once per application.
     */
    public static final class Listener implements AjaxRequestTarget.IListener {

        @Override
        public void updateAjaxAttributes(AbstractDefaultAjaxBehavior behavior, AjaxRequestAttributes attributes) {
            Component component = behavior.getComponent();
            if (component == null || component.getBehaviors(AjaxBusyBehavior.class).isEmpty()) {
                return;
            }
            for (IAjaxCallListener listener : attributes.getAjaxCallListeners()) {
                if (listener instanceof BusyListener) {
                    return;
                }
            }
            attributes.getAjaxCallListeners().add(new BusyListener());
        }
    }
}

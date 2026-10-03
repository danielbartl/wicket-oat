package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.basic.Label;

public class BadgePage extends BasePage {

    public BadgePage() {
        add(Oat.Components.badge("defaultBadge", "Default"));
        add(Oat.Components.badge("secondaryBadge", "Secondary", OatVariant.SECONDARY));
        add(Oat.Components.badge("outlineBadge", "Outline", OatVariant.SECONDARY).setOutline(true));
        add(Oat.Components.badge("successBadge", "Success", OatVariant.SUCCESS));
        add(Oat.Components.badge("warningBadge", "Warning", OatVariant.WARNING));
        add(Oat.Components.badge("dangerBadge", "Danger", OatVariant.DANGER));

        // Using behavior on a regular Label
        Label customLabel = new Label("customBehaviorBadge", "New");
        customLabel.add(Oat.Behaviors.badge(OatVariant.SUCCESS));
        add(customLabel);
    }
}

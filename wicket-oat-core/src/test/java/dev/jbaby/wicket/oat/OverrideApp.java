package dev.jbaby.wicket.oat;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;

/**
 * A test application whose OverrideApp_de.properties overrides a library text. Its
 * properties are searched first; by default a component's own resources would win.
 */
public class OverrideApp extends MockApplication {

    @Override
    protected void init() {
        super.init();
        getResourceSettings().getStringResourceLoaders().add(0, new ClassStringResourceLoader(OverrideApp.class));
    }
}

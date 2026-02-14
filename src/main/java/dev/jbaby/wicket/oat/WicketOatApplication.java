package dev.jbaby.wicket.oat;

import org.apache.wicket.Page;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WicketOatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WicketOatApplication.class, args);
    }

    @Bean
    WebApplication wicketApp(ApplicationContext ctx) {
        return new WebApplication() {
            @Override
            public Class<? extends Page> getHomePage() {
                return HomePage.class;
            }

            @Override
            protected void init() {

                super.init();

                getComponentInstantiationListeners().add(
                        new SpringComponentInjector(this, ctx));

                getCspSettings()
                        .blocking()
                        .add(CSPDirective.STYLE_SRC, "https://unpkg.com")
                        .add(CSPDirective.SCRIPT_SRC, "https://unpkg.com");
            }
        };
    }

    @Bean
    public WicketFilter wicketFilter(WebApplication wicketApp) {

        final var filter = new WicketFilter(wicketApp);
        filter.setFilterPath("/");
        return filter;

    }
}

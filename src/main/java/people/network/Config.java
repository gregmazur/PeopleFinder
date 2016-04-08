package people.network;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import people.network.service.resources.Source;
import people.network.service.rest.JsonService;

/**
 * @author Mazur G
 */
@Configuration
public class Config {
    @Bean()
    Source source() {
        Source source = new Source();
        source.setBasename("locale/res");
        return source;
    }
}

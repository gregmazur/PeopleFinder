package people.network.service.resources;

import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

/**
 * @author Mazur G
 */
public class Source extends ResourceBundleMessageSource implements SourceService {
    private Locale locale;


    @Override
    public String getMessage(String code) {
        return getMessage(code, null, locale);
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}

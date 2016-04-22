package people.network.service.resourceProvider;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * @author Mazur G
 */
@Service
public class Source extends ResourceBundleMessageSource implements SourceService {
    private Locale locale;

    public Source(){
        setBasename("locale/res");
        setDefaultEncoding("Windows-1251");
    }

    @Override
    public String getMessage(String code) {
        return getMessage(code, null, locale);
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}

package people.network.service.resourceProvider;

import java.util.Locale;
/**
 * created by Greg
 */

public interface SourceService{

    String getMessage(String code);

    void setLocale(Locale locale);
}

package people.network.resources;

import java.util.Locale;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class Resources {
    private Source source;

    public Resources(Locale locale) {
        if (locale.toString().equals("uk")) source = new ResUkr();
        else if (locale.toString().equals("ru")) source = new ResRu();
        else source = new ResEng();
    }
}

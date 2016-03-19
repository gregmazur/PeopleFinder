package people.network.rest;

import com.vaadin.ui.ComboBox;

import java.util.Collection;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class Utils {
    public static final String GET_CITIES_METHOD = "database.getCities";
    public static final String GET_COUNTRIES_METHOD = "database.getCountries";
    public static final String GET_UNIVERSITIES_METHOD = "database.getUniversities";
    public static final String GET_SCHOOLS_METHOD = "database.getSchools";
    public static final String GET_FACULTIES_METHOD = "database.getFaculties";
    public static final String GET_GROUPS_METHOD = "groups.search";

    public static void bindItemsToComboBox(ComboBox comboBox, Collection items){
        for (Object o : items){
            comboBox.addItem(o);
            comboBox.setItemCaption(o,o.toString());
        }
    }
}

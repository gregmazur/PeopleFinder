package people.network.service.rest;

import com.vaadin.server.Page;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class Utils {
    public static final String GET_CITIES_METHOD = "database.getCities";
    public static final String GET_COUNTRIES_METHOD = "database.getCountries";
    public static final String GET_UNIVERSITIES_METHOD = "database.getUniversities";
    public static final String GET_SCHOOLS_METHOD = "database.getSchools";
    public static final String GET_FACULTIES_METHOD = "database.getFaculties";
    public static final String GET_REGIONS_METHOD = "database.getRegions";

    public static final String GET_GROUPS_METHOD = "groups.search";

    public static final String GET_USERS_METHOD = "users.search";



    public static void showError(){
        new Notification("Error",
                "<br/>Couldn`t connect to VK",
                Notification.Type.WARNING_MESSAGE, true)
                .show(Page.getCurrent());
    }

    public static void putParam(MultiValueMap<String,String> map,String key, String value) {
        LinkedList<String> list = new LinkedList<>();
        list.add(value);
        map.put(key, list);
    }
}

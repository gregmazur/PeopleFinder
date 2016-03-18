package people.network.UI;

import com.vaadin.ui.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import people.network.UI.utils.InfoRole;
import people.network.entity.RespSrchCrtriaObj;
import people.network.rest.JsonService;
import people.network.rest.Utils;

import java.util.Collection;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class FindingForm extends VerticalLayout {

    private JsonService service;
    private MultiValueMap<String, String> userSearchParams = new LinkedMultiValueMap<>(35);
    ComboBox homeCountry = new ComboBox("Home country"), homeCity = new ComboBox("Home city"), school,

    currentCountry = new ComboBox("Current country"), currentCity = new ComboBox("Current city"),

    universityCountry = new ComboBox("University country"), universityCity = new ComboBox("University city"), university;

    private Button submit;
    private TextArea result;


    public FindingForm(JsonService service) {
        super();
        this.service = service;
        init();
    }

    private void init() {
        Collection<RespSrchCrtriaObj> countries = getCountriesList();

        TextField name = new TextField("name");
        name.setWidth(80, Unit.PERCENTAGE);
        name.setImmediate(true);
        addComponent(name);

        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(currentInfo(countries));
        layout.addComponent(universityInfo(countries));
        layout.addComponent(homeInfo(countries));
        addComponent(layout);

        submit = new Button("Submit", event -> {
            result.setValue(name.getValue()
                    + userSearchParams.get("school_country"));
        });
        addComponent(submit);
        result = new TextArea();
        addComponent(result);
    }

    private VerticalLayout homeInfo(Collection<RespSrchCrtriaObj> countries) {
        InfoRole role = InfoRole.HOME;
        VerticalLayout layout = fillCityCountry(countries, role);
        school = new ComboBox("School");
        school.setVisible(false);
        school.setImmediate(true);
        layout.addComponent(school);
        homeCity.addValueChangeListener(event1 -> {
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) homeCity.getValue();
            if (null != o) {
                school.setVisible(true);
                loadSchools(school, 0, o.getId(), null, role);
            } else school.setVisible(false);
        });
        return layout;
    }

    private VerticalLayout currentInfo(Collection<RespSrchCrtriaObj> countries) {
        InfoRole role = InfoRole.CURRENT;
        return fillCityCountry(countries, role);
    }

    private VerticalLayout universityInfo(Collection<RespSrchCrtriaObj> countries) {
        InfoRole role = InfoRole.UNIVERSITY;
        VerticalLayout layout = fillCityCountry(countries, role);
        university = new ComboBox("University");
        university.setVisible(false);
        university.setImmediate(true);
        layout.addComponent(university);
        universityCity.addValueChangeListener(event1 -> {
            RespSrchCrtriaObj oCity = (RespSrchCrtriaObj) universityCity.getValue();
            RespSrchCrtriaObj oCountry = (RespSrchCrtriaObj) universityCountry.getValue();
            if (null != oCity && null != oCountry) {
                university.setVisible(true);
                loadSchools(university, oCountry.getId(), oCity.getId(), null, role);
            } else university.setVisible(false);
        });
        universityCity.setNewItemHandler(newItemCaption -> {

        });
        return layout;
    }

    private VerticalLayout fillCityCountry(Collection<RespSrchCrtriaObj> countries, InfoRole role) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth(30, Unit.PERCENTAGE);
        ComboBox countryCB = bindCountry(role);
        ComboBox cityCB = bindCity(role);
        countryCB.setWidth(30, Unit.PERCENTAGE);
        countryCB.setImmediate(true);
        Utils.bindItemsToComboBox(countryCB, countries);

        countryCB.addValueChangeListener(event -> {
            cityCB.setVisible(true);
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) countryCB.getValue();
            cityCB.removeAllItems();
            if (null != o){
                loadCities(cityCB, o.getId(), null);
                cityCB.setVisible(false);
            }
            cityCB.setValue(null);
        });
        layout.addComponent(countryCB);
        cityCB.setImmediate(true);
        cityCB.addValueChangeListener(event -> {

        });
        cityCB.setNewItemsAllowed(true);
        cityCB.setNewItemHandler(newItemCaption -> {
            cityCB.removeAllItems();
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) countryCB.getValue();
            loadCities(cityCB, o.getId(), newItemCaption);
        });
        cityCB.setVisible(false);
        layout.addComponent(cityCB);
        return layout;
    }

    private ComboBox bindCountry(InfoRole role) {
        ComboBox country = null;
        if (role == InfoRole.HOME) {
            country = homeCountry;
        } else if (role == InfoRole.UNIVERSITY) {
            country = universityCountry;
        } else if (role == InfoRole.CURRENT) {
            country = currentCountry;
        }
        return country;
    }

    private ComboBox bindCity(InfoRole role) {
        ComboBox city = null;
        if (role == InfoRole.HOME) {
            city = homeCity;
        } else if (role == InfoRole.UNIVERSITY) {
            city = universityCity;
        } else if (role == InfoRole.CURRENT) {
            city = currentCity;
        }
        return city;
    }

    private Collection<RespSrchCrtriaObj> getCountriesList() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("need_all", "1");
        return service.getCriteriaList(Utils.GET_COUNTRIES_METHOD, map, 1000, 0);
    }

    private void loadCities(ComboBox comboBox, int country, String name) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        if (null != name) map.add("q", name);
        map.add("need_all", "1");
        map.add("country_id", String.valueOf(country));
        Collection<RespSrchCrtriaObj> items = service.getCriteriaList(Utils.GET_CITIES_METHOD, map, 1000, 0);
        Utils.bindItemsToComboBox(comboBox, items);
    }

    private void loadSchools(ComboBox comboBox, int country, int city, String name, InfoRole role) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        if (null != name) map.add("q", name);
        map.add("need_all", "1");
        if (0 < country) map.add("country_id", String.valueOf(country));
        map.add("city_id", String.valueOf(city));
        String method = InfoRole.HOME == role ? Utils.GET_SCHOOLS_METHOD : Utils.GET_UNIVESITIES_METHOD;
        Collection<RespSrchCrtriaObj> items = service.getCriteriaList(method, map, 1000, 0);
        Utils.bindItemsToComboBox(comboBox, items);
    }

}

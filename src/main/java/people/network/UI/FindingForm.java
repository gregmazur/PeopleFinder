package people.network.UI;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import people.network.entity.ResponseSearchCriteriaObj;
import people.network.rest.JsonService;
import people.network.rest.Utils;

import java.util.Collection;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class FindingForm extends VerticalLayout {

    private JsonService service;
    private MultiValueMap<String, String> userSearchParams = new LinkedMultiValueMap<>(35);
    private Button submit;
    private TextArea result;

    public FindingForm(JsonService service) {
        super();
        this.service = service;
        init();
    }

    private void init() {
        Collection<ResponseSearchCriteriaObj> countries = getCountriesList();

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

    private VerticalLayout homeInfo(Collection<ResponseSearchCriteriaObj> countries) {
        ComboBox homeCountry = new ComboBox("Home country"), homeCity = new ComboBox("Home city");
        return fillCityCountry(countries, homeCountry, homeCity, "school_country");
    }

    private VerticalLayout currentInfo(Collection<ResponseSearchCriteriaObj> countries) {
        ComboBox currentCountry = new ComboBox("Current country"), currentCity = new ComboBox("Current city");
        return fillCityCountry(countries, currentCountry, currentCity, "city");
    }

    private VerticalLayout universityInfo(Collection<ResponseSearchCriteriaObj> countries) {
        ComboBox universityCountry = new ComboBox("University country"), universityCity = new ComboBox("University city");
        return fillCityCountry(countries, universityCountry, universityCity,
                "university_country");
    }

    private VerticalLayout fillCityCountry(Collection<ResponseSearchCriteriaObj> countries, ComboBox countryCB, ComboBox cityCB,
                                           String countryParam) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth(30, Unit.PERCENTAGE);
        countryCB.setWidth(80, Unit.PERCENTAGE);
        Utils.bindItemsToComboBox(countryCB, countries);
        final ComboBox finalCountryCB = countryCB;
        final ComboBox finalCityCB = cityCB;
        countryCB.addValueChangeListener(event -> {
            finalCityCB.setVisible(true);
            ResponseSearchCriteriaObj o = (ResponseSearchCriteriaObj) finalCountryCB.getValue();
            userSearchParams.add(countryParam, String.valueOf(o.getId()));
            loadCities(finalCityCB, o.getId());
        });
        layout.addComponent(countryCB);

        cityCB.setVisible(false);
        layout.addComponent(cityCB);
        return layout;
    }

    private Collection<ResponseSearchCriteriaObj> getCountriesList() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("need_all", "1");
        return service.getCriteriaList(Utils.GET_COUNTRIES_METHOD, map);
    }

    private void loadCities(ComboBox comboBox, int country) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("need_all", "1");
        map.add("country_id", String.valueOf(country));
        Collection<ResponseSearchCriteriaObj> items = service.getCriteriaList(Utils.GET_CITIES_METHOD, map);
        Utils.bindItemsToComboBox(comboBox, items);
    }

}

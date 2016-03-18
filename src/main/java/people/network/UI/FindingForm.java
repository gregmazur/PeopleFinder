package people.network.UI;

import com.vaadin.ui.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    universityCountry = new ComboBox("University country"), universityCity = new ComboBox("University city"), university, faculty,

    sex, status;

    private TextField ageFrom, ageTo;

    private Button submit;
    private TextArea result;

    private enum InfoRole {
        HOME, UNIVERSITY, CURRENT
    }

    private enum Sex {
        MALE(2), FEMALE(1);
        private final int value;

        Sex(int value) {
            this.value = value;
        }
    }

    private enum Staus {
        NOTMARRIED(1), DATING(2), ENGAGED(3), MARRIED(4), INLOVE(7), COMPLICATED(5), ACTIVELYSEARCHING(6);
        private final int value;

        Staus(int value) {
            this.value = value;
        }
    }


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

        addComponent(commonInfo());

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
            school.removeAllItems();
            if (null != o) {
                school.setVisible(true);
                loadSchools(school, 0, o.getId(), null, role);
            } else school.setVisible(false);
            school.setValue(null);
        });
        school.setNewItemHandler(newItemCaption -> {
            school.removeAllItems();
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) homeCity.getValue();
            loadSchools(school, 0, o.getId(), newItemCaption, role);
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
            university.removeAllItems();
            if (null != oCity && null != oCountry) {
                university.setVisible(true);
                loadSchools(university, oCountry.getId(), oCity.getId(), null, role);
            } else university.setVisible(false);
            university.setValue(null);
        });
        university.setNewItemHandler(newItemCaption -> {
            university.removeAllItems();
            RespSrchCrtriaObj oCity = (RespSrchCrtriaObj) universityCity.getValue();
            RespSrchCrtriaObj oCountry = (RespSrchCrtriaObj) universityCountry.getValue();
            loadSchools(university, oCountry.getId(), oCity.getId(), newItemCaption, role);
        });
        faculty = new ComboBox("Faculty");
        faculty.setVisible(false);
        faculty.setImmediate(true);
        university.addValueChangeListener(event -> {
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) university.getValue();
            faculty.removeAllItems();
            if (null != o) {
                faculty.setVisible(true);
                loadFaculties(o.getId());
            } else faculty.setVisible(false);
            faculty.setValue(null);
        });
        return layout;
    }

    private VerticalLayout commonInfo() {
        VerticalLayout layout = new VerticalLayout();
        sex = new ComboBox("Sex");
        sex.addItem(Sex.FEMALE);
        sex.addItem(Sex.MALE);
        layout.addComponent(sex);
        status = new ComboBox("Staus");
        status.addItem(Staus.ACTIVELYSEARCHING);
        status.addItem(Staus.COMPLICATED);
        status.addItem(Staus.DATING);
        status.addItem(Staus.ENGAGED);
        status.addItem(Staus.MARRIED);
        status.addItem(Staus.NOTMARRIED);
        layout.addComponent(status);

        HorizontalLayout ageLayout = new HorizontalLayout();
        ageFrom = new TextField("age from");
        ageFrom.addValueChangeListener(event -> {
            String text = ageFrom.getValue();
            if (null == text) return;
            try {
                int ageFromValue = (int) ageFrom.getConvertedValue();
                if (ageFromValue > (int) ageTo.getConvertedValue()) ageFrom.setValue(null);
            } catch (Exception e) {
                ageFrom.setValue(null);
            }
        });
        ageLayout.addComponent(ageFrom);
        ageTo = new TextField("age to");
        ageTo.addValueChangeListener(event -> {
            String text = ageTo.getValue();
            if (null == text) return;
            try {
                Integer ageToValue = (Integer) ageTo.getConvertedValue();
                if (ageToValue < (int) ageFrom.getConvertedValue()) ageTo.setValue(null);
            } catch (Exception e) {
                ageTo.setValue(null);
            }
        });
        ageLayout.addComponent(ageTo);
        layout.addComponent(ageLayout);

        DateField dateField = new DateField();
        layout.addComponent(dateField);


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
            if (null != o) {
                loadCities(cityCB, o.getId(), null);
                cityCB.setVisible(true);
            } else cityCB.setVisible(false);
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
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(6);
        if (null != name) map.add("q", name);
        map.add("need_all", "1");
        map.add("country_id", String.valueOf(country));
        Collection<RespSrchCrtriaObj> items = service.getCriteriaList(Utils.GET_CITIES_METHOD, map, 1000, 0);
        Utils.bindItemsToComboBox(comboBox, items);
    }

    private void loadSchools(ComboBox comboBox, int country, int city, String name, InfoRole role) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(7);
        if (null != name) map.add("q", name);
        map.add("need_all", "1");
        if (0 < country) map.add("country_id", String.valueOf(country));
        map.add("city_id", String.valueOf(city));
        String method = InfoRole.HOME == role ? Utils.GET_SCHOOLS_METHOD : Utils.GET_UNIVESITIES_METHOD;
        Collection<RespSrchCrtriaObj> items = service.getCriteriaList(method, map, 1000, 0);
        Utils.bindItemsToComboBox(comboBox, items);
    }

    private void loadFaculties(int university) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("university_id", String.valueOf(university));
        Collection<RespSrchCrtriaObj> items = service.getCriteriaList(Utils.GET_CITIES_METHOD, map, 1000, 0);
        Utils.bindItemsToComboBox(faculty, items);
    }

}

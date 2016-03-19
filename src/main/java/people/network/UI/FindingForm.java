package people.network.UI;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import people.network.entity.RespSrchCrtriaObj;
import people.network.rest.JsonService;
import people.network.rest.Utils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class FindingForm extends VerticalLayout implements Serializable {

    private static final long serialVersionUID = -6575751250735498511L;

    private JsonService service;
    private MultiValueMap<String, String> userSearchParams = new LinkedMultiValueMap<>(35);
    ComboBox homeCountry = new ComboBox("Home country"), homeCity = new ComboBox("Home city"),

    currentCountry = new ComboBox("Current country"), currentCity = new ComboBox("Current city"),

    universityCountry = new ComboBox("University country"), universityCity = new ComboBox("University city");
    private TextField name = new TextField("name");
    private Button submit;
    private TextArea result;

    private enum InfoRole {
        HOME, UNIVERSITY, CURRENT
    }

    private enum Sex {
        MALE("2"), FEMALE("1");
        private final String value;

        Sex(String value) {
            this.value = value;
        }
    }

    private enum Status {
        NOTMARRIED("1"), DATING("2"), ENGAGED("3"), MARRIED("4"), INLOVE("7"), COMPLICATED("5"), SEARCHING("6");
        private final String value;

        Status(String value) {
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
        ComboBox school = new ComboBox("School");
        school.setVisible(false);
        school.setImmediate(true);
        layout.addComponent(school);
        homeCity.addValueChangeListener(event1 -> {
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) homeCity.getValue();
            school.removeAllItems();
            if (null != o) {
                school.setVisible(true);
                loadSchools(school, 0, o.getId(), null, role);
                putParam("hometown", o.getId());
            } else school.setVisible(false);
            school.setValue(null);
        });
        school.setNewItemHandler(newItemCaption -> {
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) homeCity.getValue();
            loadSchools(school, 0, o.getId(), newItemCaption, role);
        });
        school.addValueChangeListener(event -> {
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) school.getValue();
            if (null != o) putParam("school", o.getId());
        });

        return layout;
    }

    private VerticalLayout currentInfo(Collection<RespSrchCrtriaObj> countries) {
        InfoRole role = InfoRole.CURRENT;
        currentCity.addValueChangeListener(event -> {
            RespSrchCrtriaObj city = (RespSrchCrtriaObj) currentCity.getValue();
            RespSrchCrtriaObj country = (RespSrchCrtriaObj) currentCountry.getValue();
            if (null != country && null != city) {
                putParam("city", city.getId());
                putParam("country", country.getId());
            }
        });
        return fillCityCountry(countries, role);
    }

    private VerticalLayout universityInfo(Collection<RespSrchCrtriaObj> countries) {
        InfoRole role = InfoRole.UNIVERSITY;
        VerticalLayout layout = fillCityCountry(countries, role);
        ComboBox university = new ComboBox("University");
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
                putParam("university_country", oCountry.getId());
            } else university.setVisible(false);
            university.setValue(null);
        });

        university.setNewItemHandler(newItemCaption -> {
            university.removeAllItems();
            RespSrchCrtriaObj oCity = (RespSrchCrtriaObj) universityCity.getValue();
            RespSrchCrtriaObj oCountry = (RespSrchCrtriaObj) universityCountry.getValue();
            loadSchools(university, oCountry.getId(), oCity.getId(), newItemCaption, role);
        });
        ComboBox faculty = new ComboBox("Faculty");
        faculty.setVisible(false);
        faculty.setImmediate(true);
        university.addValueChangeListener(event -> {
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) university.getValue();
            faculty.removeAllItems();
            if (null != o) {
                faculty.setVisible(true);
                loadFaculties(faculty, o.getId());
                putParam("university", o.getId());
            } else faculty.setVisible(false);
            faculty.setValue(null);
        });
        faculty.addValueChangeListener(event -> {
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) faculty.getValue();
            if (null != o) putParam("university_faculty", String.valueOf(o.getId()));
        });
        layout.addComponents(faculty);

        return layout;
    }

    private VerticalLayout commonInfo() {
        VerticalLayout layout = new VerticalLayout();
        ComboBox sex = new ComboBox("Sex");
        sex.addItem(Sex.FEMALE);
        sex.addItem(Sex.MALE);
        sex.addValueChangeListener(event -> {
            Sex sex1 = (Sex) sex.getValue();
            if (null != sex1) putParam("sex", sex1.value);
        });
        layout.addComponent(sex);
        ComboBox status = new ComboBox("Status");
        status.addItem(Status.SEARCHING);
        status.addItem(Status.COMPLICATED);
        status.addItem(Status.DATING);
        status.addItem(Status.ENGAGED);
        status.addItem(Status.MARRIED);
        status.addItem(Status.NOTMARRIED);
        status.addItem(Status.INLOVE);
        status.addValueChangeListener(event -> {
            Status status1 = (Status) status.getValue();
            if (null != status1) putParam("status", status1.value);
        });
        layout.addComponent(status);

        HorizontalLayout ageLayout = new HorizontalLayout();
        TextField ageFrom = new TextField("age from");
        ageFrom.setImmediate(true);
        TextField ageTo = new TextField("age to");
        ageTo.setImmediate(true);
        ageFrom.addValueChangeListener(event -> {
            String text = ageFrom.getValue();
            if (null == text) return;//TODO
            try {
                int ageFromValue = (int) ageFrom.getConvertedValue();
                if (ageFromValue > (int) ageTo.getConvertedValue()) ageFrom.clear();
                else putParam("age_from", ageFromValue);
            } catch (Exception e) {
                ageFrom.clear();
            }
        });
        ageLayout.addComponent(ageFrom);
        ageTo.addValueChangeListener(event -> {
            String text = ageTo.getValue();
            if (null == text) return;
            try {
                int ageToValue = (int) ageTo.getConvertedValue();
                if (ageToValue < (int) ageFrom.getConvertedValue()) ageTo.clear();
                else putParam("age_to", ageToValue);
            } catch (Exception e) {
                ageTo.clear();
            }
        });
        ageLayout.addComponent(ageTo);
        layout.addComponent(ageLayout);

        layout.addComponent(addDate());

        ComboBox group = new ComboBox("Type name of group and press Enter");//TODO
        group.setImmediate(true);
        group.setNewItemHandler(caption -> {
            group.removeAllItems();
            if (null != caption && !caption.isEmpty()) loadGroups(group, caption);
        });
        group.addValueChangeListener(event -> {
            if (null != group.getValue()) putParam("group_id", ((RespSrchCrtriaObj) group.getValue()).getId());
        });
        layout.addComponents(group);

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
            cityCB.setValue(newItemCaption);
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

    private HorizontalLayout addDate() {
        HorizontalLayout layout = new HorizontalLayout();
        ComboBox day = new ComboBox("Day"), month = new ComboBox("Month"), year = new ComboBox("Year");
        year.setImmediate(true);
        for (int i = LocalDate.now().getYear(); i > 1900; i--) {
            year.addItem(i);
        }
        Property.ValueChangeListener listener = event -> {
            Integer y = (Integer) year.getValue();
            Integer m = (Integer) month.getValue();
            if (null != y && null != m) {
                day.removeAllItems();
                int length = YearMonth.of(y, m).lengthOfMonth();
                for (int i = 1; i <= length; i++) {
                    day.addItem(i);
                }
                putParam("birth_year", y);
                putParam("birth_month", m);
            }
        };
        year.addValueChangeListener(listener);
        layout.addComponents(year);
        month.setImmediate(true);
        month.addValueChangeListener(listener);
        layout.addComponents(month);
        for (int i = 1; i <= 12; i++) {
            month.addItem(i);
        }
        day.setImmediate(true);
        day.addValueChangeListener(event -> {
            if (null != day.getValue()) putParam("birth_day", (String) day.getValue());
        });
        layout.addComponents(day);
        return layout;
    }


    private Collection<RespSrchCrtriaObj> getCountriesList() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("need_all", "1");
        return service.getCriteriaList(Utils.GET_COUNTRIES_METHOD, map, 1000, 0);
    }

    private void loadCities(ComboBox comboBox, long country, String name) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(6);
        if (null != name) map.add("q", name);
        map.add("need_all", "1");
        map.add("country_id", String.valueOf(country));
        loader(comboBox, map, Utils.GET_CITIES_METHOD);
    }

    private void loadSchools(ComboBox comboBox, long country, long city, String name, InfoRole role) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(7);
        if (null != name) map.add("q", name);
        map.add("need_all", "1");
        if (0 < country) map.add("country_id", String.valueOf(country));
        map.add("city_id", String.valueOf(city));
        String method = InfoRole.HOME == role ? Utils.GET_SCHOOLS_METHOD : Utils.GET_UNIVERSITIES_METHOD;
        loader(comboBox, map, method);
    }

    private void loadFaculties(ComboBox faculty, long university) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("university_id", String.valueOf(university));
        loader(faculty, map, Utils.GET_FACULTIES_METHOD);
    }

    private void loadGroups(ComboBox group, String name) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(7);
        if (null != name) map.add("q", name);
        loader(group, map, Utils.GET_GROUPS_METHOD);
    }

    private void loader(ComboBox comboBox, MultiValueMap<String, String> map, String method) {
        Collection<RespSrchCrtriaObj> items = service.getCriteriaList(method, map, 1000, 0);
        Utils.bindItemsToComboBox(comboBox, items);
    }

    private void putParam(String key, String value) {
        LinkedList<String> list = new LinkedList<>();
        list.add(value);
        userSearchParams.put(key, list);
    }

    private void putParam(String key, long value) {
        putParam(key, String.valueOf(value));
    }

}

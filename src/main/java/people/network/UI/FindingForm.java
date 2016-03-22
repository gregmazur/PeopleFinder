package people.network.UI;

import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import people.network.entity.criteria.RespSrchCrtriaObj;
import people.network.rest.Utils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class FindingForm extends VerticalLayout implements Serializable, View {

    private static final long serialVersionUID = -6575751250735498511L;

    private MainPage mainPage;
    ComboBox homeCountry = new ComboBox("Home country"), homeCity = new ComboBox("Home city"),
            homeRegion = new ComboBox("Home region"),

    currentCountry = new ComboBox("Current country"), currentCity = new ComboBox("Current city"),
            currentRegion = new ComboBox("Current region"),

    universityCountry = new ComboBox("University country"), universityCity = new ComboBox("University city"),
            universityRegion = new ComboBox("University region");
    private TextField name = new TextField("name");
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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

    private enum Status {
        NOTMARRIED("1"), DATING("2"), ENGAGED("3"), MARRIED("4"), INLOVE("7"), COMPLICATED("5"), SEARCHING("6");
        private final String value;

        Status(String value) {
            this.value = value;
        }
    }


    public FindingForm(MainPage mainPage) {
        super();
        this.mainPage = mainPage;
        init();
    }

    private void init() {
        setResponsive(true);
        String token = getAccessToken();
        if (null != token) mainPage.getService().setAccessToken(token);
        else openSignInWindow();
        Collection<RespSrchCrtriaObj> countries = getCountriesList();
        name.setWidth(50, Unit.PERCENTAGE);
        addComponent(name);
        setComponentAlignment(name, Alignment.MIDDLE_CENTER);

        HorizontalLayout placesInfo = new HorizontalLayout();
        placesInfo.addComponent(currentInfo(countries));
        placesInfo.addComponent(universityInfo(countries));
        placesInfo.addComponent(homeInfo(countries));
        addComponent(placesInfo);
        setComponentAlignment(placesInfo, Alignment.MIDDLE_CENTER);

        ComboBox sex = getSex();
        addComponents(sex);
        sex.setWidth(50, Unit.PERCENTAGE);
        setComponentAlignment(sex, Alignment.MIDDLE_CENTER);

        ComboBox status = getStatus();
        addComponents(status);
        status.setWidth(50, Unit.PERCENTAGE);
        setComponentAlignment(status, Alignment.MIDDLE_CENTER);

        HorizontalLayout ageLayout = getAgeLayout();
        addComponents(ageLayout);
        setComponentAlignment(ageLayout, Alignment.MIDDLE_CENTER);

        HorizontalLayout birthDate = getBirthDate();
        addComponents(birthDate);
        setComponentAlignment(birthDate, Alignment.MIDDLE_CENTER);

        ComboBox group = getGroup();
        addComponents(group);
        group.setWidth(50, Unit.PERCENTAGE);
        setComponentAlignment(group, Alignment.MIDDLE_CENTER);

        Button submit = new Button("Submit", event -> {
            String name = this.name.getValue();
            if (null != name) putParam("q", name);
            mainPage.getNavigator().navigateTo(MainPage.PEOPLE_FOUND);
        });
        addComponent(submit);
        setComponentAlignment(submit, Alignment.MIDDLE_CENTER);
        result = new TextArea();
        addComponent(result);
        setComponentAlignment(result, Alignment.MIDDLE_CENTER);
    }

    private String getAccessToken() {
        String uri = mainPage.getPage().getUriFragment();
        if (null != uri && uri.indexOf("access_token") > -1) return uri.substring(uri.indexOf("=") + 1, uri.indexOf("&"));
        return null;
    }

    private void openSignInWindow() {
        Window subWindow = new Window("Welcome");
        subWindow.setModal(true);
        VerticalLayout subContent = new VerticalLayout();
        subContent.setMargin(true);
        subWindow.setContent(subContent);
        subWindow.setWidth(30, Unit.PERCENTAGE);
        subWindow.setHeight(30, Unit.PERCENTAGE);

        subContent.addComponent(new Label("Please login first"));
        String link = "https://oauth.vk.com/authorize?client_id=5343222&display=page&redirect_uri=http://localhost:8080&scope=friends&response_type=token&v=5.8";
        subContent.addComponent(new Button("LOGIN", event -> {
            mainPage.getPage().open(link, "login");
            subWindow.close();
        }));
        // Center it in the browser window
        subWindow.center();

        // Open it in the UI
        mainPage.addWindow(subWindow);
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

    private ComboBox getSex(){
        ComboBox sex = new ComboBox("Sex");
        sex.addItem(Sex.FEMALE);
        sex.addItem(Sex.MALE);
        sex.addValueChangeListener(event -> {
            Sex sex1 = (Sex) sex.getValue();
            if (null != sex1) putParam("sex", sex1.value);
        });
        return sex;
    }

    private ComboBox getStatus(){
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
        return status;
    }

    private ComboBox getGroup(){
        ComboBox group = new ComboBox("Group");
        group.setImmediate(true);
        group.setNewItemsAllowed(true);
        group.setNewItemHandler(caption -> {
            group.removeAllItems();
            if (null != caption && !caption.isEmpty()) loadGroups(group, caption);
        });
        return group;
    }

    private VerticalLayout fillCityCountry(Collection<RespSrchCrtriaObj> countries, InfoRole role) {
        VerticalLayout layout = new VerticalLayout();
        ComboBox bindedCB[] = bindCountryRegionCity(role);
        ComboBox countryCB = bindedCB[0];
        ComboBox regionCB = bindedCB[1];
        ComboBox cityCB = bindedCB[2];
        countryCB.setImmediate(true);

        Utils.bindItemsToComboBox(countryCB, countries);

        countryCB.addValueChangeListener(event -> {
            regionCB.setVisible(true);
            RespSrchCrtriaObj o = (RespSrchCrtriaObj) countryCB.getValue();
            regionCB.removeAllItems();
            if (null != o) {
                loadRegions(regionCB, o.getId());
                regionCB.setVisible(true);
            } else regionCB.setVisible(false);
            regionCB.setValue(null);
        });
        layout.addComponent(countryCB);

        regionCB.setVisible(false);
        regionCB.setImmediate(true);
        regionCB.addValueChangeListener(event -> {
            cityCB.setVisible(true);
            RespSrchCrtriaObj cntry = (RespSrchCrtriaObj) countryCB.getValue();
            RespSrchCrtriaObj rgn = (RespSrchCrtriaObj) regionCB.getValue();
            cityCB.removeAllItems();
            if (null != cntry && null != rgn) {
                loadCities(cityCB, cntry.getId(), rgn.getId(), null);
            }
            cityCB.setValue(null);
        });
        layout.addComponents(regionCB);

        cityCB.setImmediate(true);
        cityCB.setNewItemsAllowed(true);
        cityCB.setNewItemHandler(newItemCaption -> {
            cityCB.removeAllItems();
            RespSrchCrtriaObj cntry = (RespSrchCrtriaObj) countryCB.getValue();
            RespSrchCrtriaObj rgn = (RespSrchCrtriaObj) regionCB.getValue();
            loadCities(cityCB, cntry.getId(), rgn.getId(), newItemCaption);
            cityCB.setValue(newItemCaption);
        });
        cityCB.setVisible(false);
        layout.addComponent(cityCB);
        return layout;
    }

    private ComboBox[] bindCountryRegionCity(InfoRole role) {
        ComboBox[] result = new ComboBox[3];
        ComboBox country = null, region = null, city = null;
        if (role == InfoRole.HOME) {
            country = homeCountry;
            region = homeRegion;
            city = homeCity;
        } else if (role == InfoRole.UNIVERSITY) {
            country = universityCountry;
            region = universityRegion;
            city = universityCity;
        } else if (role == InfoRole.CURRENT) {
            country = currentCountry;
            region = currentRegion;
            city = currentCity;
        }
        result[0] = country;
        result[1] = region;
        result[2] = city;
        return result;
    }

    private HorizontalLayout getBirthDate() {
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
            if (null != day.getValue()) putParam("birth_day", String.valueOf(day.getValue()));
        });
        layout.addComponents(day);
        return layout;
    }

    private HorizontalLayout getAgeLayout() { //TODO error
        HorizontalLayout ageLayout = new HorizontalLayout();
        ComboBox ageFrom = new ComboBox("age from");
        ComboBox ageTo = new ComboBox("age to");

        for (int i = 1; i < 100; i++) {
            ageFrom.addItem(i);
            ageTo.addItem(i);
        }
        ageFrom.addValueChangeListener(event -> {
            Integer from = (Integer) ageFrom.getValue();
            Integer to = (Integer) ageTo.getValue();
            if (null == from) return;
            if (null != to && from > to) ageTo.removeAllItems();
            for (int i = from; i < 100; i++) {
                ageTo.addItem(i);
            }
            putParam("age_from", from);
        });
        ageTo.addValueChangeListener(event -> {
            Integer from = (Integer) ageFrom.getValue();
            Integer to = (Integer) ageTo.getValue();
            if (null == to) return;
            if (null != from && from > to) ageFrom.removeAllItems();
            for (int i = 1; i < to; i++) {
                ageFrom.addItem(i);
            }
            putParam("age_to", to);
        });
        ageLayout.addComponent(ageFrom);
        ageLayout.addComponent(ageTo);
        return ageLayout;
    }


    private Collection<RespSrchCrtriaObj> getCountriesList() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("need_all", "1");
        return mainPage.getService().getCriteriaList(Utils.GET_COUNTRIES_METHOD, map, 1000, 0);
    }

    private void loadRegions(ComboBox comboBox, long country) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(4);
        map.add("country_id", String.valueOf(country));
        loader(comboBox, map, Utils.GET_REGIONS_METHOD);
    }

    private void loadCities(ComboBox comboBox, long country, long region, String name) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(7);
        if (null != name) map.add("q", name);
        map.add("need_all", "1");
        map.add("country_id", String.valueOf(country));
        map.add("region_id", String.valueOf(region));
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
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(5);
        if (null != name) map.add("q", name);
        loader(group, map, Utils.GET_GROUPS_METHOD);
    }

    private void loader(ComboBox comboBox, MultiValueMap<String, String> map, String method) {
        Collection<RespSrchCrtriaObj> items = mainPage.getService().getCriteriaList(method, map, 1000, 0);
        Utils.bindItemsToComboBox(comboBox, items);
    }

    private void putParam(String key, String value) {
        LinkedList<String> list = new LinkedList<>();
        list.add(value);
        mainPage.getSearchPerson().getUserSearchParams().put(key, list);
    }

    private void putParam(String key, long value) {
        putParam(key, String.valueOf(value));
    }

}

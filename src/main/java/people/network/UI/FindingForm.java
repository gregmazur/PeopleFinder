package people.network.UI;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import people.network.entity.ResponseSearchCriteriaObj;
import people.network.rest.JsonService;

import java.util.Collection;

/**
 *
 *
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/
public class FindingForm extends VerticalLayout {

    private JsonService service;

    public FindingForm(JsonService service) {
        super();
        this.service = service;

       init();
    }

    private void init(){
        HorizontalLayout layout = new HorizontalLayout();
        addComponent(layout);

        TextField name = new TextField();
        layout.addComponent(name);
//        Collection<ResponseSearchCriteriaObj> items = service.getCriteriaList("database.getCountries", "u");
//        BeanItemContainer<ResponseSearchCriteriaObj> objects = new BeanItemContainer<>(ResponseSearchCriteriaObj.class, items);
        ComboBox country = new ComboBox("country");
        addComponent(country);
    }

}

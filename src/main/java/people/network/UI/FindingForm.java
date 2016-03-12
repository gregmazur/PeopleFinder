package people.network.UI;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.springframework.web.client.RestTemplate;
import people.network.entity.Country;

/**
 *
 *
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/
public class FindingForm extends VerticalLayout {

    private String accessToken;

    public FindingForm(String accessToken) {
        super();
        this.accessToken = accessToken;
        addComponent(new Label(accessToken));
        Country country = new Country();
        country.getText();
    }



    final String uri = "http://localhost:8080/springrestexample/employees.xml";

    RestTemplate restTemplate = new RestTemplate();
    String result = restTemplate.getForObject(uri, String.class);




}

package people.network.rest;

import com.sun.deploy.util.SessionState;
import com.vaadin.spring.annotation.SpringComponent;
import lombok.Data;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import people.network.entity.ResponseSearchCriteriaObj;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
@SpringComponent
@Data
@Service
public class JsonService {
    private String accessToken;
    private RestTemplate restTemplate = new RestTemplate();

    public JsonService() {    }

    public JsonService(String accessToken) {
        this.accessToken = accessToken;
    }

    public Collection<ResponseSearchCriteriaObj> getCriteriaList(String method, String q) {
        String uri = "https://api.vk.com/method/{method}?q={q}&v=5.8&access_token=" + accessToken;
        Map<String,String> values = new HashMap<>(2);
        values.put("method", method);
        values.put("q", q);
        ResponseEntity<Collection<ResponseSearchCriteriaObj>> response =
                restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Collection<ResponseSearchCriteriaObj>>() {
                }, values );
        return response.getBody();
    }
    public void show(){
        RestTemplate template = new RestTemplate();
        System.out.print(template.getForObject("https://api.vk.com/method/database.getCountries?q=u&v=5.8&access_token="+accessToken,
                String.class));
    }


}

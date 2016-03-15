package people.network.rest;


import com.vaadin.spring.annotation.SpringComponent;
import lombok.Data;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import people.network.entity.ResponseObject;
import people.network.entity.ResponseSearchCriteriaObj;

import java.net.URI;
import java.util.Arrays;
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

    public JsonService() {
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public JsonService(String accessToken) {
        this();
        this.accessToken = accessToken;
    }

    public Collection<ResponseSearchCriteriaObj> getCriteriaList(String method, String q) {
        String uri = "https://api.vk.com/method/{method}?q={q}&v=5.8&access_token=" + accessToken;
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.
        Map<String, String> values = new HashMap<>(2);
        values.put("method", method);
        values.put("q", q);
        //for IBIS
        System.setProperty("https.proxyHost","proxy.ibis");
        System.setProperty("https.proxyPort","3128");
        // marshaling the response from JSON to an array
        ResponseObject responseObject= restTemplate.getForObject(uri, ResponseObject.class, values);
        ResponseSearchCriteriaObj[] objects = responseObject.getResponse().getItems();
        return Arrays.asList(objects);
        UriComponents uriComponents =
                UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host("www.leveluplunch.com")
                        .path("/{lanuage}/{type}/")
                        .queryParam("test", "a", "b")
                        .build()
                        .expand("java", "examples")
                        .encode();

        assertEquals("http://www.leveluplunch.com/java/examples/?test=a&test=b",
                uriComponents.toUriString());
    }
    }


}

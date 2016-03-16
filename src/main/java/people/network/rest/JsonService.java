package people.network.rest;


import com.vaadin.spring.annotation.SpringComponent;
import lombok.Data;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import people.network.entity.ResponseObject;
import people.network.entity.ResponseSearchCriteriaObj;

import java.util.*;

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

    /**
     *
     * @param method
     * @param params 3 obligatory parameters will be added
     * @return
     */
    public Collection<ResponseSearchCriteriaObj> getCriteriaList(String method, MultiValueMap<String, String> params) {
        if (null == params) params = new LinkedMultiValueMap<>(3);
        params.add("v", "5.8");
        params.add("access_token", accessToken);
        params.add("count", "1000");
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https").host("api.vk.com").path("/method/{method}").queryParams(params);
        String url = uriBuilder.buildAndExpand(method).encode().toUriString();

        //for IBIS
        System.setProperty("https.proxyHost", "proxy.ibis");
        System.setProperty("https.proxyPort", "3128");
        // marshaling the response from JSON to an array
        ResponseObject responseObject = restTemplate.getForObject(url, ResponseObject.class);
        ResponseSearchCriteriaObj[] objects = responseObject.getResponse().getItems();
        return Arrays.asList(objects);


    }
}




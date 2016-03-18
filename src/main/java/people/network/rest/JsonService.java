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
import people.network.entity.RespSrchCrtriaObj;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

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
     * @param method
     * @param params 3 obligatory parameters will be added
     * @param count  amount of result entries you want to receive
     * @param from   number of from which you need to get the list
     * @return
     */
    public List<RespSrchCrtriaObj> getCriteriaList(String method, MultiValueMap<String, String> params,
                                                           int count, int from) {
        if (null == params) params = new LinkedMultiValueMap<>(3);
        params.add("v", "5.8");
        params.add("access_token", accessToken);
        params.add("count", String.valueOf(count));
        if (0 < from) params.add("offset", String.valueOf(from));
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https").host("api.vk.com").path("/method/{method}").queryParams(params);
        String url = uriBuilder.buildAndExpand(method).toUriString();
        System.out.println(url);

        // marshaling the response from JSON to an array
        ResponseObject responseObject = restTemplate.getForObject(url, ResponseObject.class);
        RespSrchCrtriaObj[] objects = responseObject.getResponse().getItems();
        return Arrays.asList(objects);


    }
}




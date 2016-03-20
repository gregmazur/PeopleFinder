package people.network.rest;


import com.vaadin.spring.annotation.SpringComponent;
import lombok.Data;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import people.network.entity.criteria.RespSrchCrtriaObj;
import people.network.entity.criteria.ResponseObjectCriteria;
import people.network.entity.user.ResponseObjectUsers;
import people.network.entity.user.UserDetails;

import java.util.Arrays;
import java.util.Collections;
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
        String url = buildURL(method,params,count,from);
        System.out.println(url);

        // marshaling the response from JSON to an
        ResponseObjectCriteria responseObject;
        try {
            responseObject = restTemplate.getForObject(url, ResponseObjectCriteria.class);
        } catch (ResourceAccessException e){
            Utils.showError();
            return Collections.emptyList();
        }
        RespSrchCrtriaObj[] objects = responseObject.getResponse().getItems();
        return Arrays.asList(objects);
    }

    public List<UserDetails> getUserList(String method, MultiValueMap<String, String> params,
                                                   int count, int from) {
        if (null == params) params = new LinkedMultiValueMap<>(3);
        String url = buildURL(method,params,count,from);
        System.out.println(url);

        // marshaling the response from JSON to an
        ResponseObjectUsers responseObject;
        try {
            responseObject = restTemplate.getForObject(url, ResponseObjectUsers.class);
        } catch (ResourceAccessException e){
            Utils.showError();
            return Collections.emptyList();
        }
        UserDetails[] objects = responseObject.getResponse().getItems();
        return Arrays.asList(objects);
    }

    private String buildURL(String method, MultiValueMap<String, String> params,
                            int count, int from){
        params.add("v", "5.8");
        params.add("access_token", accessToken);
        params.add("count", String.valueOf(count));
        if (0 < from) params.add("offset", String.valueOf(from));
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https").host("api.vk.com").path("/method/{method}").queryParams(params);
        return uriBuilder.buildAndExpand(method).toUriString();
    }
}




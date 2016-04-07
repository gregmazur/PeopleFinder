package people.network.service.rest;


import com.vaadin.spring.annotation.SpringComponent;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import people.network.entity.criteria.RespSrchCrtriaObj;
import people.network.entity.criteria.ResponseObjectCriteria;
import people.network.entity.user.ResponseObjectUsers;
import people.network.entity.user.Person;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
    @Value("${vk.app.id}")
    private String vkAppId;
    @Value("${host.name}")
    private String hostName;

    public JsonService() {
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    /**
     * @param method
     * @param params 5 obligatory parameters will be added
     * @param count  amount of result entries you want to receive
     * @param from   number of from which you need to get the list
     * @return
     */
    public List<RespSrchCrtriaObj> getCriteriaList(String method, MultiValueMap<String, String> params,
                                                   int count, int from) {
        if (null == params) return Collections.emptyList();
        String url = buildURL(method, params, count, from);
        System.out.println(url);

        // marshaling the response from JSON to an object
        ResponseObjectCriteria responseObject;
        try {
            responseObject = restTemplate.getForObject(url, ResponseObjectCriteria.class);
        } catch (ResourceAccessException e) {
            Utils.showError();
            return Collections.emptyList();
        }
        RespSrchCrtriaObj[] objects = responseObject.getResponse().getItems();
        return Arrays.asList(objects);
    }

    /**
     * @param method method name
     * @param params a map with params for the request
     * @param count number of results per one request
     * @param from offset
     * @return no more than 5000 results, will be called recursively inside
     */
    public List<Person> getUserList(String method, MultiValueMap<String, String> params,
                                    int count, int from) {
        // if we have more than 5000 people found no need to load all of them
        if (null == params || from > 5000) return Collections.emptyList();
        Utils.putParam(params, "fields", "photo_max_orig");
        Utils.putParam(params, "has_photo", "1");
        String url = buildURL(method, params, count, from);
        System.out.println(url);

        // marshaling the response from JSON to an object
        ResponseObjectUsers responseObject;
        List<Person> result = new ArrayList<>();
        try {
            responseObject = restTemplate.getForObject(url, ResponseObjectUsers.class);

            if (null == responseObject) return Collections.emptyList();
            Person[] objects = responseObject.getResponse().getItems();
            result.addAll(Arrays.asList(objects));
            if (0 != objects.length) {
                Thread.sleep(500);
                result.addAll(getUserList(method, params, count, from + objects.length));
            }
        } catch (ResourceAccessException e) {
            Utils.showError();
            return Collections.emptyList();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String buildURL(String method, MultiValueMap<String, String> params,
                            int count, int from) {
        Utils.putParam(params, "v", "5.8");
        Utils.putParam(params, "access_token", accessToken);
        Utils.putParam(params, "count", String.valueOf(count));
        if (0 < from) Utils.putParam(params, "offset", String.valueOf(from));
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https").host("api.vk.com").path("/method/{method}").queryParams(params);
        return uriBuilder.buildAndExpand(method).toUriString();
    }
}




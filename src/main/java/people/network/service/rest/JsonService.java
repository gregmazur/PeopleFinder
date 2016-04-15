package people.network.service.rest;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import people.network.entity.user.Person;
import people.network.entity.user.ResponseObjectUsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
@Data
@Service
public class JsonService implements ExternalRestService {
    private String accessToken;
    private ObjectMapper mapper = new ObjectMapper();
    private RestTemplate restTemplate = new RestTemplate();
    @Value("${vk.app.id}")
    private String vkAppId;
    @Value("${host.name}")
    private String hostName;

    public JsonService() {
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }


    @Override
    public List<RespSrchCrtriaObj> getCriteriaList(String method, MultiValueMap<String, String> params,
                                                   int count, int from) {
        if (null == params) return Collections.emptyList();
        String url = buildURL(method, params, count, from, false);
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

    @Override
    public List<Person> getUserList(String method, MultiValueMap<String, String> params,
                                    int count, int from) {
        // if we have more than 5000 people found no need to load all of them
        if (null == params || from > 5000) return Collections.emptyList();
        Utils.putParam(params, "fields", "photo_max_orig,occupation,universities");
        Utils.putParam(params, "has_photo", "1");
        String url = buildURL(method, params, count, from, true);
        System.out.println(url);

        // marshaling the response from JSON to an object
        ResponseObjectUsers responseObject;
        List<Person> result = new ArrayList<>();
//        try {
            responseObject = restTemplate.getForObject(url, ResponseObjectUsers.class);

            if (null == responseObject) return Collections.emptyList();
            Person[] objects = responseObject.getResponse().getItems();
            result.addAll(Arrays.asList(objects));
//            if (0 != objects.length) {
//                Thread.sleep(500);
//                result.addAll(getUserList(method, params, count, from + objects.length));
//            }

//        } catch (InterruptedException e) {
//            e.printStackTrace();

        return result;
    }

    @Override
    public List<Person> getUserList(String jsonText) throws IOException {
        mapper.readValue(jsonText, ResponseObjectUsers.class);
        return null;
    }

    @Override
    public String buildURLforPersonSearchRequest(String method, MultiValueMap<String, String> params, int count, int from) {
        Utils.putParam(params, "fields", "photo_max_orig,occupation,universities");
        Utils.putParam(params, "has_photo", "1");
        return buildURL(method, params, count, from, true);
    }


    private String buildURL(String method, MultiValueMap<String, String> params,
                            int count, int from, boolean withAT) {
        Utils.putParam(params, "v", "5.8");
        if (withAT)Utils.putParam(params, "access_token", accessToken);
        Utils.putParam(params, "count", String.valueOf(count));
        if (0 < from) Utils.putParam(params, "offset", String.valueOf(from));
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https").host("api.vk.com").path("/method/{method}").queryParams(params);
        return uriBuilder.buildAndExpand(method).toUriString();
    }
}




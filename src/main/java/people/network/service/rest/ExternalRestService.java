package people.network.service.rest;

import org.springframework.util.MultiValueMap;
import people.network.entity.criteria.RespSrchCrtriaObj;
import people.network.entity.user.Person;

import java.util.List;

/**
 * created by Greg
 */
public interface ExternalRestService {
    /**
     * @param method
     * @param params 5 obligatory parameters will be added
     * @param count  amount of result entries you want to receive
     * @param from   number of from which you need to get the list
     * @return
     */
    List<RespSrchCrtriaObj> getCriteriaList(String method, MultiValueMap<String, String> params,
                                            int count, int from);
    /**
     * @param method method name
     * @param params a map with params for the request
     * @param count number of results per one request
     * @param from offset
     * @return no more than 5000 results, will be called recursively inside
     */
    List<Person> getUserList(String method, MultiValueMap<String, String> params,
                             int count, int from);

    void setAccessToken(String token);

    String getVkAppId();

    String getHostName();
}

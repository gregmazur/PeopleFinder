package people.network.service;

import people.network.entity.SearchPerson;
import people.network.entity.user.UserDetails;

import java.util.Collection;

public interface ImageService {
    /**
     *
     * @param person target
     * @param potentialPeople Collection of users gathered by selection params
     * @param kSimilarity  coefficient of similarity of given faces with the target
     * @return
     */
    Collection<UserDetails> getSimmilarPeople(SearchPerson person, Collection<UserDetails> potentialPeople, float kSimilarity);
}

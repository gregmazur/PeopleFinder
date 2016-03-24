package people.network.service;

import people.network.entity.SearchPerson;
import people.network.entity.user.Person;

import java.util.Collection;

public interface ImageService {
    /**
     *
     * @param person search person
     * @param potentialPeople Collection of users gathered by selection params
     * @return sorted potential persons by similarity
     */
    Collection<Person> getSimilarPeople(SearchPerson person, Collection<Person> potentialPeople);
}

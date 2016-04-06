package people.network.service;

import people.network.entity.SearchPerson;
import people.network.entity.user.Person;

import java.util.Collection;
import java.util.List;

public interface ImageService {
    /**
     * @param person          search person
     * @param potentialPeople Collection of users gathered by selection params
     */
    void findSimilarPeople(SearchPerson person, Collection<Person> potentialPeople);

    void addProcessingListener(ProcessingListener listener);
}

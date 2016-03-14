package people.network.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 *
 *
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseSearchCriteriaObj {
    private String title;
    private int id;
}

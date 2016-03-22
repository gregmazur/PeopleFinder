package people.network.entity.criteria;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/

@Getter
@Setter
public class RespSrchCrtriaObj implements Serializable {

    private static final long serialVersionUID = -4848270644711351479L;

    private String title;
    private String name;
    private long id;

    @Override
    public String toString() {
        return null == title ? name : title;
    }
}

package people.network.entity;

import lombok.Getter;
import lombok.Setter;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;

import java.io.File;

/**
 *
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
@Getter
@Setter
public class PersonTest {

    private long id;

    private String fName;
    private String lName;

    private File imgFile;
    private MBFImage mbfImage;
    private FImage fImage;

    public PersonTest() { }

    public PersonTest(File imgFile) {
        this.imgFile = imgFile;
    }


    @Override
    public String toString() {
        return id + ":" + fName;
    }
}

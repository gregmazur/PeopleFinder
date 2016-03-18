package people.network.entity;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;

import java.io.File;

/**
 *
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class Person {

    private long id;

    private String fName;
    private String lName;

    private File imgFile;
    private MBFImage mbfImage;
    private FImage fImage;

    public Person() { }

    public Person(File imgFile) {
        this.imgFile = imgFile;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFName() {
        return fName;
    }

    public void setFName(String fName) {
        this.fName = fName;
    }

    public String getLName() {
        return lName;
    }

    public void setLName(String lName) {
        this.lName = lName;
    }

    public File getImageFile() {
        return imgFile;
    }

    public void setImageFile(File imgFile) {
        this.imgFile = imgFile;
    }

    public MBFImage getMBFImage() {
        return mbfImage;
    }

    public void setMBFImage(MBFImage mbfImage) {
        this.mbfImage = mbfImage;
    }

    public FImage getFImage() {
        return fImage;
    }

    public void setFImage(FImage fImage) {
        this.fImage = fImage;
    }

    @Override
    public String toString() {
        return id + ":" + fName;
    }
}

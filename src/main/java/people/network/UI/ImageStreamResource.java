package people.network.UI;

import com.vaadin.server.StreamResource;

import java.io.InputStream;

/**
 *
 *
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/
public class ImageStreamResource implements StreamResource.StreamSource {

    private InputStream stream;

    public ImageStreamResource(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public InputStream getStream() {
        return stream;
    }
}

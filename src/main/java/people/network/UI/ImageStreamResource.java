package people.network.UI;

import com.vaadin.server.StreamResource;

import java.io.InputStream;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class ImageStreamResource extends StreamResource {


    public ImageStreamResource(InputStream stream, String name) {
        super(new ImageStreamSource(stream), name);
    }

    private static class ImageStreamSource implements StreamSource {
        private InputStream stream;

        public ImageStreamSource(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public InputStream getStream() {
            return stream;
        }
    }
}

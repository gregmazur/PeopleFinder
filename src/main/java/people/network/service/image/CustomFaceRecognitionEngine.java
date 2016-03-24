package people.network.service.image;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DatasetFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class CustomFaceRecognitionEngine<FACE extends DetectedFace, PERSON> implements ReadWriteableBinary {

    private static final Logger logger = Logger.getLogger(CustomFaceRecognitionEngine.class);

    protected FaceDetector<FACE, FImage> detector;
    protected FaceRecogniser<FACE, PERSON> recogniser;


    /**
     * Construct a {@link CustomFaceRecognitionEngine} with the given face detector
     * and recogniser.
     *
     * @param detector   the face detector
     * @param recogniser the face recogniser
     */
    private CustomFaceRecognitionEngine(final FaceDetector<FACE, FImage> detector,
                                        final FaceRecogniser<FACE, PERSON> recogniser) {
        this.detector = detector;
        this.recogniser = recogniser;
    }

    /**
     * Create a {@link CustomFaceRecognitionEngine} with the given face detector and
     * recogniser.
     *
     * @param <FACE>      Type of {@link DetectedFace}
     * @param <EXTRACTOR> Type of {@link FeatureExtractor}
     * @param <PERSON>    Type representing a person
     * @param detector    the face detector
     * @param recogniser  the face recogniser
     *
     * @return new {@link CustomFaceRecognitionEngine}
     */
    public static <FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON> CustomFaceRecognitionEngine<FACE, PERSON> create(
            final FaceDetector<FACE, FImage> detector, final FaceRecogniser<FACE, PERSON> recogniser) {
        return new CustomFaceRecognitionEngine<FACE, PERSON>(detector, recogniser);
    }

    /**
     * @return the detector
     */
    public FaceDetector<FACE, FImage> getDetector() {
        return this.detector;
    }

    /**
     * @return the recogniser
     */
    public FaceRecogniser<FACE, PERSON> getRecogniser() {
        return this.recogniser;
    }

    /**
     * Save the {@link CustomFaceRecognitionEngine} to a file, including all the
     * internal state of the recogniser, etc.
     *
     * @param file the file to save to
     *
     * @throws IOException if an error occurs when writing
     */
    public void save(final File file) throws IOException {
        IOUtils.writeBinaryFull(file, this);
    }

    /**
     * Load a {@link CustomFaceRecognitionEngine} previously saved by
     * {@link #save(File)}.
     *
     * @param <O>  Type of {@link DetectedFace}
     * @param <P>  Type representing a person
     * @param file the file to read from
     *
     * @return the created recognition engine
     *
     * @throws IOException if an error occurs during the read
     */
    public static <O extends DetectedFace, P> CustomFaceRecognitionEngine<O, P> load(
            final File file) throws IOException {
        final CustomFaceRecognitionEngine<O, P> engine = IOUtils.read(file);

        return engine;
    }

    /**
     * Train with a dataset
     *
     * @param dataset the dataset
     */
    public void train(final GroupedDataset<PERSON, ListDataset<FImage>, FImage> dataset) {
        final GroupedDataset<PERSON, ListDataset<FACE>, FACE> faceDataset = DatasetFaceDetector.process(dataset, this.detector);
        this.recogniser.train(faceDataset);
    }

    /**
     * Train the recogniser with a single example, returning the detected face.
     * If multiple faces are found, the biggest is chosen.
     * <p>
     * If you need more control, consider calling {@link #getDetector()} to get
     * a detector which you can apply to your image and {@link #getRecogniser()}
     * to get the recogniser which you can train with the detections directly.
     *
     * @param person the person
     * @param image  the image with the persons face
     *
     * @return the detected face
     */
    public FACE train(final PERSON person, final FImage image) {
        final List<FACE> faces = this.detector.detectFaces(image);

        if(faces == null || faces.size() == 0) {
            CustomFaceRecognitionEngine.logger.warn("no face detected");
            return null;
        } else if(faces.size() == 1) {
            this.recogniser.train(AnnotatedObject.create(faces.get(0), person));
            return faces.get(0);
        } else {
            CustomFaceRecognitionEngine.logger.warn("More than one face found. Choosing biggest.");

            final FACE face = DatasetFaceDetector.getBiggest(faces);
            this.recogniser.train(AnnotatedObject.create(face, person));
            return face;
        }
    }

    /**
     * Train for the given face patch without doing any face detection. It is
     * assumed that the given image will be a cropped/aligned image of the face
     * as is necessary for the given recogniser.
     *
     * @param face   The detected face implementation
     * @param person The person to whom this face belongs
     *
     * @return The face image
     */
    public FACE train(final FACE face, final PERSON person) {
        this.recogniser.train(AnnotatedObject.create(face, person));
        return face;
    }

    /**
     * Detect and recognise the faces in the given image, returning a list of
     * potential people for each face.
     *
     * @param image the image
     *
     * @return a list of faces and recognitions
     */
    public List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> recognise(final FImage image) {
        final List<FACE> detectedFaces = this.detector.detectFaces(image);
        final List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>>();

        for(final FACE df : detectedFaces) {
            results.add(new IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>(df, this.recogniser.annotate(df)));
        }

        return results;
    }

    /**
     * Detect and recognise the faces in the given image, returning the most
     * likely person for each face.
     *
     * @param image the image
     *
     * @return a list of faces with the most likely person
     */
    public List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> recogniseBest(final FImage image) {
        final List<FACE> detectedFaces = this.detector.detectFaces(image);
        final List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> results = new ArrayList<IndependentPair<FACE, ScoredAnnotation<PERSON>>>();

        for(final FACE df : detectedFaces) {
            results.add(new IndependentPair<FACE, ScoredAnnotation<PERSON>>(df, this.recogniser.annotateBest(df)));
        }

        return results;
    }

    /**
     * Detect and recognise the faces in the given image, returning a list of
     * potential people for each face. The recognised people will be restricted
     * to the given set.
     *
     * @param image    the image
     * @param restrict set of people to restrict to
     *
     * @return a list of faces and recognitions
     */
    public List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> recognise(final FImage image,
                                                                                 final Set<PERSON> restrict) {
        final List<FACE> detectedFaces = this.detector.detectFaces(image);
        final List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>>();

        for(final FACE df : detectedFaces) {
            results.add(new IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>(df, this.recogniser.annotate(df, restrict)));
        }

        return results;
    }

    /**
     * Detect and recognise the faces in the given image, returning the most
     * likely person for each face. The recognised people will be restricted to
     * the given set.
     *
     * @param image    the image
     * @param restrict set of people to restrict to
     *
     * @return a list of faces with the most likely person
     */
    public List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> recogniseBest(final FImage image,
                                                                               final Set<PERSON> restrict) {
        final List<FACE> detectedFaces = this.detector.detectFaces(image);
        final List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> results = new ArrayList<IndependentPair<FACE, ScoredAnnotation<PERSON>>>();

        for(final FACE df : detectedFaces) {
            results.add(new IndependentPair<FACE, ScoredAnnotation<PERSON>>(df, this.recogniser.annotateBest(df, restrict)));
        }

        return results;
    }

    @Override
    public void readBinary(final DataInput in) throws IOException {
        final String detectorClass = in.readUTF();
        this.detector = IOUtils.newInstance(detectorClass);
        this.detector.readBinary(in);

        final String recogniserClass = in.readUTF();
        this.recogniser = IOUtils.newInstance(recogniserClass);
        this.recogniser.readBinary(in);
    }

    @Override
    public byte[] binaryHeader() {
        return "FaRE".getBytes();
    }

    @Override
    public void writeBinary(final DataOutput out) throws IOException {
        out.writeUTF(this.detector.getClass().getName());
        this.detector.writeBinary(out);

        out.writeUTF(this.recogniser.getClass().getName());
        this.recogniser.writeBinary(out);
    }
}


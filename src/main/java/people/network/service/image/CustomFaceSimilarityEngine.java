package people.network.service.image;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DatasetFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.feature.FacialFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import people.network.entity.SearchPerson;
import people.network.entity.user.Person;
import people.network.service.ProcessingEvent;
import people.network.service.ProcessingListener;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Face similarity engine
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class CustomFaceSimilarityEngine<D extends DetectedFace, F extends FacialFeature> {

    private List<ProcessingListener> listeners;

    private FacialFeatureExtractor<F, D> extractor;
    private FacialFeatureComparator<F> comparator;

    private ThreadLocal<FaceDetector<D, FImage>> detector;
    private LoadingCache<Person, List<D>> detectedFaceCache;

    private ExecutorService executor;

    private D searchFace;

    private volatile boolean cache;

    private volatile float faceConfidence = 10.0f;

    /**
     * Construct a new {@link CustomFaceSimilarityEngine} from the
     * specified detector, extractor and comparator.
     *
     * @param detectorSupplier The face detector supplier
     * @param extractor The feature extractor
     * @param comparator The feature comparator
     */
    private CustomFaceSimilarityEngine(Supplier<FaceDetector<D, FImage>> detectorSupplier,
                                      FacialFeatureExtractor<F, D> extractor,
                                      FacialFeatureComparator<F> comparator) {
        this.detector = ThreadLocal.withInitial(detectorSupplier);
        this.extractor = extractor;
        this.comparator = comparator;

        detectedFaceCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).
                build(new CacheLoader<Person, List<D>>() {
                    @Override
                    public List<D> load(Person key) {
                        FImage fImage = key.getFImage();
                        if(fImage == null) return null;
                        return getDetectedFaces(fImage);
                    }
                });

        int n_thds = (int) (1.5f * Runtime.getRuntime().availableProcessors());
        executor = Executors.newFixedThreadPool(n_thds);
    }

    /**
     * Create a new {@link CustomFaceSimilarityEngine} from the
     * specified detector, extractor and comparator.
     *
     * @param <D> The type of {@link DetectedFace}
     * @param <F> the type of {@link FacialFeature}
     *
     * @param detectorSupplier The face detector supplier
     * @param extractor The feature extractor
     * @param comparator The feature comparator
     * @return the new {@link CustomFaceSimilarityEngine}
     */
    public static <D extends DetectedFace, F extends FacialFeature> CustomFaceSimilarityEngine<D, F> create(
            Supplier<FaceDetector<D, FImage>> detectorSupplier, FacialFeatureExtractor<F, D> extractor, FacialFeatureComparator<F> comparator) {
        return new CustomFaceSimilarityEngine<>(detectorSupplier, extractor, comparator);
    }

    /**
     * Set the search person
     *
     * @param searchPerson search person
     */
    public boolean setSearchPerson(SearchPerson searchPerson) {
        System.out.println("Starting search face detection...");
        List<FImage> fImages = searchPerson.getFImages();
        if(fImages.isEmpty()) {
            System.out.println("Search person images not set!");
            return false;
        }
        List<D> faces = fImages.stream().flatMap(fImage -> getDetectedFaces(fImage).stream()).collect(Collectors.toList());
        if(faces.isEmpty()) {
            System.out.println("Search face not detected!");
            return false;
        }
        this.searchFace = DatasetFaceDetector.getBiggest(faces);
        //DisplayUtilities.display("TestImage", searchFace.getFacePatch());
        System.out.println("Search face detected");
        return true;
    }

    private List<D> getDetectedFacesCache(Person person) {
        return this.cache ? detectedFaceCache.getUnchecked(person) : getDetectedFaces(person.getFImage());
    }

    private List<D> getDetectedFaces(FImage image) {
        if(image == null)
            return Collections.emptyList();
        FaceDetector<D, FImage> detector = this.detector.get();
        List<D> facesList = detector.detectFaces(image);
        return facesList.stream().filter(this::isFaceConfident).collect(Collectors.toList());
    }

    private boolean isFaceConfident(D face) {
        return face.getConfidence() > faceConfidence;
    }

    /**
     * Compute the similarities between faces of search person and potential person
     */
    public void calculateSimilarities(Collection<Person> personList) {

        System.out.println("Starting calculate similarities...");
        AtomicInteger aInt = new AtomicInteger(0);
        int tasksSize = personList.size();
        F searchFaceFeature = getFaceFeature(searchFace);
        List<Future<Person>> fList = new ArrayList<>(tasksSize);

        for(Person person : personList) {
            Future<Person> f = executor.submit(() -> {
                System.out.println(String.format("Processing person %d of %d. PicURL=%s",
                        aInt.incrementAndGet(), tasksSize, person.getPicURL()));
                List<D> faces = getDetectedFacesCache(person);
                faces.stream().map(this::getFaceFeature).forEach(faceFeature -> {
                    double d = comparator.compare(faceFeature, searchFaceFeature);
                    if(comparator.isDistance()) {
                        if(person.getSimilarity() < d) person.setSimilarity(d);
                    } else {
                        if(person.getSimilarity() > d) person.setSimilarity(d);
                    }
                });
                return person;
            });
            fList.add(f);
        }

        List<Person> pList = new ArrayList<>(32);
        for(Future<Person> f : fList) {
            try {
                Person p = f.get(30, TimeUnit.SECONDS);
                pList.add(p);
                if(pList.size() >= 15)
                    fireEvent(pList);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        if(!pList.isEmpty())
            fireEvent(pList);

        System.out.println("All similarities calculated.");
    }

    private void fireEvent(List<Person> pList) {
        ProcessingEvent event = new ProcessingEvent(this, pList);
        for(ProcessingListener l : listeners) {
            l.eventHappened(event);
        }
    }

    private F getFaceFeature(D face) {
        return extractor.extractFeature(face);
    }

    public FaceDetector<D, FImage> detector() {
        return detector.get();
    }

    public FacialFeatureExtractor<F, D> extractor() {
        return extractor;
    }

    public FacialFeatureComparator<F> comparator() {
        return comparator;
    }

    public void setUseCache(boolean cache) {
        this.cache = cache;
    }

    public boolean getUseCache() {
        return this.cache;
    }

    public float getFaceConfidence() {
        return this.faceConfidence;
    }

    public void setFaceConfidence(float faceConfidence) {
        this.faceConfidence = faceConfidence;
    }

    public void resetEngine() {
        detectedFaceCache.invalidateAll();
        searchFace = null;
        detector = new ThreadLocal<>();
    }

    public void addProcessingListener(ProcessingListener listener) {
        listeners.add(listener);
    }
}

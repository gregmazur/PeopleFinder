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

    private FacialFeatureExtractor<F, D> extractor;
    private FacialFeatureComparator<F> comparator;

    private ThreadLocal<FaceDetector<D, FImage>> detector;
    private LoadingCache<Person, List<D>> detectedFaceCache;

    private ExecutorService executor;

    private D searchFace;
    private Multimap<Person, D> potentialFaces;

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

        potentialFaces = ArrayListMultimap.create(500, 3);

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

    /**
     * Set the potential persons
     *
     * @param personList potential person list
     */
    public void setPotentialPersons(Collection<Person> personList) {
        System.out.println("Starting potential face detection...");
        AtomicInteger aInt = new AtomicInteger(0);
        int tasksSize = personList.size();
        CountDownLatch latch = new CountDownLatch(tasksSize);
        for(Person person : personList) {
            executor.execute(() -> {
                try {
                    System.out.println(String.format("Processing person %d of %d. PicURL=%s",
                            aInt.incrementAndGet(), tasksSize, person.getPicURL()));
                    List<D> faces = getDetectedFacesCache(person);
                    if(!faces.isEmpty()) potentialFaces.putAll(person, faces);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("All potential faces detected");
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
    public List<Person> calculateSimilarities() {
        F searchFaceFeature = getFaceFeature(searchFace);
        Map<Person, Collection<D>> map = potentialFaces.asMap();
        List<Person> resultList = new ArrayList<>();
        for(Entry<Person, Collection<D>> entry : map.entrySet()) {
            Person person = entry.getKey();
            resultList.add(person);
            Collection<D> faces = entry.getValue();
            faces.stream().map(this::getFaceFeature).forEach(faceFeature -> {
                double d = comparator.compare(faceFeature, searchFaceFeature);
                if(comparator.isDistance()) {
                    if(person.getSimilarity() < d) person.setSimilarity(d);
                } else {
                    if(person.getSimilarity() > d) person.setSimilarity(d);
                }
            });
        }
        Collections.sort(resultList, Person::compareBySimilarity);
        return resultList;
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
        potentialFaces.clear();
        searchFace = null;
        detector = new ThreadLocal<>();
    }
}

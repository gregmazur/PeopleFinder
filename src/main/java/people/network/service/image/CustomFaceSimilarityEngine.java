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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Face similarity engine
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class CustomFaceSimilarityEngine<D extends DetectedFace, F extends FacialFeature> {

    private FaceDetector<D, FImage> detector;
    private FacialFeatureExtractor<F, D> extractor;
    private FacialFeatureComparator<F> comparator;

    private LoadingCache<Person, List<D>> detectedFaceCache;
    //private LoadingCache<Person, F> featureCache;

    private D searchFace;
    private Multimap<Person, D> potentialFaces;

    private boolean cache;

    private float faceConfidence = 10.0f;

    /**
     * Construct a new {@link CustomFaceSimilarityEngine} from the
     * specified detector, extractor and comparator.
     *
     * @param detector The face detector
     * @param extractor The feature extractor
     * @param comparator The feature comparator
     */
    private CustomFaceSimilarityEngine(FaceDetector<D, FImage> detector,
                                      FacialFeatureExtractor<F, D> extractor,
                                      FacialFeatureComparator<F> comparator) {
        this.detector = detector;
        this.extractor = extractor;
        this.comparator = comparator;

        potentialFaces = ArrayListMultimap.create(100, 3);

        detectedFaceCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).
                build(new CacheLoader<Person, List<D>>() {
                    @Override
                    public List<D> load(Person key) {
                        FImage fImage = key.getFImagePic();
                        if(fImage == null) return null;
                        return getDetectedFaces(fImage);
                    }
                });

        /*featureCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).
                build(new CacheLoader<Person, F>() {
                    @Override
                    public F load(Person key) {
                        return getFaceFeature(face);
                    }
                });*/
    }

    /**
     * Create a new {@link CustomFaceSimilarityEngine} from the
     * specified detector, extractor and comparator.
     *
     * @param <D> The type of {@link DetectedFace}
     * @param <F> the type of {@link FacialFeature}
     *
     * @param detector The face detector
     * @param extractor The feature extractor
     * @param comparator The feature comparator
     * @return the new {@link CustomFaceSimilarityEngine}
     */
    public static <D extends DetectedFace, F extends FacialFeature> CustomFaceSimilarityEngine<D, F> create(
            FaceDetector<D, FImage> detector, FacialFeatureExtractor<F, D> extractor, FacialFeatureComparator<F> comparator) {
        return new CustomFaceSimilarityEngine<>(detector, extractor, comparator);
    }

    /**
     * Set the search person
     *
     * @param searchPerson search person
     */
    public void setSearchPerson(SearchPerson searchPerson) {
        List<FImage> fImages = searchPerson.getFImages();
        List<D> faces = new ArrayList<>();
        for(FImage fImage : fImages) {
            faces.addAll(getDetectedFaces(fImage));
        }
        this.searchFace = DatasetFaceDetector.getBiggest(faces);
    }

    /**
     * Set the potential persons
     *
     * @param personList potential person list
     */
    public void setPotentialPersons(Collection<Person> personList) {
        System.out.println("Starting face detection");
        int i = 1;
        for(Person person : personList) {
            System.out.println(String.format("Proc person %d of %d.", i++, personList.size()));
            List<D> faces = getDetectedFacesCache(person);
            if(!faces.isEmpty())
                potentialFaces.putAll(person, faces);
        }
        System.out.println("All faces detected");
    }

    private List<D> getDetectedFacesCache(Person person) {
        List<D> toRet;
        if(this.cache) {
            return detectedFaceCache.getUnchecked(person);
        } else {
            toRet = getDetectedFaces(person.getFImagePic());
        }
        return toRet;
    }

    private List<D> getDetectedFaces(FImage image) {
        if(image == null)
            return Collections.emptyList();
        List<D> facesList = this.detector.detectFaces(image);
        List<D> newFacesList = new ArrayList<>(facesList.size());
        for(D face : facesList) {
            if(face.getConfidence() > faceConfidence) {
                //DisplayUtilities.display("FaceConfidence=" + face.getConfidence(), face.getFacePatch());
                newFacesList.add(face);
            }
        }
        return newFacesList;
    }

    /**
     * Compute the similarities between faces of search person and potential person
     */
    public List<Person> calculateSimilarities() {
        F searchFaceFeature = getFaceFeature(searchFace);
        Map<Person, Collection<D>> map = potentialFaces.asMap();
        List<Person> resultList = new ArrayList<>();
        int i = 1;
        for(Entry<Person, Collection<D>> entry : map.entrySet()) {
            System.out.println(String.format("Proc person %d of %d.", i++, map.size()));
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

    /*private F getFaceFeatureCache(Person person, D face) {
        F toRet;
        if (!cache) {
            toRet = getFaceFeature(face);
        } else {
            toRet = this.featureCache.getUnchecked(person);//todo

            if(toRet == null){
                toRet = getFaceFeature(face);
                this.featureCache.put(person, toRet);
            }
        }
        return toRet;
    }*/

    private F getFaceFeature(D face) {
        return extractor.extractFeature(face);
    }

    public FaceDetector<D, FImage> detector() {
        return detector;
    }

    public FacialFeatureExtractor<F, D> extractor() {
        return extractor;
    }

    public FacialFeatureComparator<F> comparator() {
        return comparator;
    }

    /**
     * Set whether detections should be cached
     *
     * @param cache enable cache if true
     */
    public void setUseCache(boolean cache) {
        this.cache = cache;
    }

    /**
     * Get whether detections use cache
     *
     * @return whether detections use cache
     */
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
        //featureCache.invalidateAll();
        detectedFaceCache.invalidateAll();
        potentialFaces.clear();
        searchFace = null;
    }
}

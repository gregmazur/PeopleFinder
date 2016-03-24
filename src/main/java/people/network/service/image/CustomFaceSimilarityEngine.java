package people.network.service.image;

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
import people.network.entity.user.UserDetails;

import java.util.*;
import java.util.Map.Entry;

/**
 * Face similarity engine
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class CustomFaceSimilarityEngine<D extends DetectedFace, F extends FacialFeature> {

    private FaceDetector<D, FImage> detector;
    private FacialFeatureExtractor<F, D> extractor;
    private FacialFeatureComparator<F> comparator;

    private Map<Long, F> featureCache;
    private Map<Long, List<D>> detectedFaceCache;

    private D searchFace;
    private Multimap<Long, D> potentialFaces;

    private Map<Long, UserDetails> personMap;

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

        featureCache = new HashMap<>(256);
        detectedFaceCache = new HashMap<>(256);
        potentialFaces = ArrayListMultimap.create();
        personMap = new HashMap<>(256);
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
    public void setPotentialPersons(Collection<UserDetails> personList) {
        for(UserDetails person : personList) {
            boolean isLoaded = person.loadImage();
            if(!isLoaded)
                continue;
            Long personId = person.getId();
            FImage personFImage = person.getFImage();
            List<D> faces = getDetectedFaces(personId, personFImage);
            potentialFaces.putAll(personId, faces);
            personMap.put(personId, person);
        }
    }

    private List<D> getDetectedFaces(Long personId, FImage personImage) {
        List<D> toRet;
        if(this.cache) {
            toRet = this.detectedFaceCache.get(personId);
            if(toRet == null) {
                toRet = getDetectedFaces(personImage);
                this.detectedFaceCache.put(personId, toRet);
            }
        } else {
            toRet = getDetectedFaces(personImage);
        }
        return toRet;
    }

    private List<D> getDetectedFaces(FImage image) {
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
    public void calculateSimilarities() {
        Collection<Entry<Long, D>> entries = potentialFaces.entries();
        F searchFaceFeature = getFaceFeature(searchFace);

        for(Entry<Long, D> entry : entries) {
            Long personId = entry.getKey();
            D face = entry.getValue();
            F potentialFaceFeature = getFaceFeature(personId, face);
            double d = comparator.compare(potentialFaceFeature, searchFaceFeature);

            UserDetails p = personMap.get(personId);
            if(p == null) continue;

            if(comparator.isDistance()) {
                if(p.getSimilarity() < d) p.setSimilarity(d);
            } else {
                if(p.getSimilarity() > d) p.setSimilarity(d);
            }

        }
    }

    private F getFaceFeature(Long id, D face) {
        F toRet;
        if (!cache) {
            toRet = getFaceFeature(face);
        } else {
            //String combinedID = String.format("%s:%b", id);
            toRet = this.featureCache.get(id);

            if(toRet == null){
                toRet = getFaceFeature(face);
                this.featureCache.put(id, toRet);
            }
        }
        return toRet;
    }

    private F getFaceFeature(D face) {
        return extractor.extractFeature(face);
    }


    public Map<Long, UserDetails> getPersonMap() {
        return this.personMap;
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
        featureCache.clear();
        detectedFaceCache.clear();
        potentialFaces.clear();
        searchFace = null;
        personMap.clear();
    }
}

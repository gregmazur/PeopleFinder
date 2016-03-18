package people.network.image;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.feature.FacialFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.math.matrix.similarity.processor.InvertData;
import people.network.entity.Person;

import java.util.*;

/**
 * Face similarity engine
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class CustomFaceSimilarityEngine<D extends DetectedFace, F extends FacialFeature> {

    private FaceDetector<D, FImage> detector;
    private FacialFeatureExtractor<F, D> extractor;
    private FacialFeatureComparator<F> comparator;

    private Map<String, Rectangle> boundingBoxes;
    private Map<String, F> featureCache;
    private Map<String, List<D>> detectedFaceCache;
    private LinkedHashMap<String, Map<String, Double>> similarityMatrix;

    private String potentialId;
    private List<D> potentialFaces;

    private String personId;
    private List<D> personFaces;

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

        this.similarityMatrix = new LinkedHashMap<>(256);
        this.boundingBoxes = new HashMap<>(256);
        featureCache = new HashMap<>(256);
        detectedFaceCache = new HashMap<>(256);
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
    public void setSearchPerson(Person searchPerson) {
        this.personId = searchPerson.toString();
        this.personFaces = getDetectedFaces(personId, searchPerson.getFImage());
        updateBoundingBox(this.personFaces, personId);
    }

    /**
     * Set the potential person.
     * After calling this method should be called {@link #findSimilarities()}()
     *
     * @param potentialPerson potential person
     */
    public void setPotentialPerson(Person potentialPerson) {
        this.potentialId = potentialPerson.toString();
        this.potentialFaces = getDetectedFaces(potentialId, potentialPerson.getFImage());
        updateBoundingBox(this.potentialFaces, potentialId);
    }

    /**
     * Set the search person
     *
     * @param personId    search person id
     * @param personImage search person's image
     */
    public void setSearchPerson(String personId, FImage personImage) {
        this.personId = personId;
        this.personFaces = getDetectedFaces(personId, personImage);
        updateBoundingBox(this.personFaces, personId);
    }

    /**
     * Set the potential person.
     * After calling this method should be called {@link #findSimilarities()}()
     *
     * @param personId    potential person id
     * @param personImage potential person's image
     */
    public void setPotentialPerson(String personId, FImage personImage) {
        this.potentialId = personId;
        this.potentialFaces = getDetectedFaces(potentialId, personImage);
        updateBoundingBox(this.potentialFaces, potentialId);
    }

    public void setPotentialPersons(List<Person> personList) {
        int size = personList.size();
        for(Person person : personList) {
            System.out.println(String.format("Processing person %d of %d", person.getId(), size));
            String personId = person.toString();
            FImage fImage = person.getFImage();
            setPotentialPerson(personId, fImage);
            findSimilarities();
        }
    }

    private List<D> getDetectedFaces(String personId, FImage personImage) {
        List<D> toRet;
        if(this.cache) {
            toRet = this.detectedFaceCache.get(personId);
            if(toRet == null) {
                toRet = getDetectedFacesWithConfidence(personImage);
                this.detectedFaceCache.put(personId, toRet);
            }
        } else {
            toRet = getDetectedFacesWithConfidence(personImage);
        }
        return toRet;
    }

    private List<D> getDetectedFacesWithConfidence(FImage image) {
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

    private void updateBoundingBox(List<D> faces, String imageId) {
        // We need to store the first one if we're running withFirst = true
        if (boundingBoxes != null)
            for (int ff = 0; ff < faces.size(); ff++)
                if (boundingBoxes.get(imageId + ":" + ff) == null)
                    boundingBoxes.put(imageId + ":" + ff, faces.get(ff)
                            .getBounds());
    }


    /**
     * Compute the similarities between faces of search person and potential person
     */
    public void findSimilarities() {
        // Now compare all the faces in the first image
        // with all the faces in the second image.
        for (int ii = 0; ii < potentialFaces.size(); ii++) {
            String face1id = potentialId + ":" + ii;
            D f1f = potentialFaces.get(ii);

            F f1fv = getFeature(face1id, f1f);
            //
            // NOTE that the distance matrix will be symmetrical
            // so we only have to do half the comparisons.
            for (int jj = 0; jj < personFaces.size(); jj++) {
                double d = 0;
                String face2id = null;

                // If we're comparing the same face in the same image
                // we can assume the distance is zero. Saves doing a match.
                if (potentialFaces == personFaces && ii == jj) {
                    d = 0;
                    face2id = face1id;
                } else {
                    // Compare the two feature vectors using the chosen
                    // distance metric.
                    D f2f = personFaces.get(jj);
                    face2id = personId + ":" + jj;

                    // F f2fv = featureFactory.createFeature(f2f, false);
                    F f2fv = getFeature(face2id, f2f);

                    d = comparator.compare(f1fv, f2fv);
                }

                // Put the result in the result map
                Map<String, Double> mm = this.similarityMatrix.get(face1id);
                if (mm == null)
                    this.similarityMatrix.put(face1id, mm = new HashMap<>());
                mm.put(face2id, d);
            }
        }
    }

    private F getFeature(String id, D face) {
        F toRet;
        if (!cache) {
            toRet = extractor.extractFeature(face);
        } else {
            //String combinedID = String.format("%s:%b", id);
            toRet = this.featureCache.get(id);

            if(toRet == null){
                toRet = extractor.extractFeature(face);
                this.featureCache.put(id, toRet);
            }
        }
        return toRet;
    }

    /**
     * @return The similarity dictionary structured as: {image0:face0 => {image0:face0 => DISTANCE,...},...,}
     */
    public Map<String, Map<String, Double>> getSimilarityDictionary() {
        return this.similarityMatrix;
    }

    /**
     * Get the similarity matrix computed by {@link #findSimilarities()}.
     * @param invertIfRequired invert distances into similarities if required.
     * @return the similarity matrix
     */
    public SimilarityMatrix getSimilarityMatrix(boolean invertIfRequired) {
        Set<String> keys = this.similarityMatrix.keySet();
        String[] indexArr = keys.toArray(new String[keys.size()]);
        SimilarityMatrix simMatrix = new SimilarityMatrix(indexArr);
        for (int i = 0; i < indexArr.length; i++) {
            String x = indexArr[i];
            for (int j = 0; j < indexArr.length; j++) {
                String y = indexArr[j];
                simMatrix.set(i, j, this.similarityMatrix.get(x).get(y));
            }
        }

        if(this.comparator.isDistance() && invertIfRequired) {
            simMatrix.processInplace(new InvertData());
        }
        return simMatrix;
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
     * @return the bounding boxes of the detected faces
     */
    public Map<String, Rectangle> getBoundingBoxes() {
        return this.boundingBoxes;
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
        boundingBoxes.clear();
        featureCache.clear();
        detectedFaceCache.clear();
        similarityMatrix.clear();
        potentialId = null;
        potentialFaces.clear();
        personId = null;
        personFaces.clear();
    }
}

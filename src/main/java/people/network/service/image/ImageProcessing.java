package people.network.service.image;

import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.FaceImageFeature;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import people.network.entity.SearchPerson;
import people.network.entity.user.UserDetails;

import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class ImageProcessing {

    private CustomFaceSimilarityEngine<KEDetectedFace, FacePatchFeature> _engine;

    private ImageProcessing() {
        FKEFaceDetector faceDetector = new FKEFaceDetector(HaarCascadeDetector.BuiltInCascade.frontalface_alt.load());
        FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FaceImageFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FacePatchFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new LocalLBPHistogram.Extractor(); // ScalingAligner AffineAligner RotateScaleAligner
        FacialFeatureComparator<FacePatchFeature> featureComparator = new FaceFVComparator<>(FloatFVComparison.EUCLIDEAN);

        _engine = CustomFaceSimilarityEngine.create(faceDetector, featureExtractor, featureComparator);
        //_faceEngine.setFaceConfidence(25.0f);
        //_faceSimilarity.setUseCache(true);
    }

    public static ImageProcessing createInstance() {
        return new ImageProcessing();
    }

    public List<UserDetails> getResult(SearchPerson searchPerson, List<UserDetails> potentialPersons) {
        if(searchPerson.getImages().isEmpty())
            return Collections.emptyList();

        _engine.setSearchPerson(searchPerson);
        _engine.setPotentialPersons(potentialPersons);
        _engine.calculateSimilarities();

        Collections.sort(potentialPersons, UserDetails::compareBySimilarity);

        return potentialPersons;
    }
}

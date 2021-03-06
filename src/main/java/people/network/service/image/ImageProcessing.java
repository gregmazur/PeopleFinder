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
import people.network.entity.user.Person;
import people.network.service.ImageService;
import people.network.service.ProcessingListener;
import people.network.service.utils.MemoryUtils;

import java.util.Collection;

/**
 * Обработка изображений - для одной сессии
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class ImageProcessing implements ImageService {

    private CustomFaceSimilarityEngine<KEDetectedFace, FacePatchFeature> _engine;

    private ImageProcessing() {
        _engine = CustomFaceSimilarityEngine.create(
                ImageProcessing::createFKEFaceDetector,
                createFacialFeatureExtractor(),
                createFacialFeatureComparator()
                );
        //_engine.setFaceConfidence(25.0f);
        //_engine.setUseCache(true);
    }

    public static ImageProcessing createInstance() {
        return new ImageProcessing();
    }

    @Override
    public void findSimilarPeople(SearchPerson searchPerson, Collection<Person> potentialPersons) {
        /*new Thread(()-> {

        }).start();*/

        if(searchPerson.getImages().isEmpty()) return;

        long timeStart = System.currentTimeMillis();
        MemoryUtils.printMemoryStat();

        boolean isDetected = _engine.setSearchPerson(searchPerson);
        if(!isDetected) return;

        _engine.calculateSimilarities(potentialPersons);

        MemoryUtils.printMemoryStat();
        long timeEnd = System.currentTimeMillis();
        long timeCount = timeEnd - timeStart;
        System.out.println(String.format("Time: %d ms", timeCount));
        //_engine.resetEngine();
    }

    @Override
    public void addProcessingListener(ProcessingListener listener) {
        _engine.addProcessingListener(listener);
    }

    public static FKEFaceDetector createFKEFaceDetector() {
        return new FKEFaceDetector(HaarCascadeDetector.BuiltInCascade.frontalface_alt.load());
    }

    public static FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> createFacialFeatureExtractor() {
        FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FaceImageFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FacePatchFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new LocalLBPHistogram.Extractor(); // ScalingAligner AffineAligner RotateScaleAligner
        return featureExtractor;
    }

    public static FacialFeatureComparator<FacePatchFeature> createFacialFeatureComparator() {
        return new FaceFVComparator<>(FloatFVComparison.EUCLIDEAN);
    }
}

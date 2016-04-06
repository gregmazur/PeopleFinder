package people.network.service.image;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.FaceImageFeature;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import people.network.PeopleFinderApplication;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ImageProcessingTest {

    @Test
    public void testSimilarityEngine() throws Exception {
        System.out.println("========Test started========");

        FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FaceImageFeature.Extractor();
        FacialFeatureComparator<FacePatchFeature> featureComparator = new FaceFVComparator<>(FloatFVComparison.EUCLIDEAN);
        CustomFaceSimilarityEngine engine = CustomFaceSimilarityEngine.create(ImageProcessing::createFKEFaceDetector, featureExtractor, featureComparator);

        String testFile = "D:\\maya6.jpg";
        FImage img = ImageUtilities.readF(new File(testFile));
        FaceDetector<KEDetectedFace, FImage> detector = engine.detector();
        List<KEDetectedFace> faces = detector.detectFaces(img);
        List<FImage> faceImages = faces.stream().map(DetectedFace::getFacePatch).collect(Collectors.toList());
        DisplayUtilities.display("TestImage", faceImages);
        Thread.sleep(30000);

    }
}
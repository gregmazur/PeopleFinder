package people.network.service.image;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;
import people.network.entity.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public class Junk {

    private FaceRecognitionEngine<KEDetectedFace, Person> _faceEngine;

    public Junk() {
        // Create face stuff
        FKEFaceDetector faceDetector = new FKEFaceDetector(new HaarCascadeDetector());
        //Aligners best for FKEFaceDetector:  AffineAligner, MeshWarpAligner, RotateScaleAligner:
        FaceRecogniser<KEDetectedFace, Person> faceRecognizer = EigenFaceRecogniser.create(20, new RotateScaleAligner(), 1, DoubleFVComparison.CORRELATION, 0.9f);
        _faceEngine = FaceRecognitionEngine.create(faceDetector, faceRecognizer);
    }

    private void doFaceRecognizerProcessing(FImage image) throws IOException {

        // Decode image
        //BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgData));
        // Detect faces
        //FImage image = ImageUtilities.createFImage(img);


        List<KEDetectedFace> faces = _faceEngine.getDetector().detectFaces(image);

        //_faceEngine.train()

        // Go through detected faces
        List<FImage> detectedFaces = new ArrayList<>();
        detectedFaces.add(image);

       /* List<IndependentPair<KEDetectedFace, ScoredAnnotation<Person>>> rfaces = faceEngine.recogniseBest(image);
        for(IndependentPair<KEDetectedFace, ScoredAnnotation<Person>> pair : rfaces) {
            ScoredAnnotation<Person> score = rfaces.get(0).getSecondObject();
        }*/


        for(DetectedFace face : faces) {
            detectedFaces.add(face.getFacePatch());

            // Find existing person for this face
            Person person = null;
            try {

                List<IndependentPair<KEDetectedFace, ScoredAnnotation<Person>>> rfaces = _faceEngine.recogniseBest(face.getFacePatch());
                ScoredAnnotation<Person> score = rfaces.get(0).getSecondObject();
                if(score != null) person = score.annotation;

            } catch(Throwable e) {
                System.out.println(e.getMessage());
            }

            // If not found, create
            if(person == null) {

                // Create person
                person = new Person();
                person.setId(11L);
                System.out.println("Identified new person: " + person.getId());

                // Train engine to recognize this new person
                System.out.println("1-//");
                _faceEngine.train(person, face.getFacePatch());
                System.out.println("2-//");
                //DisplayUtilities.display(ImageUtilities.createBufferedImage(face.getFacePatch()), "Person_Id="+person.getId());
            } else {

                // This person has been detected before
                System.out.println("Identified existing person: " + person.getId());

            }
        }

        DisplayUtilities.display("Test", detectedFaces);
        //FaceSimilarityEngine a = FaceSimilarityEngine.create()
    }
}

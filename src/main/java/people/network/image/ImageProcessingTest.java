package people.network.image;


import org.jetbrains.annotations.NotNull;
import org.openimaj.data.dataset.*;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.FaceImageFeature;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;
import people.network.entity.Person;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yemelin A.M.
 **/
public class ImageProcessingTest {

    private static final String TEST_IMG_FOLDER = "D:\\img_test\\potential";
    private static final String TEST_ME_IMG_FOLDER = "D:\\img_test\\me_test";
    private static long _id = 1;

    private CustomFaceSimilarityEngine<KEDetectedFace, FacePatchFeature> _faceSimilarityEngine;
    private CustomFaceRecognitionEngine<KEDetectedFace, Person> _faceRecognitionEngine;

    private ImageProcessingTest() {
        FKEFaceDetector faceDetector = new FKEFaceDetector(HaarCascadeDetector.BuiltInCascade.frontalface_alt.load());
        FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FaceImageFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FacePatchFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new LocalLBPHistogram.Extractor(); // ScalingAligner AffineAligner RotateScaleAligner
        FacialFeatureComparator<FacePatchFeature> featureComparator = new FaceFVComparator<>(FloatFVComparison.EUCLIDEAN);

        _faceSimilarityEngine = CustomFaceSimilarityEngine.create(faceDetector, featureExtractor, featureComparator);
        //_faceEngine.setFaceConfidence(25.0f);
        //_faceSimilarity.setUseCache(true);



        // Create face stuff
        //FKEFaceDetector faceDetector = new FKEFaceDetector(new HaarCascadeDetector());
        //Aligners best for FKEFaceDetector:  AffineAligner, MeshWarpAligner, RotateScaleAligner:
        FaceRecogniser<KEDetectedFace, Person> faceRecognizer = EigenFaceRecogniser.create(20, new RotateScaleAligner(), 1, DoubleFVComparison.EUCLIDEAN, 10);
        _faceRecognitionEngine = CustomFaceRecognitionEngine.create(faceDetector, faceRecognizer);
    }

    public static ImageProcessingTest createInstance() {
        return new ImageProcessingTest();
    }

    public void doImageProcessingTestRecognition() throws IOException {
        Person p = new Person();
        p.setId(777L);
        p.setFName("Arty");

        List<FImage> meImgList = getTestMeImages();
        DisplayUtilities.display("TestImage", meImgList);

        ListBackedDataset<FImage> listDataSet = new ListBackedDataset<>();
        listDataSet.addAll(meImgList);

        MapBackedDataset<Person, ListDataset<FImage>, FImage> mapDataSet = new MapBackedDataset<>();
        mapDataSet.add(p, listDataSet);
        System.out.println("Training started...");
        _faceRecognitionEngine.train(mapDataSet);
        System.out.println("Training finished...");

        String testFile = "D:\\img_test\\potential\\1.jpg";
        FImage img = readImage(testFile);

        try {

            List<IndependentPair<KEDetectedFace, List<ScoredAnnotation<Person>>>> rfaces = _faceRecognitionEngine.recognise(img);;
            DisplayUtilities.display(rfaces.get(0).getFirstObject().getFacePatch());
            rfaces.get(0).getSecondObject();


        } catch (Exception e) {
        }
    }

    public void doImageProcessingTestSimiliarity() throws IOException {
        String testFile = "D:\\img_test\\Photo_(16).jpg";

        Person searchPerson = createPerson(new File(testFile));
        searchPerson.setId(777L);
        List<Person> potentialPersonList = getTestPersons();

        //_faceEngine.setFaceConfidence(25.0f);
        //_faceSimilarityEngine.setSearchPerson(searchPerson);
        //_faceSimilarityEngine.setPotentialPersons(potentialPersonList);

        System.out.println(_faceSimilarityEngine.getSimilarityDictionary());

        /*List<FImage> list = new ArrayList<>();
        list.add(testImage);
        List<KEDetectedFace> detectedFacesList = _faceEngine.detector().detectFaces(testImage);
        for(KEDetectedFace detectedFace : detectedFacesList) {
            DisplayUtilities.display("TestImage. FaceConfidence=" + detectedFace.getConfidence(), detectedFace.getFacePatch());
            list.add(detectedFace.getFacePatch());
        }
        DisplayUtilities.display("TestImage", list);


        _faceEngine.setSearchPerson("SearchPerson", testImage);

        for(FImage image : imgList) {
            doFaceSimilarityProcessing(image, testImage);
        }*/
    }

    private List<FImage> getTestMeImages() throws IOException {
        File[] fileArr = getTestMeFiles();
        if(fileArr == null || fileArr.length == 0)
            return Collections.emptyList();
        List<FImage> imgList = new ArrayList<>(fileArr.length);
        for(File file : fileArr) {
            FImage fImage = readImage(file);
            imgList.add(fImage);
            //MBFImage rgbImg = readRGBImage(file);
            //DisplayUtilities.display(rgbImg);
            //DisplayUtilities.display(Transforms.calculateIntensity(rgbImg));
        }
        return imgList;
    }

    private File[] getTestMeFiles() {
        File imgDir = new File(TEST_ME_IMG_FOLDER);
        return imgDir.listFiles();
    }

    private File[] getTestFiles() {
        File imgDir = new File(TEST_IMG_FOLDER);
        return imgDir.listFiles();
    }

    private List<Person> getTestPersons() throws IOException {
        File[] fileArr = getTestFiles();
        if(fileArr == null || fileArr.length == 0)
            return Collections.emptyList();
        List<Person> list = new ArrayList<>(fileArr.length);
        long id = 1;
        for(File file : fileArr) {
            Person p = createPerson(file);
            p.setId(id++);
            list.add(p);
        }
        return list;
    }

    private @NotNull Person createPerson(File file) throws IOException {
        Person p = new Person(file);
        MBFImage rgbImage = readRGBImage(file);
        p.setMbfImage(rgbImage);
        p.setFImage(Transforms.calculateIntensity(rgbImage));
        p.setFName(file.getName());
        return p;
    }

    private FImage readImage(File file) throws IOException {
        //byte[] byteArr = readAllBytes(file);
        //return ImageUtilities.readF(new ByteArrayInputStream(byteArr));
        return ImageUtilities.readF(file);
    }

    private FImage readImage(String fileImageName) throws IOException {
        //byte[] byteArr = readFile(fileImageName);
        //return ImageUtilities.readF(new ByteArrayInputStream(byteArr));
        return readImage(new File(fileImageName));
    }

    private byte[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        if(file.exists())
            throw new IOException(String.format("File '%s' does not exist!", fileName));
        return readAllBytes(file);
    }

    private byte[] readAllBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    private MBFImage readRGBImage(File file) throws IOException {
        return ImageUtilities.readMBF(file);
    }


}

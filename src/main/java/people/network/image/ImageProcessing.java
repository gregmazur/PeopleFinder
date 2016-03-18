package people.network.image;


import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.alignment.MeshWarpAligner;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.alignment.ScalingAligner;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.FaceImageFeature;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
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
public class ImageProcessing {

    private static final String TEST_IMG_FOLDER = "D:\\img_test\\me";
    private static long _id = 1;

    private CustomFaceSimilarityEngine<KEDetectedFace, FacePatchFeature> _faceEngine;

    private ImageProcessing() {

        FKEFaceDetector faceDetector = new FKEFaceDetector(HaarCascadeDetector.BuiltInCascade.frontalface_alt.load());
        FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FaceImageFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new FacePatchFeature.Extractor();
        //FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> featureExtractor = new LocalLBPHistogram.Extractor(); // ScalingAligner AffineAligner RotateScaleAligner
        FacialFeatureComparator<FacePatchFeature> featureComparator = new FaceFVComparator<>(FloatFVComparison.EUCLIDEAN);

        _faceEngine = CustomFaceSimilarityEngine.create(faceDetector, featureExtractor, featureComparator);
        //_faceEngine.setFaceConfidence(25.0f);
        //_faceSimilarity.setUseCache(true);
    }

    public static ImageProcessing createInstance() {
        return new ImageProcessing();
    }

    public void doImageProcessingTest() throws IOException {
        String testFile = "D:\\img_test\\Photo_(16).jpg";

        Person searchPerson = createPerson(new File(testFile));
        searchPerson.setId(777L);
        List<Person> potentialPersonList = getTestPersons();

        //_faceEngine.setFaceConfidence(25.0f);
        _faceEngine.setSearchPerson(searchPerson);
        _faceEngine.setPotentialPersons(potentialPersonList);

        System.out.println(_faceEngine.getSimilarityDictionary());

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

    private void doFaceSimilarityProcessing(FImage queryImage, FImage testImage) {
        String strId = "QueryImage=" + _id++;
        _faceEngine.setPotentialPerson(strId, queryImage);
        DisplayUtilities.display(strId, queryImage);
        _faceEngine.findSimilarities();
    }

    private List<FImage> getTestImages() throws IOException {
        File[] fileArr = getTestFiles();
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
        p.setMBFImage(rgbImage);
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

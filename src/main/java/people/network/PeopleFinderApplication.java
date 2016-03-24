package people.network;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import people.network.service.image.ImageProcessingTest;

import java.io.IOException;


@SpringBootApplication
public class PeopleFinderApplication {

	public static void main(String[] args) throws IOException {
		//testing
		ImageProcessingTest proc = ImageProcessingTest.createInstance();
		proc.doImageProcessingTestSimiliarity();
		//proc.doImageProcessingTestRecognition();
	}

	/*public static void main(String[] args) {
		SpringApplication.run(PeopleFinderApplication.class, args);
		//for IBIS
//		System.setProperty("https.proxyHost", "proxy.ibis");
//		System.setProperty("https.proxyPort", "3128");
	}*/

}

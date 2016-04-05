package people.network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class PeopleFinderApplication {

	public static void main(String[] args) throws IOException {
		//testing
		//ImageProcessingTestOld proc = ImageProcessingTestOld.createInstance();
		//proc.doImageProcessingTestSimiliarity();
		//proc.doImageProcessingTestRecognition();


		SpringApplication.run(PeopleFinderApplication.class, args);
		//for IBIS
		System.setProperty("https.proxyHost", "proxy.ibis");
		System.setProperty("https.proxyPort", "3128");
	}



}

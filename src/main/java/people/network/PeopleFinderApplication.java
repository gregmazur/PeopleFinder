package people.network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import people.network.image.ImageProcessing;

import java.io.IOException;


@SpringBootApplication
public class PeopleFinderApplication {

	public static void main(String[] args) throws IOException {
		//testing
		ImageProcessing proc = ImageProcessing.createInstance();
		proc.doImageProcessingTest();
	}

	/*public static void main(String[] args) {
		SpringApplication.run(PeopleFinderApplication.class, args);
		//for IBIS
		System.setProperty("https.proxyHost", "proxy.ibis");
		System.setProperty("https.proxyPort", "3128");
	}*/

}

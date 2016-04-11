package people.network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import people.network.service.utils.AppProperties;

import java.io.IOException;


@SpringBootApplication
public class PeopleFinderApplication {

	public static void main(String[] args) throws IOException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		SpringApplication.run(PeopleFinderApplication.class, args);


		if(AppProperties.isProxyInUse()) {
			System.setProperty("https.proxyHost", AppProperties.getProxyHost());
			System.setProperty("https.proxyPort", AppProperties.getProxyPort());
		}
	}
}

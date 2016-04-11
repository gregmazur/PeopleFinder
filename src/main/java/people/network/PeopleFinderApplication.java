package people.network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;


@SpringBootApplication
public class PeopleFinderApplication {


	public static void main(String[] args) throws IOException {

		SpringApplication.run(PeopleFinderApplication.class, args);

		//for IBIS
		System.setProperty("https.proxyHost", "proxy.ibis");
		System.setProperty("https.proxyPort", "3128");
	}



}

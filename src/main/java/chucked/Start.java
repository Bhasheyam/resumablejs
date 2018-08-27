package chucked;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class Start {
	
		public static void main( String[] args ) 
	    {
	        SpringApplication.run( Start.class, args );
	    }
	    @Bean
	    public Filter filter(){
	        ShallowEtagHeaderFilter filter=new ShallowEtagHeaderFilter();
	        return filter;
	    }
	    @Bean
	    MultipartConfigElement multipartConfigElement() {
	        MultipartConfigFactory factory = new MultipartConfigFactory();
	        factory.setMaxFileSize("128KB");
	        factory.setMaxRequestSize("128KB");
	        return factory.createMultipartConfig();
	    }

	}



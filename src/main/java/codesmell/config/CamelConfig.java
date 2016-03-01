package codesmell.config;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import codesmell.camel.routes.MultiItemRouteBuilder;

@Configuration
@PropertySource(value={"classpath:application.properties"})
public class CamelConfig extends CamelConfiguration {
	private static Logger log = LoggerFactory.getLogger(CamelConfig.class);
	
	public static final String SPLITTER_THREAD_POOL = "splitterThreadPool";
	
	@Value(value = "${splitterPoolSize}")
	private Integer poolSize;
	
	@Value(value = "${splitterMaxPoolSize}")
	private Integer maxPoolSize;
	
	@Value(value = "${splitterMaxQueueSize}")
	private Integer maxQueueSize;

	
	@Override
	public List<RouteBuilder> routes() {
		log.debug("routes are under construction...");
		
		return Arrays.asList(buildMultiItemRouteBuilder());
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
	    return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public MultiItemRouteBuilder buildMultiItemRouteBuilder() {
		log.debug("building MultiItemRouteBuilder...");
		
		return new MultiItemRouteBuilder();
	}
	
	@Bean
	public ThreadPoolProfile buildSplitterThreadPool() {
		log.debug("configuring thread pool profile...");
		
		return new ThreadPoolProfileBuilder(SPLITTER_THREAD_POOL)
				.poolSize(poolSize)
				.maxPoolSize(maxPoolSize)
				.maxQueueSize(maxQueueSize)
				.build();
	}
	
}

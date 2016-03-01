package codesmell.camel.routes;

import java.util.List;
import java.util.UUID;

import org.apache.camel.Body;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.ThreadPoolProfile;
import org.springframework.beans.factory.annotation.Autowired;

import codesmell.domain.Order;
import codesmell.domain.Product;

/**
 * Take an order comprised of multiple projects,
 * split them into separate products for parallel processing
 * then pull all back together into a single response  
 *
 * Using the Splitter EIP
 * http://camel.apache.org/splitter.html
 */
public class MultiItemRouteBuilder extends RouteBuilder {

	@Autowired
	ThreadPoolProfile splitterThreadPool;
	
	public static final String DIRECT_ORDER_TOP_ENDPOINT = "direct:order.top";
	public static final String DIRECT_PRODUCT_TOP_ENDPOINT = "direct:product.top";
	
	@Override
	public void configure() throws Exception {
		//
		// register the thread pool
		//
		this.getContext().getExecutorServiceManager().registerThreadPoolProfile(splitterThreadPool);
		
		//
		// route handling the order
		//
		from(DIRECT_ORDER_TOP_ENDPOINT).routeId("multiItemRoute")
			.log("received order: ${body}")
			.split()
				.method(this, "splitProducts")
				//.executorServiceRef(CamelConfig.SPLITTER_THREAD_POOL)
				.to(DIRECT_PRODUCT_TOP_ENDPOINT)
			.log("completed order: ${body}")
			.end();
		
		//
		// route handling the product
		//
		from(DIRECT_PRODUCT_TOP_ENDPOINT).routeId("productRoute")
			.log("processing item: ${body}")
			.process(exchange -> {
				Product product = (Product) exchange.getIn().getBody();
				product.setPin(UUID.randomUUID().toString());
			})
			.end();
	}

	/**
	 * return the list of products associated with the order
	 * @param order
	 * @return
	 */
	public List<Product> splitProducts(@Body Order order) {
		List<Product> productList = null;
		if (order != null) {
			productList = order.getProductList();
		}
		return productList;
	}
}

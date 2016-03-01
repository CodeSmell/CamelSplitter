package codesmell.camel.routes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import codesmell.config.CamelConfig;
import codesmell.domain.Order;
import codesmell.domain.Product;
import codesmell.util.ConcurrentTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CamelConfig.class }, loader = AnnotationConfigContextLoader.class)
public class MultiItemRouteBuilderTest extends CamelTestSupport {

	@Produce(uri = MultiItemRouteBuilder.DIRECT_ORDER_TOP_ENDPOINT)
	ProducerTemplate camelProducer;
	
	@Autowired
	MultiItemRouteBuilder multiItemRouteBuilder;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        context.addRoutes(multiItemRouteBuilder);
    }
	
	@Test
	public void test_split_route() {
		
		NotifyBuilder notify = new NotifyBuilder(context)
				.from(MultiItemRouteBuilder.DIRECT_ORDER_TOP_ENDPOINT)
				.whenExactlyDone(2)
				.wereSentTo(MultiItemRouteBuilder.DIRECT_PRODUCT_TOP_ENDPOINT)
				.create();
		
		Order order = new Order();
		order.setTransactionId("42");
		order.addProduct(new Product("12345", "10", null));
		order.addProduct(new Product("67890", "11", null));
		
		Object obj = camelProducer.requestBody(order);
		assertNotNull(obj);
		
		Order processedOrder = (Order) obj;
		List<Product> pList = processedOrder.getProductList();
		assertNotNull(pList);
		assertEquals(2, pList.size());
		
		assertEquals("12345", pList.get(0).getUpc());
		assertNotNull(pList.get(0).getPin());
		
		assertEquals("67890", pList.get(1).getUpc());
		assertNotNull(pList.get(1).getPin());
		
		
		assertTrue(notify.matches(3, TimeUnit.SECONDS));
	}
	
	@Test
	public void test_split_route_many_products() {
		
		NotifyBuilder notify = new NotifyBuilder(context)
				.from(MultiItemRouteBuilder.DIRECT_ORDER_TOP_ENDPOINT)
				.whenExactlyDone(10)
				.wereSentTo(MultiItemRouteBuilder.DIRECT_PRODUCT_TOP_ENDPOINT)
				.create();
		
		Order order = new Order();
		order.setTransactionId("42");
		for (int i=0;i<10;i++) {
			order.addProduct(new Product(new Integer(i).toString(), null, null));
		}
		
		Object obj = camelProducer.requestBody(order);
		assertNotNull(obj);
		
		Order processedOrder = (Order) obj;
		List<Product> pList = processedOrder.getProductList();
		assertNotNull(pList);
		assertEquals(10, pList.size());
	
		assertTrue(notify.matches(5, TimeUnit.SECONDS));
	}
    
    @Test
    public void test_split_route_concurrent() throws InterruptedException {
        int threadCount = 30;
        int maxTimeoutSeconds = 30;
        int numExceptionsExpected = 0;
        
        List<Runnable> requests = new ArrayList<Runnable>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            requests.add(new ProcessOrderRunnable(new Integer(i).toString()));
        }
        
        ConcurrentTestUtil.assertConcurrent("", requests, maxTimeoutSeconds, numExceptionsExpected);
    }
    
    class ProcessOrderRunnable implements Runnable {
        String orderId;

        public ProcessOrderRunnable(String orderId) {
            this.orderId = orderId;
        }

        public void run() {
    		Order order = new Order();
    		order.setTransactionId(orderId);
    		order.addProduct(new Product(orderId + ".12345", "10", null));
    		order.addProduct(new Product(orderId + ".67890", "11", null));
    		
    		Order processedOrder = (Order) camelProducer.requestBody(order);
    		List<Product> pList = processedOrder.getProductList();
    		assertNotNull(pList);
    		assertEquals(2, pList.size());
    		
    		assertTrue(pList.get(0).getUpc().startsWith(orderId));
    		assertNotNull(pList.get(0).getPin());
    		
    		assertTrue(pList.get(1).getUpc().startsWith(orderId));
    		assertNotNull(pList.get(1).getPin());
        }
    }

}

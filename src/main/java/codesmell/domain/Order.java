package codesmell.domain;

import java.util.ArrayList;
import java.util.List;

public class Order {
	
	private String transactionId;
	private List<Product> productList;
	
	public void addProduct(Product product) {
		if (productList == null) {
			productList = new ArrayList<Product>();
		}
		productList.add(product);
	}
	
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public List<Product> getProductList() {
		return productList;
	}
	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\r\n");
		sb.append("Order Transaction Id:" + this.getTransactionId());
		
		if (productList != null) {
			for(Product p:productList) {
				if (p != null) {
					sb.append("\r\n --- Product ---");
					sb.append(p.toString());
				}
			}
		}
		
		return sb.toString();
	}
	
}

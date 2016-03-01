package codesmell.domain;

public class Product {
	private String upc;
	private String digitalCode;
	private String pin;
	
	public Product() {
	}

	public Product(String upc, String digitalCode, String pin) {
		this.upc = upc;
		this.digitalCode = digitalCode;
		this.pin = pin;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\r\n");
		sb.append("upc:" + this.getUpc());
		sb.append("\r\n");
		sb.append("digital code:" + this.getDigitalCode());
		sb.append("\r\n");
		sb.append("pin:" + this.getPin());
		
		return sb.toString();
	}
	
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
	}
	public String getDigitalCode() {
		return digitalCode;
	}
	public void setDigitalCode(String digitalCode) {
		this.digitalCode = digitalCode;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	
	
	
}

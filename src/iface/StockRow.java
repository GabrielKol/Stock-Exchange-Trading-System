package iface;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class StockRow {

	private final SimpleStringProperty stockName;
	private final SimpleIntegerProperty amount;
	private final SimpleIntegerProperty price;
	private final SimpleStringProperty description;

	// Constructor:
	protected StockRow(String stockName, int amount, int price, String description) {
		this.stockName = new SimpleStringProperty(stockName);
		this.amount = new SimpleIntegerProperty(amount);
		this.price = new SimpleIntegerProperty(price);
		this.description = new SimpleStringProperty(description);
	}

	// Getters:
	public String getStockName() {
		return stockName.get();
	}
	public int getAmount() {
		return amount.get();
	}
	public int getPrice() {
		return price.get();
	}
	public String getDescription() {
		return description.get();
	}

	// Setters:
	public void setStockName(String stockName) {
	  	this.stockName.set(stockName);
	}
	public void setAmount(int amount) {
	  	this.amount.set(amount);
	}
	public void setPrice(int price) {
	  	this.price.set(price);
	}
	public void setDescription(String description) {
	  	this.description.set(description);
	}

}

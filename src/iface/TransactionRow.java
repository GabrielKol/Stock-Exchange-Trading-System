package iface;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TransactionRow {

	private final SimpleStringProperty openingDate;
	private final SimpleIntegerProperty orderCode;
	private final SimpleStringProperty stockName;
	private final SimpleIntegerProperty amount;
	private final SimpleIntegerProperty price;
	private final SimpleStringProperty transactionType;
	private final SimpleStringProperty transactionState;
	private final SimpleStringProperty closingDate;
	private final SimpleIntegerProperty profit;

	// Constructor:
	protected TransactionRow(String openingDate, int orderCode, String stockName, int amount, int price,
			String transactionType ,String transactionState, String closingDate, int profit) {
		this.openingDate = new SimpleStringProperty(openingDate);
		this.orderCode = new SimpleIntegerProperty(orderCode);
		this.stockName = new SimpleStringProperty(stockName);
		this.amount = new SimpleIntegerProperty(amount);
		this.price = new SimpleIntegerProperty(price);
		this.transactionType = new SimpleStringProperty(transactionType);
		this.transactionState = new SimpleStringProperty(transactionState);
		this.closingDate = new SimpleStringProperty(closingDate);
		this.profit = new SimpleIntegerProperty(profit);
	}

	// Getters:
	public String getOpeningDate() {
		return openingDate.get();
	}
	public int getOrderCode() {
		return orderCode.get();
	}
	public String getStockName() {
		return stockName.get();
	}
	public int getAmount() {
		return amount.get();
	}
	public int getPrice() {
		return price.get();
	}
	public String getTransactionType() {
		return transactionType.get();
	}
	public String getTransactionState() {
		return transactionState.get();
	}
	public String getClosingDate() {
		return closingDate.get();
	}
	public int getProfit() {
		return profit.get();
	}

	// Setters:
	public void setOpeningDate(String openingDate) {
	  	this.openingDate.set(openingDate);
	}
	public void setOrderCode(int orderCode) {
	  	this.orderCode.set(orderCode);
	}
	public void setStockName(String stockName) {
	  	this.stockName.set(stockName);
	}
	public void setAmount(int amount) {
	  	this.amount.set(amount);
	}
	public void setPrice(int price) {
	  	this.price.set(price);
	}
	public void setTransactionType(String transactionType) {
	  	this.transactionType.set(transactionType);
	}
	public void setTransactionState(String transactionState) {
	  	this.transactionState.set(transactionState);
	}
	public void setClosingDate(String closingDate) {
	  	this.closingDate.set(closingDate);
	}
	public void setProfit(int profit) {
    	this.profit.set(profit);
    }

}

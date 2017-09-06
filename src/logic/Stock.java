package logic;

public class Stock {

	private String stockName;
	private int amount;
	private int price;
	private String description;

	// Constructor:
	public Stock(String stockName, int amount, int price, String description) {
		this.stockName = stockName;
		this.amount = amount;
		this.price = price;
		this.description = description;
	}

	// Getters:
	public String getStockName() {
		return stockName;
	}
	public int getAmount() {
		return amount;
	}
	public int getPrice() {
		return price;
	}
	public String getDescription() {
		return description;
	}

}

package logic;

public class Transaction {

	private String openingDate;
	private int orderCode;
	private String stockName;
	private int amount;
	private int price;
	private String transactionType;
	private String transactionState;
	private String closingDate;
	private int profit;

	// Constructor:
	public Transaction(String openingDate, int orderCode, String stockName, int amount, int price,
			String transactionType, String transactionState, String closingDate, int profit) {
		this.openingDate = openingDate;
		this.orderCode = orderCode;
		this.stockName = stockName;
		this.amount = amount;
		this.price = price;
		this.transactionType = transactionType;
		this.transactionState = transactionState;
		this.closingDate = closingDate;
		this.profit = profit;
	}

	// Getters:
	public String getOpeningDate() {
		return openingDate;
	}
	public int getOrderCode() {
		return orderCode;
	}
	public String getStockName() {
		return stockName;
	}
	public int getAmount() {
		return amount;
	}
	public int getPrice() {
		return price;
	}
	public String getTransactionType() {
		return transactionType;
	}
	public String getTransactionState() {
		return transactionState;
	}
	public String getClosingDate() {
		return closingDate;
	}
	public int getProfit() {
		return profit;
	}

}

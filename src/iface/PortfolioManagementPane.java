package iface;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import logic.AppLogic;
import logic.Transaction;

public class PortfolioManagementPane extends ActivityPane {

	protected PortfolioManagementPane() {
		super();

		// Setting up the title:
		Label title = new Label("Your Transaction Portfolio");
		title.setPadding(new Insets(8, 20, 8, 20));
		title.setMinHeight(40);
		title.setFont(new Font("Arial", 16));
		title.setUnderline(true);


		// Setting up the table:

		TableView<TransactionRow> transactionsTableView = new TableView<TransactionRow>();
		ObservableList<TransactionRow> transactionsData = FXCollections.observableArrayList();
		transactionsTableView.setItems(transactionsData);

	    TableColumn<TransactionRow, String> openingDateColumn = new TableColumn<TransactionRow, String>("Opening Date");
	    openingDateColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("openingDate"));

	    TableColumn<TransactionRow, String> orderCodeColumn = new TableColumn<TransactionRow, String>("Order Code");
	    orderCodeColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("orderCode"));

	    TableColumn<TransactionRow, String> stockNameColumn = new TableColumn<TransactionRow, String>("Stock Name");
	    stockNameColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("stockName"));

	    TableColumn<TransactionRow, String> amountColumn = new TableColumn<TransactionRow, String>("Amount");
	    amountColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("amount"));

	    TableColumn<TransactionRow, String> priceColumn = new TableColumn<TransactionRow, String>("Price");
	    priceColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("price"));

	    TableColumn<TransactionRow, String> transactionTypeColumn = new TableColumn<TransactionRow, String>("Type");
	    transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("transactionType"));

	    TableColumn<TransactionRow, String> transactionStateColumn = new TableColumn<TransactionRow, String>("Position");
	    transactionStateColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("transactionState"));

	    TableColumn<TransactionRow, String> closingDateColumn = new TableColumn<TransactionRow, String>("Closing Date");
	    closingDateColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("closingDate"));

	    TableColumn<TransactionRow, String> profitColumn = new TableColumn<TransactionRow, String>("Profit");
	    profitColumn.setCellValueFactory(new PropertyValueFactory<TransactionRow, String>("profit"));

	    transactionsTableView.getColumns().addAll(openingDateColumn, orderCodeColumn, stockNameColumn, amountColumn,
				priceColumn, transactionTypeColumn, transactionStateColumn, closingDateColumn, profitColumn);


		// Getting all transactions data (by communication with the second tier) and updating the table:

	    ArrayList<Transaction> transactions = AppLogic.getAppData().getAllTransactions();

	    for(int i = 0; i < transactions.size(); i++){
	    	String openingDate = transactions.get(i).getOpeningDate();
	    	int orderCode = transactions.get(i).getOrderCode();
	    	String stockName = transactions.get(i).getStockName();
	    	int amount = transactions.get(i).getAmount();
	    	int price = transactions.get(i).getPrice();
	    	String transactionType = transactions.get(i).getTransactionType();
	    	String transactionState = transactions.get(i).getTransactionState();
	    	String closingDate = transactions.get(i).getClosingDate();
	    	int profit = transactions.get(i).getProfit();
	    	transactionsData.add(new TransactionRow(openingDate, orderCode, stockName, amount, price,
	    			transactionType, transactionState, closingDate, profit));
		}


	    // Finally, adding everything to the major pane:
	    setTop(title);
		setCenter(transactionsTableView);

	}

}

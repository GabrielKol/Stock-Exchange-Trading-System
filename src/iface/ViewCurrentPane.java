package iface;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import logic.AppLogic;
import logic.Stock;

public class ViewCurrentPane extends ActivityPane {

	protected ViewCurrentPane() {
		super();

	    // Setting up a loading screen:
		displayFeedback("Loading...");

		// Creating a thread that will work in the background in order to
		// update the UI and show the correct contents when the right data is ready.
		// In the meanwhile, all the user would be able to see is a loading screen.
		new Thread(()-> {


			// Setting up the title:
			Label stockMarketTitle = new Label("Stock Market Position");
			stockMarketTitle.setPadding(new Insets(8, 20, 8, 20));
			stockMarketTitle.setMinHeight(40);
			stockMarketTitle.setFont(new Font("Arial", 16));
			stockMarketTitle.setUnderline(true);


			// Setting up the table:

			TableView<StockRow> stocksTableView = new TableView<StockRow>();
			ObservableList<StockRow> stocksData = FXCollections.observableArrayList();

			stocksTableView.setItems(stocksData);

		    TableColumn<StockRow, String> stockNameColumn = new TableColumn<StockRow, String>("Stock Name");
		    stockNameColumn.setCellValueFactory(new PropertyValueFactory<StockRow, String>("stockName"));

		    TableColumn<StockRow, String> amountColumn = new TableColumn<StockRow, String>("Amount");
		    amountColumn.setCellValueFactory(new PropertyValueFactory<StockRow, String>("amount"));

		    TableColumn<StockRow, String> priceColumn = new TableColumn<StockRow, String>("Price");
		    priceColumn.setCellValueFactory(new PropertyValueFactory<StockRow, String>("price"));

		    TableColumn<StockRow, String> descriptionColumn = new TableColumn<StockRow, String>("Description");
		    descriptionColumn.setCellValueFactory(new PropertyValueFactory<StockRow, String>("description"));

		    stocksTableView.getColumns().addAll(stockNameColumn, amountColumn, priceColumn, descriptionColumn);


			// Getting all stocks data (by communication with the second tier) and updating the UI:
			ArrayList<Stock> stocks = AppLogic.getAppData().getStockSuppliesAndDemands();

			// Filling in the table with the correct data:
			for(int i = 0; i < stocks.size(); i++){
		    	String stockName = stocks.get(i).getStockName();
		    	int amount = stocks.get(i).getAmount();
		    	int price = stocks.get(i).getPrice();
		    	String description = stocks.get(i).getDescription();
		    	stocksData.add(new StockRow(stockName, amount, price, description));
			}


			// Finally, get rid of the loading display, and show the proper contents:
			Platform.runLater(()-> {
			    removeFeedback();
			    setTop(stockMarketTitle);
			    setCenter(stocksTableView);
			});

		}).start();

	}


}

package iface;

import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import logic.AppLogic;
import logic.Constants;
import logic.Stock;
import logic.TransactionEvents;

public class SellPane extends ActivityPane {

	protected SellPane() {
		super();

	    // Setting up a loading screen:
		displayFeedback("Loading...");

		// Creating a thread that will work in the background in order to
		// update the UI and show the correct contents when the right data is ready.
		// In the meanwhile, all the user would be able to see is a loading screen.
		new Thread(()-> {

			// Setting up the table:

			TableView<StockRow> stocksTableView = new TableView<StockRow>();
			stocksTableView.setMinHeight(146);
			stocksTableView.setMaxHeight(146);
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


			// Getting all stock demands data (by communication with the second tier) and updating the UI:
			ArrayList<Stock> demands = AppLogic.getAppData().getStockDemands();

			// Filling in the table with the correct data:
			for(int i = 0; i < demands.size(); i++){
		    	String stockName = demands.get(i).getStockName();
		    	int amount = demands.get(i).getAmount();
		    	int price = demands.get(i).getPrice();
		    	String description = demands.get(i).getDescription();
		    	stocksData.add(new StockRow(stockName, amount, price, description));
			}


			// Setting up the stock market pane:

			Label stockMarketTitle = new Label("Stock Market's Demands");
			stockMarketTitle.setPadding(new Insets(8, 20, 8, 20));
			stockMarketTitle.setMinHeight(40);
			stockMarketTitle.setFont(new Font("Arial", 16));
			stockMarketTitle.setUnderline(true);

			Label stockMarketTip = new Label("Tip: The following table should help you make a better vendition decision."
					+ "\nYet, you could always try selling something that isn't currently demanded by the stock market.");
			stockMarketTip.setPadding(new Insets(0, 20, 8, 20));

			VBox stockMarketPane = new VBox();
			stockMarketPane.setStyle("-fx-border-color: gray; -fx-border-radius: 2.0; -fx-border-width: 1px;");
			stockMarketPane.getChildren().addAll(stockMarketTitle, stockMarketTip, stocksTableView);


			// Setting up the bank account pane:
			BankAccountPane bankAccountPane = new BankAccountPane();


			// Setting up the primary pane:

			Label sellTitle = new Label("Make A Sale/Supply");
			sellTitle.setMinHeight(40);
			sellTitle.setFont(new Font("Arial", 16));
			sellTitle.setUnderline(true);

			ComboBox<String> assetsCB = new ComboBox<>();
			ObservableList<String> itemsCB = FXCollections.observableArrayList(Constants.ASSET_NAMES);
			assetsCB.getItems().addAll(itemsCB);
			assetsCB.setValue(Constants.ASSET_NAMES[0]);

			TextField amount = new TextField();
			amount.setPromptText("Amount");
			TextField price = new TextField();
			price.setPromptText("Price");

			// Forcing the fields to be numeric only
			amount.textProperty().addListener(new ChangeListener<String>() {
		        @Override
		        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		            if (!newValue.matches("\\d*")) {
		            	amount.setText(newValue.replaceAll("[^\\d]", ""));
		            }
		        }
		    });
			price.textProperty().addListener(new ChangeListener<String>() {
		        @Override
		        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		            if (!newValue.matches("\\d*")) {
		            	price.setText(newValue.replaceAll("[^\\d]", ""));
		            }
		        }
		    });

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);

			grid.add(new Label("Asset Name:"), 0, 0);
			grid.add(assetsCB, 1, 0);
			grid.add(new Label("Amount:"), 0, 1);
			grid.add(amount, 1, 1);
			grid.add(new Label("Price:"), 0, 2);
			grid.add(price, 1, 2);

			Button callToActionButton = new Button("Place Ask");
			callToActionButton.setStyle("-fx-font: 24 Ariel; -fx-base: #accaf9;");
			callToActionButton.setMinSize(170, 94);
			callToActionButton.setMaxSize(170, 94);

			HBox greedNBottonPane = new HBox();
			greedNBottonPane.setSpacing(20);
			greedNBottonPane.getChildren().addAll(grid, callToActionButton);

			VBox primaryPane = new VBox();
			primaryPane.setSpacing(8);
			primaryPane.setStyle("-fx-border-color: gray; -fx-border-radius: 2.0; -fx-border-width: 1px;");
			primaryPane.setPadding(new Insets(8, 20, 8, 20));
			primaryPane.getChildren().addAll(sellTitle, greedNBottonPane);


			// Finally, get rid of the loading display, and show the proper contents:
			Platform.runLater(()-> {
			    removeFeedback();
			    setTop(stockMarketPane);
			    setLeft(bankAccountPane);
			    setCenter(primaryPane);
			});


			// Setting up the action listener for the "call to action" button:
		    callToActionButton.setOnAction(e -> {

		    	// Confirming the trader's decision:
		    	Alert confirmationDialog = new Alert(AlertType.CONFIRMATION);
		    	confirmationDialog.setTitle("Confirmation Dialog");
		    	confirmationDialog.setHeaderText("Transaction Verification");
		    	confirmationDialog.setContentText("You are about to make a transaction. Are you ok with this decision?");
		    	Optional<ButtonType> decision = confirmationDialog.showAndWait();

		    	// If trader chose OK.
		    	if (decision.get() == ButtonType.OK){

		    		try {

			    		// Making sure input is valid
			    		// (if the integers are not positive, we won't continue,
			    		// and if they are not integers, meaning the fields are empty,
			    		// then an exception will be thrown and we won't continue either)
			    		if(Integer.parseInt(amount.getText()) > 0 && Integer.parseInt(price.getText()) > 0) {

			    			// If we got passed here, then the input is valid.

				    		// Displaying a proper message on the screen so inform the user he should wait:
				    		Platform.runLater(()-> {
					    		getChildren().removeAll(stockMarketPane, bankAccountPane, primaryPane);
						    	displayFeedback("Trying to Make the Transaction...\nPlease Wait.");
					    	});

				    		// Opening a thread that will communicate with the second tier to pass the user's action
				    		// and wait in order to show the user the proper message regarding the transaction:
				    		new Thread(()-> {

					    		TransactionEvents.eventType result = AppLogic.getAppData().makeTransaction(
					    				assetsCB.getValue(),
					    				Integer.parseInt(amount.getText()),
					    				Integer.parseInt(price.getText()),
					    				TransactionEvents.eventType.ASK_ATTEMPT);

					    		Platform.runLater(()-> {

					    			removeFeedback();

					    			if(result == TransactionEvents.eventType.NOT_ENOUGH_ASSETS) {
					    				displayFeedback("Sorry, the ransaction could not be made."
					    						+ "\nThere are not enough assets in your bank account.");
					    			}
					    			else if(result == TransactionEvents.eventType.DEAL_OPENED) {
					    				displayFeedback("Deal was Successfuly Opened!");
					    			}

					    		});

					    	}).start();

			    		}

			    		else {
			    			Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Error Dialog");
							alert.setHeaderText(null);
							alert.setContentText("Input is invalid.");
							alert.showAndWait();
			    		}

			    	}

			    	catch(NumberFormatException ex) {
			    		Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Error Dialog");
						alert.setHeaderText(null);
						alert.setContentText("Input is invalid.");
						alert.showAndWait();
			    	}

		    	}

		    });

		}).start();

	}


}

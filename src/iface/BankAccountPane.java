package iface;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import logic.AppLogic;
import logic.Constants;

public class BankAccountPane extends VBox{

	protected BankAccountPane() {

		Label bankAccountTitle = new Label("Your Bank Account's Assets");
		bankAccountTitle.setMinHeight(40);
		bankAccountTitle.setFont(new Font("Arial", 16));
		bankAccountTitle.setUnderline(true);

		ArrayList<Label> assetLabels = new ArrayList<Label>();

		// Getting a map of all assets in the bank account (by communicating with the second tier):
		LinkedHashMap<String, Integer> assets = AppLogic.getAppData().getBankAccountAssets();
		for (Map.Entry<String, Integer> entry : assets.entrySet()) {
			Label assetLabel = new Label(entry.getKey() + ": " + entry.getValue());
			assetLabels.add(assetLabel);
		}

		// Getting the total account value (by communicating with the second tier):
		int totalValue = AppLogic.getAppData().getTotalAccountValue();
		Label totalLabel = new Label("Total Account Value: " + totalValue + " " + Constants.CURRENCY_NAME);
		totalLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
		assetLabels.add(totalLabel);

		// Getting the account yield (by communicating with the second tier):
		String yield = AppLogic.getAppData().getAccountYield();
		Label yieldLabel = new Label("Account Yield: " + yield);
		yieldLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
		assetLabels.add(yieldLabel);

		this.setSpacing(4);
		this.setStyle("-fx-border-color: gray; -fx-border-radius: 2.0; -fx-border-width: 1px;");
		this.setPadding(new Insets(8, 20, 8, 20));
		this.getChildren().add(bankAccountTitle);
		this.getChildren().addAll(assetLabels);

	}
}

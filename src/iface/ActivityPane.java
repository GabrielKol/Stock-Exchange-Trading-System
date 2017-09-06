package iface;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class ActivityPane extends BorderPane {

	private Label mFeedbackLabel;

	protected ActivityPane() {
		super();
		setStyle("-fx-border-radius: 4.0; -fx-border-color: #2076fc; -fx-border-width: 3px;");
		setMinWidth(710);
		setMaxWidth(710);
	}

	protected void displayFeedback(String message) {
		mFeedbackLabel = new Label(message);
		mFeedbackLabel.setAlignment(Pos.CENTER);
		mFeedbackLabel.setFont(new Font("Arial", 18));
		setCenter(mFeedbackLabel);
	}

	protected void removeFeedback() {
		getChildren().remove(mFeedbackLabel);
	}

}

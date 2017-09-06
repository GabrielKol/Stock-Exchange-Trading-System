package iface;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import logic.AppLogic;
import logic.AuthenticationEvents;
import logic.Trader;

public class TraderApp extends Application {

	private AuthenticationEvents.eventType mAuthType = null; // Login or SignUp
	private Trader mTrader = null;
	private BorderPane mActivityPane = null;

	public static void main(String[] args) {
		launch(args);
	}

	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) throws IOException {

		// Showing UI regarding user authentication:
		showAuthUI();

		// If was authenticated, show the main trader's UI:
		showMainUI(primaryStage);

	}

	private void showAuthUI() {

		boolean authenticationVerified = false;

		while(!authenticationVerified){

			// Show a proper dialog:
			showConnectionDialog();

			// Communicating with the business logic layer for a user authentication purpose:
			AuthenticationEvents.eventType resultET = AppLogic.getAppData().authenticateUser(mAuthType, mTrader);

			// If connection has granted:
			if(resultET == AuthenticationEvents.eventType.CONNECTION_GRANTED){
				authenticationVerified = true;
			}

			// Otherwise:
			else{

				// Showing a dialog that explains the reason for not being able to connect:
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Error Dialog");
				alert.setHeaderText(null);

				// In case Login failed:

				if(resultET == AuthenticationEvents.eventType.USERNAME_NOT_FOUND)
					alert.setContentText("Sorry, Username Could Not be Found in Database.");

				else if(resultET == AuthenticationEvents.eventType.PASSWORD_DENIED)
					alert.setContentText("Wrong Password.");

				// In case Sign Up failed:

				else if(resultET == AuthenticationEvents.eventType.NAME_OCCUPIED)
					alert.setContentText("This Username is Already Taken.");

				else if(resultET == AuthenticationEvents.eventType.SECRET_DENIED)
					alert.setContentText("Secret or Account ID was Incorrect.");

				alert.showAndWait();

			}
		}

	}

	private void showConnectionDialog() {

		  Alert alert = new Alert(AlertType.CONFIRMATION);
		  alert.setTitle("Confirmation Dialog");
		  alert.setHeaderText(null);
		  alert.setContentText("Choose your option:");

		  ButtonType buttonLogin = new ButtonType("Login");
		  ButtonType buttonSignUp = new ButtonType("Sign Up");
		  ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		  alert.getButtonTypes().setAll(buttonLogin, buttonSignUp, buttonTypeCancel);

		  Optional<ButtonType> result = alert.showAndWait();
		  if (result.get() == buttonLogin){
		      // ... user chose "Login"
			  mAuthType = AuthenticationEvents.eventType.LOGIN_ATTEMPT;
			  showLoginDialog();
		  } else if (result.get() == buttonSignUp) {
		      // ... user chose "SignUp"
			  mAuthType = AuthenticationEvents.eventType.SIGNUP_ATTEMPT;
			  showSignUpDialog();
		  } else {
		      // ... user chose CANCEL or closed the dialog
			  Platform.exit();
			  System.exit(0);
		  }

	}

	private void showLoginDialog() {

	  	  // Create the custom dialog.
		  Dialog<Pair<String, String>> dialog = new Dialog<>();

		  dialog.setTitle("Login");
		  dialog.setHeaderText("Please insert your username and password below:");

		  // Set the button types.
		  ButtonType loginButtonType = new ButtonType("Done", ButtonData.OK_DONE);
		  dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		  // Create the username and password labels and fields.
		  GridPane grid = new GridPane();
		  grid.setHgap(10);
		  grid.setVgap(10);
		  grid.setPadding(new Insets(20, 150, 10, 10));

		  TextField username = new TextField();
		  username.setPromptText("Username");
		  PasswordField password = new PasswordField();
		  password.setPromptText("Password");

		  grid.add(new Label("Username:"), 0, 0);
		  grid.add(username, 1, 0);
		  grid.add(new Label("Password:"), 0, 1);
		  grid.add(password, 1, 1);

		  // Enable/Disable login button depending on whether a username was entered.
		  Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		  loginButton.setDisable(true);

		  // Do some validation (using the Java 8 lambda syntax).
		  username.textProperty().addListener((observable, oldValue, newValue) -> {
		      loginButton.setDisable(newValue.trim().isEmpty());
		  });

		  dialog.getDialogPane().setContent(grid);

		  // Request focus on the username field by default.
		  Platform.runLater(() -> username.requestFocus());

		  // Convert the result to a username-password-pair when the login button is clicked.
		  dialog.setResultConverter(dialogButton -> {
		      if (dialogButton == loginButtonType) {
		          return new Pair<>(username.getText(), password.getText());
		      }
		      return null;
		  });

		  Optional<Pair<String, String>> result = dialog.showAndWait();

		  result.ifPresent(usernamePassword -> {
			  mTrader = new Trader(usernamePassword.getKey(), usernamePassword.getValue().hashCode());
		  });

		  if (!result.isPresent()){
			  Platform.exit();
			  System.exit(0);
		  }

	}

	private void showSignUpDialog(){

	  	  // Create the custom dialog.
		  Dialog<Trader> dialog = new Dialog<Trader>();

		  dialog.setTitle("Sign Up");
		  dialog.setHeaderText("Choose your username and password and fill in your personal details:");

		  // Set the button types.
		  ButtonType loginButtonType = new ButtonType("Done", ButtonData.OK_DONE);
		  dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		  // Create the info labels and fields.
		  GridPane grid = new GridPane();
		  grid.setHgap(10);
		  grid.setVgap(10);
		  grid.setPadding(new Insets(20, 150, 10, 10));

		  TextField username = new TextField();
		  username.setPromptText("Username");
		  PasswordField password = new PasswordField();
		  password.setPromptText("Password");
		  TextField firstName = new TextField();
		  firstName.setPromptText("First Name");
		  TextField lastName = new TextField();
		  lastName.setPromptText("Last Name");
		  TextField bankSecret = new TextField();
		  bankSecret.setPromptText("Bank Secret");
		  TextField bankAccountId = new TextField();
		  bankAccountId.setPromptText("Bank Account ID");
		  TextField email = new TextField();
		  email.setPromptText("Email");
		  TextField phone = new TextField();
		  phone.setPromptText("Phone");

		  grid.add(new Label("* Username:"), 0, 0);
		  grid.add(username, 1, 0);
		  grid.add(new Label("* Password:"), 0, 1);
		  grid.add(password, 1, 1);
		  grid.add(new Label("* First Name:"), 0, 2);
		  grid.add(firstName, 1, 2);
		  grid.add(new Label("* Last Name:"), 0, 3);
		  grid.add(lastName, 1, 3);
		  grid.add(new Label("* Bank Secret:"), 0, 4);
		  grid.add(bankSecret, 1, 4);
		  grid.add(new Label("* Bank Account ID:"), 0, 5);
		  grid.add(bankAccountId, 1, 5);
		  grid.add(new Label("  Email:"), 0, 6);
		  grid.add(email, 1, 6);
		  grid.add(new Label("  Phone:"), 0, 7);
		  grid.add(phone, 1, 7);

		  // Enable/Disable login button depending on whether necessary details were entered.
		  Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		  loginButton.setDisable(true);

		  // Do some validation (using the Java 8 lambda syntax).
		  username.textProperty().addListener((observable, oldValue, newValue) -> {
		      loginButton.setDisable(newValue.trim().isEmpty() || password.getText().isEmpty()
		    		  || firstName.getText().isEmpty() || lastName.getText().isEmpty()
		    		  || bankSecret.getText().isEmpty() || bankAccountId.getText().isEmpty());
		  });
		  password.textProperty().addListener((observable, oldValue, newValue) -> {
			  loginButton.setDisable(newValue.trim().isEmpty() || username.getText().isEmpty()
					  || firstName.getText().isEmpty() || lastName.getText().isEmpty()
					  || bankSecret.getText().isEmpty() || bankAccountId.getText().isEmpty());
		  });
		  firstName.textProperty().addListener((observable, oldValue, newValue) -> {
			  loginButton.setDisable(newValue.trim().isEmpty() || username.getText().isEmpty()
					  || password.getText().isEmpty() || lastName.getText().isEmpty()
					  || bankSecret.getText().isEmpty() || bankAccountId.getText().isEmpty());
		  });
		  lastName.textProperty().addListener((observable, oldValue, newValue) -> {
			  loginButton.setDisable(newValue.trim().isEmpty() || username.getText().isEmpty()
					  || password.getText().isEmpty() || firstName.getText().isEmpty()
					  || bankSecret.getText().isEmpty() || bankAccountId.getText().isEmpty());;
		  });
		  bankSecret.textProperty().addListener((observable, oldValue, newValue) -> {
			  loginButton.setDisable(newValue.trim().isEmpty() || username.getText().isEmpty()
					  || password.getText().isEmpty() || firstName.getText().isEmpty()
					  || lastName.getText().isEmpty() || bankAccountId.getText().isEmpty());;
		  });
		  bankAccountId.textProperty().addListener((observable, oldValue, newValue) -> {
			  loginButton.setDisable(newValue.trim().isEmpty() || username.getText().isEmpty()
					  || password.getText().isEmpty() || firstName.getText().isEmpty()
					  || lastName.getText().isEmpty() || bankSecret.getText().isEmpty());;
		  });

		  dialog.getDialogPane().setContent(grid);

		  // Request focus on the username field by default.
		  Platform.runLater(() -> username.requestFocus());

		  // Convert the result to Trader when the login button is clicked.
		  dialog.setResultConverter(dialogButton -> {
		      if (dialogButton == loginButtonType) {
		    	  return new Trader(username.getText(), password.getText().hashCode(),
		    			  firstName.getText(), lastName.getText(), bankSecret.getText(),
		    			  bankAccountId.getText(), email.getText(), phone.getText());
		      }
		      return null;
		  });

		  Optional<Trader> result = dialog.showAndWait();

		  result.ifPresent(trader -> {
			  mTrader = trader;
		  });

		  if (!result.isPresent()){
			  Platform.exit();
			  System.exit(0);
		  }

	}


	private void showMainUI(Stage primaryStage){

		// Setting up the menu pane:

		final int menuComponentsWidth = 240;
		final int menuTitleHeight = 80;
		final int menuButtonHeight = 40;

		Label menuTitle = new Label("Menu");
		menuTitle.setMinSize(menuComponentsWidth, menuTitleHeight);
		menuTitle.setAlignment(Pos.CENTER);
		menuTitle.setFont(new Font("Arial", 30));

		Button purchaseTab = new Button("Purchase");
		purchaseTab.setMinSize(menuComponentsWidth, menuButtonHeight);
		Button sellTab = new Button("Sell");
		sellTab.setMinSize(menuComponentsWidth, menuButtonHeight);
		Button portfolioManagementTab = new Button("Portfolio Management");
		portfolioManagementTab.setMinSize(menuComponentsWidth, menuButtonHeight);
		Button ViewCurrentTab = new Button("View Current");
		ViewCurrentTab.setMinSize(menuComponentsWidth, menuButtonHeight);

		VBox menuPane = new VBox();
		menuPane.setSpacing(15);
		menuPane.setStyle("-fx-border-color: orange; -fx-border-radius: 4.0; -fx-border-width: 3px;");
		menuPane.setPadding(new Insets(10,20,30,20));
		menuPane.getChildren().addAll(menuTitle, purchaseTab, sellTab, portfolioManagementTab, ViewCurrentTab);


		// Setting up the major pane (right pane) for the first time:
		Label initialGuidance = new Label("Use the Menu to choose an activity.\n\n"
				+ "Note that the Menu will always be there for you\nto switch between screens.");
		initialGuidance.setAlignment(Pos.CENTER);
		initialGuidance.setFont(new Font("Arial", 18));
		mActivityPane = new ActivityPane();
		mActivityPane.setCenter(initialGuidance);


		// Setting up the main pain:
	    BorderPane mainPane = new BorderPane();
	    mainPane.setMinHeight(420);
	    mainPane.setMaxHeight(420);
	    mainPane.setCenter(menuPane);
	    mainPane.setRight(mActivityPane);

	    // Create a scene and place it in the stage:
	    Scene scene = new Scene(mainPane);
	    primaryStage.setTitle("Trader: " + mTrader.getUsername()); // Set the stage title
	    primaryStage.setScene(scene); // Place the scene in the stage
	    primaryStage.show(); // Display the stage
	    primaryStage.setResizable(false);
	    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	    	public void handle(WindowEvent event) {
	    		Platform.exit();
	    		System.exit(0);
	    	}
	    });


	    // Request focus on the menuPane field by default.
		Platform.runLater(() -> menuPane.requestFocus());


	    // Setting up the action listeners for the menu buttons:

	    purchaseTab.setOnAction(e -> {
	    	Platform.runLater(() -> menuPane.requestFocus());
	    	purchaseTab.setStyle("-fx-base: Orange;");
	    	sellTab.setStyle("");
	    	portfolioManagementTab.setStyle("");
	    	ViewCurrentTab.setStyle("");
	    	mainPane.getChildren().remove(mActivityPane);
			mActivityPane = new PurchasePane();
	    	mainPane.setRight(mActivityPane);
	    });

	    sellTab.setOnAction(e -> {
	    	Platform.runLater(() -> menuPane.requestFocus());
	    	purchaseTab.setStyle("");
	    	sellTab.setStyle("-fx-base: Orange;");
	    	portfolioManagementTab.setStyle("");
	    	ViewCurrentTab.setStyle("");
	    	mainPane.getChildren().remove(mActivityPane);
    		mActivityPane = new SellPane();
    		mainPane.setRight(mActivityPane);
	    });

	    portfolioManagementTab.setOnAction(e -> {
	    	Platform.runLater(() -> menuPane.requestFocus());
	    	purchaseTab.setStyle("");
	    	sellTab.setStyle("");
	    	portfolioManagementTab.setStyle("-fx-base: Orange;");
	    	ViewCurrentTab.setStyle("");
	    	mainPane.getChildren().remove(mActivityPane);
    		mActivityPane = new PortfolioManagementPane();
    		mainPane.setRight(mActivityPane);
	    });

	    ViewCurrentTab.setOnAction(e -> {
	    	Platform.runLater(() -> menuPane.requestFocus());
	    	purchaseTab.setStyle("");
	    	sellTab.setStyle("");
	    	portfolioManagementTab.setStyle("");
	    	ViewCurrentTab.setStyle("-fx-base: Orange;");
	    	mainPane.getChildren().remove(mActivityPane);
    		mActivityPane = new ViewCurrentPane();
    		mainPane.setRight(mActivityPane);
	    });

	}


}


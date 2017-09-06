package logic;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import auth.api.WrongSecretException;
import bank.api.BankManager;
import bank.api.DoesNotHaveThisAssetException;
import bank.api.InternalServerErrorException;
import bank.api.NotEnoughAssetException;
import exchange.api.DoesNotHaveThisStockException;
import exchange.api.ExchangeManager;
import exchange.api.InternalExchangeErrorException;
import exchange.api.NoSuchAccountException;
import exchange.api.NotEnoughMoneyException;
import exchange.api.NotEnoughStockException;
import exchange.api.StockNotTradedException;
import storage.DatabaseConnection;


/**
 * This class was implemented using the Singleton pattern,<br>
 * meaning, there will only be one instance of this class<br>
 * for the entire running of the application.
 */
public class AppLogic {

	// This is the reference to the single instance of the AppData class:
	private static AppLogic mAppData = null;

	// A private constructor that is only called one time:
	private AppLogic(){}

	/** A public method to make the app's data available throughout the application. */
	public static AppLogic getAppData() {
		if(mAppData == null){
			mAppData = new AppLogic();
		}
		return mAppData;
	}

	// Lock for synchronization:
	private static Object lock = new Object();

	// Current logged-in trader's username and bank details:
	private static String mUsername;
	private static String mBankSecret;
	private static int mBankAccountId;


	/**
	 * This method is used only once, as soon as the system starts.<br>
	 * It will open a thread that will constantly go over opened deals<br>
	 * and check rather they have been closed already, in order to update the system's database<br>
	 * and transfer money/stocks from the stock market's bank account to the trader's bank account.<br>
	 * this thread will work for the sake of all users of this platform,<br>
	 * and not just for the currently logged-in trader.
	 */
	private static void activateOrdersFollowUp() {

		new Thread(()-> {

			while(true) {

				// Each time, wait 4 seconds before checking again:
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				synchronized(lock) {

					try {

						ExchangeManager exchange = (ExchangeManager) Naming.lookup(Constants.EXCHANGE_URL);

						Statement stmt = DatabaseConnection.getConnection().createStatement();

						ArrayList<Integer> recentlyClosedOrderIDs = new ArrayList<Integer>();

						ArrayList<String> recentlyPurchasedStockNames = new ArrayList<String>();
						ArrayList<Integer> recentlyPurchasedStockPrices = new ArrayList<Integer>();

						// Going through all opened deals in our database:
						ResultSet rset = stmt.executeQuery("select orderCode, stockName, amount, price, transactionType "
								+ "from Transaction where transactionState = '" + transactionState.OPENED.toString() + "';");
						while (rset.next()){

							int orderID = rset.getInt(1);
							String stockName = rset.getString(2);
							int amount = rset.getInt(3);
							int price = rset.getInt(4);
							String tp = rset.getString(5);

							boolean stillOpened = false;

							// Going through all open deals in the stock market:
							List<Integer> openOrderIDs = exchange.getOpenOrders(mBankSecret, mBankAccountId);
							for (Integer id : openOrderIDs) {
								if(id == orderID) {
									stillOpened = true;
								}
							}

							// If deal is finally closed:
							if(!stillOpened) {

								// Adding the closed deal to the array list:
								recentlyClosedOrderIDs.add(orderID);

								// if the closed deal was a purchase deal:
								if(tp.equals(transactionType.BID.toString())){
									// Adding the newly purchased stocks to the array lists:
									for(int i=0; i < amount; i++){
										recentlyPurchasedStockNames.add(stockName);
										recentlyPurchasedStockPrices.add(price);
									}
								}

							}

						}

						String currentDate = mAppData.dateToMYSQLFormat(new Date());
						int totalAccountValue = mAppData.getTotalAccountValue();

						// Going through all finally-closed deals:
						for(int i = 0; i < recentlyClosedOrderIDs.size(); i++){

							// Updating database:
							stmt.executeUpdate("update Transaction set transactionState = '"
									+ transactionState.CLOSED.toString()
									+ "', closingDate = '" + currentDate
									+ "', pointBasedAccountValue = " + totalAccountValue
									+ " where orderCode = " + recentlyClosedOrderIDs.get(i) + ";");

						}

						// Going through all finally closed purchase deals in particular:
						for(int i = 0; i < recentlyPurchasedStockNames.size(); i++){
							// Adding the new purchased stocks into the PurchasedAndOwnedStocks in database:
							stmt.executeUpdate("insert into PurchasedAndOwnedStocks (traderUsername, stockName, price)"
			    	    			+ " values ('" + mUsername + "', '" + recentlyPurchasedStockNames.get(i)
			    	    			+ "', " + recentlyPurchasedStockPrices.get(i) + ")");
						}


						// Update the trader's yield:

						ArrayList<String> dates = new ArrayList<String>();
						ArrayList<Float> accountValues = new ArrayList<Float>();

						// Going through all trader's closed deals in our database:
						rset = stmt.executeQuery("select openingDate, pointBasedAccountValue from Transaction "
								+ "where transactionState = '" + transactionState.CLOSED.toString()
								+ "' and traderUsername = '" + mUsername + "';");
						while (rset.next()) {
							dates.add(rset.getString(1));
							accountValues.add((float)rset.getInt(2));
						}

						float yieldNumerator = 0;
						float totalTimeLapse = 0;

						for (int i = 0; i < dates.size()-1; i++) {
							rset = stmt.executeQuery("SELECT TIMESTAMPDIFF(SECOND, '" + dates.get(i) + "', '" + dates.get(i+1)+"')");
							rset.next();
							int timeLapse = rset.getInt(1);
							totalTimeLapse+= timeLapse;
							float gapPercentage = (accountValues.get(i+1) - accountValues.get(i)) / accountValues.get(i);
							yieldNumerator+= gapPercentage*timeLapse;
						}

						if(totalTimeLapse != 0){

							String generalYield = new DecimalFormat("##.##").format((yieldNumerator/totalTimeLapse)*100) + "%";
							stmt.executeUpdate("update Trader set accountYield = '" + generalYield
									+ "' where username = '" + mUsername + "';");

						}

					} catch (SQLException e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (NotBoundException e) {
						e.printStackTrace();
					} catch (WrongSecretException e) {
						e.printStackTrace();
					} catch (NoSuchAccountException e) {
						e.printStackTrace();
					} catch (InternalExchangeErrorException e) {
						e.printStackTrace();
					}

				}

			}

		}).start();

	}


	/**
	 * This method is used for user authentication.
	 */
	public AuthenticationEvents.eventType authenticateUser(AuthenticationEvents.eventType recievedET, Trader trader) {

		synchronized(lock) {

			AuthenticationEvents.eventType resultET = null;

			try {

				Statement stmt = DatabaseConnection.getConnection().createStatement();

				// Sign up:
				if (recievedET.equals(AuthenticationEvents.eventType.SIGNUP_ATTEMPT)){

					// Making sure username is not occupied:
					ResultSet rset = stmt.executeQuery("select username from Trader");
		    	    while (rset.next()){
		    	    	String exsistingUserName = rset.getString(1);
		        	    if (exsistingUserName.equals(trader.getUsername()))
		        	    	resultET = AuthenticationEvents.eventType.NAME_OCCUPIED;
		    	    }

		    	    if (resultET!=AuthenticationEvents.eventType.NAME_OCCUPIED){
		    	    	// Making sure bank details are correct:
		    	    	try {

							BankManager bank = (BankManager) Naming.lookup(Constants.BANK_URL);
							bank.getAssets(trader.getBankSecret(), Integer.parseInt(trader.getBankAccountId()));

							// If we got here, bank details are indeed correct.
							// Updating the Trader table and granting connection:
							stmt.executeUpdate("insert into Trader (username, password, firstName, lastName, "
									+ "bankSecret, bankAccountId, email, phone) "
			    	    			+ "values ('" + trader.getUsername() + "', " + trader.getPasswordHash()
			    	    			+ ", '" + trader.getFirstName() + "', '" + trader.getLastName()
			    	    			+ "', '" + trader.getBankSecret() + "', '" + Integer.parseInt(trader.getBankAccountId())
			    	    			+ "', '" + trader.getEmail() + "', '" + trader.getPhone() + "')");
			    	    	resultET = AuthenticationEvents.eventType.CONNECTION_GRANTED;
			    	    	mUsername = trader.getUsername();
			    	    	mBankSecret = trader.getBankSecret();
			    	    	mBankAccountId = Integer.parseInt(trader.getBankAccountId());
			    	    	activateOrdersFollowUp();

		    	    	} catch(NumberFormatException e) {
		    	    		resultET = AuthenticationEvents.eventType.SECRET_DENIED;
		    	        } catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (NotBoundException e) {
							e.printStackTrace();
						} catch (WrongSecretException e) {
							resultET = AuthenticationEvents.eventType.SECRET_DENIED;
						} catch (InternalServerErrorException e) {
							e.printStackTrace();
						}

		    	    }
				}

				// Login:
				else if (recievedET.equals(AuthenticationEvents.eventType.LOGIN_ATTEMPT)){

					// Making sure that the username and password match with an already-existing user in our database:
					ResultSet rset = stmt.executeQuery("select username, password, bankSecret, bankAccountId from Trader");
		    	    while (rset.next()){
		    	    	String exsistingUserName = rset.getString(1);
		        	    int exsistingPassword = rset.getInt(2);
		        	    if (exsistingUserName.equals(trader.getUsername())){
		        	    	if (exsistingPassword==trader.getPasswordHash()) {
		        	    		// Granting connection:
		        	    		resultET = AuthenticationEvents.eventType.CONNECTION_GRANTED;
		        	    		mUsername = trader.getUsername();
		        	    		mBankSecret = rset.getString(3);
				    	    	mBankAccountId = rset.getInt(4);
				    	    	activateOrdersFollowUp();
		        	    	}
		        	    	else
		        	    		resultET = AuthenticationEvents.eventType.PASSWORD_DENIED;
		        	    }
		    	    }
		    	    if(resultET==null)
		    	    	resultET = AuthenticationEvents.eventType.USERNAME_NOT_FOUND;
				}

			}

			catch (SQLException e) {
				e.printStackTrace();
			}

			return resultET;

		}

	}


	/**
	 * This method is used for getting a map of the trader's bank account's assets<br>
	 * (stock names as keys, and the amount as value).
	 */
	public LinkedHashMap<String, Integer> getBankAccountAssets() {

		synchronized(lock) {

			LinkedHashMap<String, Integer> assets = new LinkedHashMap<String, Integer>();

			try {

				BankManager bank = (BankManager) Naming.lookup(Constants.BANK_URL);
				Set<String> assetNames = bank.getAssets(mBankSecret, mBankAccountId);
				for (String name : assetNames) {
					assets.put(name, bank.getQuantityOfAsset(mBankSecret, mBankAccountId, name));
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			} catch (WrongSecretException e) {
				e.printStackTrace();
			} catch (InternalServerErrorException e) {
				e.printStackTrace();
			} catch (DoesNotHaveThisAssetException e) {
				e.printStackTrace();
			}

			return assets;

		}

	}


	/**
	 * This method calculates and returns the current total worth of all assets in the trader's bank account.
	 */
	public int getTotalAccountValue() {

		int total = 0;

		try {

			BankManager bank = (BankManager) Naming.lookup(Constants.BANK_URL);
			ExchangeManager exchange = (ExchangeManager) Naming.lookup(Constants.EXCHANGE_URL);

			Set<String> assetNames = bank.getAssets(mBankSecret, mBankAccountId);
			for (String name : assetNames) {
				if(name.equals(Constants.CURRENCY_NAME)){
					total+= bank.getQuantityOfAsset(mBankSecret, mBankAccountId, name);
				}
				else {
					int quantity = bank.getQuantityOfAsset(mBankSecret, mBankAccountId, name);
					int maxDemandPrice = 0;
					Map<Integer, Integer> demand = exchange.getDemand(name);
					for(Map.Entry<Integer, Integer> quote : demand.entrySet()) {
						if (quote.getKey() > maxDemandPrice) maxDemandPrice = quote.getKey();
					}
					total+= maxDemandPrice*quantity;
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (WrongSecretException e) {
			e.printStackTrace();
		} catch (InternalServerErrorException e) {
			e.printStackTrace();
		} catch (DoesNotHaveThisAssetException e) {
			e.printStackTrace();
		}

		return total;

	}


	/**
	 * This method is used for getting the trader's general account yield.
	 */
	public String getAccountYield() {

		String yield = "";

		synchronized(lock) {

			try {
				Statement stmt = DatabaseConnection.getConnection().createStatement();
				ResultSet rset = stmt.executeQuery("select accountYield from Trader where username = '" + mUsername + "';");
				rset.next();
				yield = rset.getString(1);
			}

			catch (SQLException e) {
				e.printStackTrace();
			}

			return yield;

		}

	}


	/**
	 * This method is used for getting the entire Transaction table from the database.
	 */
	public ArrayList<Transaction> getAllTransactions() {

		synchronized(lock) {

			ArrayList<Transaction> transactions = new ArrayList<Transaction>();

			try {

				Statement stmt = DatabaseConnection.getConnection().createStatement();

				ResultSet rset = stmt.executeQuery("select openingDate, orderCode, stockName, amount, price, "
						+ "transactionType, transactionState, closingDate, profit from Transaction where traderUsername = '" + mUsername + "';");

				while (rset.next()){
					transactions.add(new Transaction(rset.getString(1), rset.getInt(2), rset.getString(3),
							rset.getInt(4), rset.getInt(5), rset.getString(6), rset.getString(7),
							rset.getString(8), rset.getInt(9)));
				}

			}

			catch (SQLException e) {
				e.printStackTrace();
			}

			return transactions;

		}

	}


	/**
	 * This method is used for getting an array of all the Stocks
	 * currently supplied or demanded by the stock market.
	 */
	public ArrayList<Stock> getStockSuppliesAndDemands() {

		synchronized(lock) {

			ArrayList<Stock> stocks = new ArrayList<Stock>();

			try {

				ExchangeManager exchange = (ExchangeManager) Naming.lookup(Constants.EXCHANGE_URL);

				for(String stockName : exchange.getStockNames()) {
					Map<Integer, Integer> supply = exchange.getSupply(stockName);
					for(Map.Entry<Integer, Integer> quote : supply.entrySet()) {
						stocks.add(new Stock(stockName, quote.getValue(), quote.getKey(), "Supply"));
					}
				}

				for(String stockName : exchange.getStockNames()) {
					Map<Integer, Integer> demand = exchange.getDemand(stockName);
					for(Map.Entry<Integer, Integer> quote : demand.entrySet()) {
						stocks.add(new Stock(stockName, quote.getValue(), quote.getKey(), "Demand"));
					}
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

			return stocks;

		}

	}


	/**
	 * This method is used for getting an array of all the Stocks supplied by the stock market.
	 */
	public ArrayList<Stock> getStockSupplies() {

		synchronized(lock) {

			ArrayList<Stock> supplies = new ArrayList<Stock>();

			try {

				ExchangeManager exchange = (ExchangeManager) Naming.lookup(Constants.EXCHANGE_URL);

				for(String stockName : exchange.getStockNames()) {
					Map<Integer, Integer> supply = exchange.getSupply(stockName);
					for(Map.Entry<Integer, Integer> quote : supply.entrySet()) {
						supplies.add(new Stock(stockName, quote.getValue(), quote.getKey(), "Supply"));
					}
				}

			}catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

			return supplies;

		}

	}


	/**
	 * This method is used for getting an array of all the Stocks demanded by the stock market.
	 */
	public ArrayList<Stock> getStockDemands() {

		synchronized(lock) {

			ArrayList<Stock> demands = new ArrayList<Stock>();

			try {

				ExchangeManager exchange = (ExchangeManager) Naming.lookup(Constants.EXCHANGE_URL);

				for(String stockName : exchange.getStockNames()) {
					Map<Integer, Integer> demand = exchange.getDemand(stockName);
					for(Map.Entry<Integer, Integer> quote : demand.entrySet()) {
						demands.add(new Stock(stockName, quote.getValue(), quote.getKey(), "Demand"));
					}
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

			return demands;

		}

	}


	/**
	 * This method is used for making a transaction.
	 */
	public TransactionEvents.eventType makeTransaction(String assetName, int amount, int price, TransactionEvents.eventType request) {

		synchronized(lock) {

			try {

				BankManager bank = (BankManager) Naming.lookup(Constants.BANK_URL);
				ExchangeManager exchange = (ExchangeManager) Naming.lookup(Constants.EXCHANGE_URL);

				// If its a bid (purchase) event:
				if(request == TransactionEvents.eventType.BID_ATTEMPT) {

					// Making sure there's enough money in trader's bank account:
					int moneyInAccount = bank.getQuantityOfAsset(mBankSecret, mBankAccountId, Constants.CURRENCY_NAME);

					// If there is not enough money:
					if(price*amount > moneyInAccount) {
						return TransactionEvents.eventType.NOT_ENOUGH_MONEY;
					}

					// Otherwise:
					else {

						// Saving current date (for later SQL query):
						String currentDate = dateToMYSQLFormat(new Date());

						// Transferring money from trader's bank account to the exchange bank account:
						System.out.println("Transferring money...");
						bank.transferAssets(mBankSecret, mBankAccountId, exchange.getExchangeBankAccountNumber(),
								Constants.CURRENCY_NAME, price*amount);

						// Trying to place the bid over and over again until the stock market allows it
						// (because it finally received the money from the trader's bank account):

						int orderID = 0;
						boolean hasPlaced = false;

						System.out.println("Trying to place the order...");
						while(!hasPlaced) {
							// Wait 1 second before each check:
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							// Trying to place the bid:
							try {
								orderID = exchange.placeBid(mBankSecret, mBankAccountId, assetName, amount, price);
								hasPlaced = true;
								System.out.println("Order successfully placed.");
							}
							catch (NoSuchAccountException | NotEnoughStockException | StockNotTradedException
									| NotEnoughMoneyException | InternalExchangeErrorException e) {
							}
						}

						// Iterating over the open orders to see if our deal has been opened:
						// Wait 2 seconds before checking (just in case):

						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

						// Updating the database:
						System.out.println("Updating database...");
						try {

							Statement stmt = DatabaseConnection.getConnection().createStatement();

							stmt.executeUpdate("insert into Transaction (openingDate, traderUsername, "
									+ "orderCode, stockName,  amount, price, transactionType, transactionState) "
					    			+ "values('" + currentDate + "', '" + mUsername + "', "
									+ orderID + ", '" + assetName + "', " + amount + ", " + price
					    			+ ", '" + transactionType.BID.toString() + "', '" + transactionState.OPENED.toString() + "')");

						}
						catch (SQLException e) {
							e.printStackTrace();
						}

						System.out.println("Process completed!\n___________________________\n");
						return TransactionEvents.eventType.DEAL_OPENED;

					}

				}

				// If its an ask (sell) event:
				else if(request == TransactionEvents.eventType.ASK_ATTEMPT) {

					// Making sure there are enough assets in trader's bank account:
					Set<String> stockMarketAssetNames = bank.getAssets(mBankSecret, mBankAccountId);
					for (String name : stockMarketAssetNames) {
						if (name.equals(assetName)){

							// If there are not enough assets:
							if(amount > bank.getQuantityOfAsset(mBankSecret, mBankAccountId, name)){
								return TransactionEvents.eventType.NOT_ENOUGH_ASSETS;
							}

							// Otherwise:
							else {

								// Saving current date (for later SQL query):
								String currentDate = dateToMYSQLFormat(new Date());

								// Transferring assets from trader's bank account to the exchange bank account:
								System.out.println("Transferring stocks...");
								bank.transferAssets(mBankSecret, mBankAccountId, exchange.getExchangeBankAccountNumber(),
										assetName, amount);

								// Trying to place the ask over and over again until the stock market allows it
								// (because it finally received the stocks from the trader's bank account):

								int orderID = 0;
								boolean hasPlaced = false;

								System.out.println("Trying to place the order...");
								while(!hasPlaced) {
									// Wait 1 second before each check:
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									// Trying to place the ask:
									try {
										orderID = exchange.placeAsk(mBankSecret, mBankAccountId, assetName, amount, price);
										hasPlaced = true;
										System.out.println("Order was successfuly placed.");
									} catch (NotEnoughStockException | StockNotTradedException
											| DoesNotHaveThisStockException e) {
									}
								}

								// Iterating over the open orders to see if our deal has been opened:
								// Wait 2 seconds before checking (just in case):

								try {
									Thread.sleep(2000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}

								// Updating the database:
								System.out.println("Updating database...");
								try {

									Statement stmt = DatabaseConnection.getConnection().createStatement();

									int profit = 0;

									ResultSet rset = stmt.executeQuery("select count(*) from PurchasedAndOwnedStocks;");
									rset.next();
									int purchasedAndOwnedAmount = rset.getInt(1);

									if(purchasedAndOwnedAmount <= amount) {

										int giftedStocksAmount = amount - purchasedAndOwnedAmount;
										profit+= giftedStocksAmount*price;

										rset = stmt.executeQuery("select sum(price) from PurchasedAndOwnedStocks;");
										rset.next();
										profit+= price*purchasedAndOwnedAmount - rset.getInt(1);

										stmt.executeUpdate("DELETE FROM PurchasedAndOwnedStocks;");

									}

									else {

										ResultSet rs = stmt.executeQuery("select sum(price) from "
												+ "(SELECT * FROM PurchasedAndOwnedStocks where traderUsername = '" + mUsername
												+ "' limit 0, " + amount + ") as stock_table;");
										rs.next();
										profit+= price*amount - rs.getInt(1);

										stmt.executeUpdate("DELETE FROM PurchasedAndOwnedStocks "
												+ "where traderUsername = '" + mUsername + "' limit " + amount + ";");

									}

									// Updating the Transaction table:
									stmt.executeUpdate("insert into Transaction (openingDate, traderUsername, "
											+ "orderCode, stockName, amount, price, transactionType, transactionState, "
							    			+ "profit) values('" + currentDate + "', '" + mUsername + "', "
											+ orderID + ", '" + assetName + "', " + amount + ", " + price
							    			+ ", '" + transactionType.ASK.toString() + "', '" + transactionState.OPENED.toString()
							    			+ "', " + profit + ")");

								}
								catch (SQLException e) {
									e.printStackTrace();
								}

								System.out.println("Process completed!\n___________________________\n");
								return TransactionEvents.eventType.DEAL_OPENED;

							}

						}
					}

				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			} catch (WrongSecretException e) {
				e.printStackTrace();
			} catch (InternalServerErrorException e) {
				e.printStackTrace();
			} catch (DoesNotHaveThisAssetException e) {
				e.printStackTrace();
			} catch (NotEnoughAssetException e) {
				e.printStackTrace();
			} catch (NoSuchAccountException e) {
				e.printStackTrace();
			} catch (InternalExchangeErrorException e) {
				e.printStackTrace();
			}

			return null;

		}

	}


	/**
	 * gets Date in java Date format and returns it in String
	 * @param date	- the actual date in java Date format
	 * @return time and date in String format adjusted to MySQL
	 */
	private String dateToMYSQLFormat(Date date) {
		java.text.SimpleDateFormat sdf =  new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = sdf.format(date);
		return currentTime;
	}


}

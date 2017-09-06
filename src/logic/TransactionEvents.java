package logic;

public interface TransactionEvents {
	enum eventType{
		// Ask:
		ASK_ATTEMPT, NOT_ENOUGH_MONEY,
		// Put:
		BID_ATTEMPT, NOT_ENOUGH_ASSETS,
		// General:
		DEAL_OPENED
	}
}

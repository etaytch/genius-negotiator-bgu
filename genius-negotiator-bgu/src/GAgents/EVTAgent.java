package GAgents;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.Agent;
import negotiator.ContinuousTimeline;
import negotiator.DiscreteTimeline;
import negotiator.Global;
import negotiator.NegotiationOutcome;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EVTAgent extends Agent {
	
	
	
	Vector<Action> opponentActiones;
	private static double MINIMUM_BID_UTILITY = 0.0;
	private Action actionOfPartner;
	private double progressFactor;
	
	
	/*
	 * Set initial parameters for all sessions
	 */
	@Override
	public void init() {
		actionOfPartner=null;
		MINIMUM_BID_UTILITY = utilitySpace.getReservationValueUndiscounted(); 
		opponentActiones = new Vector<Action>();
		progressFactor = 0;
	}

	/*
	 * Set initial parameters for a specific session.
	 */
	@Override
	public void beginSession(int sessionNumber){
		
	}
	
	/*
	 * Recives the last bid that made in the previous session (this is how you'll know that the session is over, and how does it ends)
	 * If LastAction is null, the last session has ended due to a TimeOut or other general error.
	 */
	@Override
	public void endSession(Action LastAction){
		
	}

	@Override
	public Action chooseAction() {
		Action action = null;
		try 
		{ 
			if(actionOfPartner==null) action = chooseInitialAction();
			if(actionOfPartner instanceof Offer)
			{
				Bid partnerBid = ((Offer)actionOfPartner).getBid();
				double offeredUtilFromOpponent = getUtility(partnerBid);
				// get current time
				double time = timeline.getTime();
				action = chooseRandomBidAction();
				
				Bid myBid = ((Offer) action).getBid();
				double myOfferedUtil = getUtility(myBid);
				
				// accept under certain circumstances
				if (isAcceptable(offeredUtilFromOpponent, myOfferedUtil, time))
					action = new Accept(getAgentID());
			}
			sleep(0.005); // just for fun
		} catch (Exception e) { 
			System.out.println("Exception in ChooseAction:"+e.getMessage());
			action=new Accept(getAgentID()); // best guess if things go wrong. 
		}
		return action;
	}
	
	
	private chooseInitialAction()
	{
	
	}
	
	@Override
	public void ReceiveMessage(Action opponentAction)
	{
		actionOfPartner = opponentAction;
	}
	
	@Override
	public String getName() {
		return "EVT Agent";
	}
	
	public static String getVersion() { 
		return "1.0";
	}
	
}

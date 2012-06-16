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

public class EVTAgent extends Agent {
	
	/*
	 * Set initial parameters for all sessions
	 */
	@Override
	public void init() {
		
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
		return null;
	}
	
	@Override
	public void ReceiveMessage(Action opponentAction)
	{
		
	}
	
	@Override
	public String getName() {
		return "";
	}
	
	public static String getVersion() { 
		return "1.0";
	}
	
}

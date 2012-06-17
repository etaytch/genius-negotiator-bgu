package GAgents;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.utility.EVALUATORTYPE;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.Agent;
import negotiator.ContinuousTimeline;
import negotiator.DiscreteTimeline;
import negotiator.Global;
import negotiator.NegotiationOutcome;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
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
		try {
			initStates();
		} catch (Exception e) {
 			e.printStackTrace();
		}		
		
	}

	public void initStates() throws Exception
	{
		HashMap<Integer, Value> values = new HashMap<Integer, Value>(); // pairs <issuenumber,chosen value string>
		ArrayList<Issue> issues=utilitySpace.getDomain().getIssues();
		Random randomnr= new Random();

		// create a random bid with utility>MINIMUM_BID_UTIL.
		// note that this may never succeed if you set MINIMUM too high!!!
		// in that case we will search for a bid till the time is up (3 minutes)
		// but this is just a simple agent.
		Bid bid=null;
		do 
		{
			for(Issue lIssue:issues) 
			{
				switch(lIssue.getType()) {
				case DISCRETE:
					IssueDiscrete lIssueDiscrete = (IssueDiscrete)lIssue;
					int optionIndex=randomnr.nextInt(lIssueDiscrete.getNumberOfValues());
					values.put(lIssue.getNumber(), lIssueDiscrete.getValue(optionIndex));
					break;
				case REAL:
					IssueReal lIssueReal = (IssueReal)lIssue;
					int optionInd = randomnr.nextInt(lIssueReal.getNumberOfDiscretizationSteps()-1);
					values.put(lIssueReal.getNumber(), new ValueReal(lIssueReal.getLowerBound() + (lIssueReal.getUpperBound()-lIssueReal.getLowerBound())*(double)(optionInd)/(double)(lIssueReal.getNumberOfDiscretizationSteps())));
					break;
				case INTEGER:
					IssueInteger lIssueInteger = (IssueInteger)lIssue;
					int optionIndex2 = lIssueInteger.getLowerBound() + randomnr.nextInt(lIssueInteger.getUpperBound()-lIssueInteger.getLowerBound());
					values.put(lIssueInteger.getNumber(), new ValueInteger(optionIndex2));
					break;
				default: throw new Exception("issue type "+lIssue.getType()+" not supported by SimpleAgent2");
				}
			}
			bid=new Bid(utilitySpace.getDomain(),values);
		} while (getUtility(bid) < MINIMUM_BID_UTILITY);

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
		/*
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
		*/
		return action;
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

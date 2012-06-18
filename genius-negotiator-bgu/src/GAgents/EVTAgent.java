package GAgents;

import misc.Pair;
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
import java.util.Collections;
import java.util.Comparator;
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
	private ArrayList<Pair<Bid,Double>> myBids;
	private ArrayList<Pair<Bid,Double>> opponetBids;
	
	
	
	/*
	 * Set initial parameters for all sessions
	 */
	@Override
	public void init() {
		actionOfPartner=null;
		MINIMUM_BID_UTILITY = utilitySpace.getReservationValueUndiscounted(); 
		opponentActiones = new Vector<Action>();
		progressFactor = 0;
		myBids = new ArrayList<Pair<Bid,Double>>();
		opponetBids = new ArrayList<Pair<Bid,Double>>();
		
		try {
			initStates();
		} catch (Exception e) {
 			e.printStackTrace();
		}		
		
	}

	public void initStates() throws Exception
	{
		Vector<Integer> issuesNumbers = new Vector<Integer>();
		Vector<Vector<Value>> issuesValues = new Vector<Vector<Value>>(); // pairs <issuenumber,chosen value string>
		
		ArrayList<Issue> issues=utilitySpace.getDomain().getIssues();
		HashMap<Integer,Integer> orientation = new HashMap<Integer,Integer>();
 		int i = 0;
		for (Issue lIssue:issues)
		{
			issuesNumbers.add(lIssue.getNumber());
			issuesValues.add(new Vector<Value>());
			orientation.put(lIssue.getNumber(),i);
			i++;
		}

		Bid bid=null;
		
		for(Issue lIssue:issues) 
		{
			IssueDiscrete lIssueDiscrete = (IssueDiscrete)lIssue;
			int vals = lIssueDiscrete.getNumberOfValues();
			for (int val=0; val<vals; val++)
			{
				issuesValues.get(orientation.get(lIssue.getNumber())).add(lIssueDiscrete.getValue(val));
			}
		}
		System.out.println("now we have it all! Lets Build the Bids!");
		//HashMap<Integer, Value> bidHash= new HashMap<Integer, Value>();
		Vector<Integer> bidIssues= new Vector<Integer>();
		Vector<Value> bidValues= new Vector<Value>();
		buildBids(issuesNumbers,issuesValues,bidIssues,bidValues);
		Collections.sort(myBids, new Comparator<Pair<Bid,Double>>(){  
		    		public int compare(Pair<Bid, Double> p1, Pair<Bid, Double> p2){   
		    			return p2.getSecond().compareTo(p1.getSecond());  
		    		}});
		System.out.println("Bids Vector is built!");
	}
	
	private void buildBids(Vector<Integer> issuesNumbers, Vector<Vector<Value>> issuesValues, 
								Vector<Integer> bidIssues ,Vector<Value> bidValues)
	{	
		if (issuesNumbers.size()==0)
		{
			HashMap<Integer, Value> bidHash= new HashMap<Integer, Value>();
			for(int i = 0; i<bidIssues.size(); i++)
			{
				bidHash.put(bidIssues.get(i), bidValues.get(i));
			}
			
			Bid bid = null;
			try {
				bid = new Bid(utilitySpace.getDomain(),bidHash);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Illigal BID!!!!!!");
			}
			this.myBids.add(new Pair<Bid, Double>(bid,getUtility(bid)));
			return;
		}
		
		if (issuesValues.size()==0){
			return;
		}
			
		Integer issue = issuesNumbers.remove(0);
		Vector<Value> valVec = issuesValues.remove(0);
		for(Value val : valVec)
		{
			bidIssues.add(issue);
			bidValues.add(val);
			buildBids(issuesNumbers, issuesValues, bidIssues ,bidValues);
			bidIssues.remove(issue);
			bidValues.remove(val);
		}
		issuesNumbers.add(0,issue);
		issuesValues.add(0,valVec);

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
				double time = timeline.getTime();
				action = chooseInitialAction();
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

	public Action chooseInitialAction()
	{
		return new Offer(getAgentID(), myBids.get(0).getFirst());
	}
	
	
	private boolean isAcceptable(double offeredUtilFromOpponent, double myOfferedUtil, double time) throws Exception
	{		
		if (offeredUtilFromOpponent>myOfferedUtil)
			return true;
		return false;
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

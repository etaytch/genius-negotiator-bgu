package GAgents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import misc.Pair;
import negotiator.Agent;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;



public class EVTAgent extends Agent {
	
	
	

	private Action actionOfPartner;
	private double compromiseRate;
	private double compromiseFact;
	private double ProgressDif;
	private double biasRate;
	private double biasFactor;
	private double sleepRate;
	private double noisRate;
	private double noisFact;
	private double compromiseLearn;
	private double progressLearn;
	private double noisLearn;
	private double df;
	private double reservationPanic;
	private int currentBidInx;
	private ArrayList<Pair<Bid,Double>> myBids;
	private Vector<ArrayList<Pair<Bid,Double>>> opponentBidsA;
	private Vector<ArrayList<Pair<Bid,Double>>> opponentBidsB;
	private int session;
	
	
	/*
	 * Set initial parameters for all sessions
	 */
	@Override
	public void init() { 
		
		session = -1;
		
		ProgressDif = 0;
		noisFact = 0;
		compromiseFact = 0;
		
		compromiseLearn = 20;
		progressLearn = 10;
		noisLearn = 0.2;
		
		reservationPanic = 0.2;
		myBids = new ArrayList<Pair<Bid,Double>>();
		opponentBidsA = new Vector<ArrayList<Pair<Bid,Double>>>();
		opponentBidsB = new Vector<ArrayList<Pair<Bid,Double>>>();

		df = utilitySpace.getDiscountFactor();
		if (df==0) df = 1; 

		try {
			initStates();
		} catch (Exception e) {
 			e.printStackTrace();
		}		
	}
	
	/*
	 * Set initial parameters for a specific session.
	 */
	@Override
	public void beginSession(int sessionNumber){
		session++;
		actionOfPartner=null;
		currentBidInx = 0;
		
		compromiseRate = 0;
		noisRate = 0;
		sleepRate = 0;
		biasRate = 1.2;
		
		ProgressDif = 0;
		noisFact = 0;
		compromiseFact = 0;
		
		compromiseFact+=compromiseLearn;
		compromiseFact = compromiseFact<0?0:compromiseFact;
		
		ProgressDif+=progressLearn;
		ProgressDif = ProgressDif<0?0:ProgressDif;
		
		noisFact+=noisLearn;
		noisFact = noisFact<0?0:noisFact;
		
		biasFactor = 1;
		
		ArrayList<Pair<Bid,Double>> l = new ArrayList<Pair<Bid,Double>>();
		opponentBidsA.add(l);
		opponentBidsB.add(l);
		
		
	}
	
	/*
	 * Recives the last bid that made in the previous session (this is how you'll know that the session is over, and how does it ends)
	 * If LastAction is null, the last session has ended due to a TimeOut or other general error.
	 */
	@Override
	public void endSession(Action LastAction){
		
		if (LastAction instanceof EndNegotiation)
		{
			EndNegotiation e = (EndNegotiation)LastAction;
			boolean isOpponent = e.getAgent().equals(this);
			double lastOfferedUtil = opponentBidsA.get(session).get(0).getSecond();
			double maxUtil = myBids.get(0).getSecond();
			double loss =  maxUtil-lastOfferedUtil;
			
			
			if (loss>0.4)
			{  
				return;
			}	
			else if (isOpponent)
			{
				progressLearn/=1.2;
				noisLearn/=1.2;
				compromiseLearn-=5;
				biasFactor*=2;
				return;
			}
			else if (!isOpponent)
			{
				progressLearn/=2;
				compromiseLearn-=10;
				biasFactor*=2;
				return;
			}
			
		}
		if (LastAction instanceof Accept)
		{
			double lastOfferedUtil = opponentBidsA.get(session).get(0).getSecond();
			double maxUtil = myBids.get(0).getSecond();
			double loss =  maxUtil-lastOfferedUtil;
			if (loss>0.5)
			{
				compromiseLearn+=20;
				progressLearn*=3;
				noisLearn*=3;
				biasFactor/=2;
			}
			else if (loss>0.3)
			{
				progressLearn*=2;
				noisLearn*=2;
				compromiseLearn+=10;
				biasFactor/=1.2;
			}
		}
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
				addToopponentBids(partnerBid,offeredUtilFromOpponent);
				double time = timeline.getTime();
				action = chooseAction(partnerBid,offeredUtilFromOpponent,time);
				if (!(action instanceof EndNegotiation))
				{
					
					biasRate = 1+(Math.pow(1-time,biasFactor)); 
					Bid myBid = ((Offer) action).getBid();
					double myOfferedUtil = getUtility(myBid);
					if (isAcceptable(offeredUtilFromOpponent, myOfferedUtil, time))
						action = new Accept(getAgentID());
				}				
			}
			sleep(sleepRate);
		} catch (Exception e) { 
			System.out.println("Exception in ChooseAction:"+e.getMessage());
			action=new Accept(getAgentID()); 
		}
		return action;
	}

	private void addToopponentBids(Bid partnerBid, double offeredUtilFromOpponent) {
		ArrayList<Pair<Bid, Double>>  l = opponentBidsA.get(session);
		Pair<Bid,Double>  p = new Pair<Bid,Double>(partnerBid,offeredUtilFromOpponent);
	    boolean flag = true;
		for (Pair<Bid,Double> inner : l)
	    {
	    	if (inner.getFirst().equals(partnerBid))
	    	{
	    		flag = false;
	    		break;
	    	}
	    }
		if (flag)
		{
			l.add(p);
		}
		
	}
	

	private Action chooseAction(Bid partnerBid, double offeredUtilFromOpponent,double time) 
	{	
		int oldBidinx =  currentBidInx;
		Action action = null;
		
		Collections.sort(opponentBidsA.get(session), new Comparator<Pair<Bid,Double>>(){  
    		public int compare(Pair<Bid, Double> p1, Pair<Bid, Double> p2){   
    			return p2.getSecond().compareTo(p1.getSecond());  
    		}});
		
	
		
		
		Double oppMaxBidEval = opponentBidsA.get(session).get(0).getSecond();	
		Double myMaxEval = myBids.get(currentBidInx).getSecond();
		
		//parameters reajustment
		/////////////////////////////////////////
		noisRate = Math.pow(time,noisFact); 
		double midEval = ((oppMaxBidEval*(compromiseRate))+ (myMaxEval*(1-compromiseRate)))*biasRate;
		Bid midBid = lookForBid(midEval,oldBidinx);
		if (midBid==null)
		{
			midBid = myBids.get(oldBidinx).getFirst();
		}
		if (df==1)
			compromiseRate = Math.pow(time,compromiseFact);
		else
			compromiseRate = Math.pow(Math.sqrt(time/Math.pow(df,time)),compromiseFact);
		if (compromiseRate>1) compromiseRate = 1;
		
		if (myBids.get(currentBidInx).getSecond()*Math.pow(df,time)-utilitySpace.getReservationValueUndiscounted()<reservationPanic)
		{
			compromiseFact/=2;
		}	
		////////////////////////////////////////
		
		
		
		action = new Offer(getAgentID(), midBid);
		if (myBids.get(currentBidInx).getSecond() < utilitySpace.getReservationValueUndiscounted())
		{
			action = new Offer(getAgentID(), myBids.get(oldBidinx).getFirst());
			currentBidInx = oldBidinx;
		}	
		
		if (myBids.get(currentBidInx).getSecond()*Math.pow(df,time) < utilitySpace.getReservationValueUndiscounted())
		{
			action = new EndNegotiation(getAgentID());
		}	
		
		if ((time>0.95)&&(opponentBidsA.get(session).size()<=2))
		{ 
			action = new EndNegotiation(getAgentID());
		}
		
		return action;
	}

	private Bid lookForBid(double midEval,int oldInx) {
		if (Math.random()>noisRate)
		{
			 currentBidInx = (int)(currentBidInx*Math.random());
			 return myBids.get(currentBidInx).getFirst();
		}
		for (Pair<Bid,Double> bid : myBids)
	    {
			 if (bid.getSecond()<= midEval)
			 {
				currentBidInx = myBids.indexOf(bid);
				if(currentBidInx>oldInx)
				{	
					compromiseFact+=ProgressDif*(opponentBidsA.size());		
				}
				return bid.getFirst();
			 }
	    }
		
		return null;
	}

	public Action chooseInitialAction()
	{
		return new Offer(getAgentID(), myBids.get(currentBidInx).getFirst());
	}
	
	
	private boolean isAcceptable(double offeredUtilFromOpponent, double myOfferedUtil, double time) throws Exception
	{		
		if (offeredUtilFromOpponent>=(0.95)*myOfferedUtil)
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
	
	/**
	 * 
	 * Build all
	 * 
	 * @throws Exception
	 */
	private void initStates() throws Exception
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
}




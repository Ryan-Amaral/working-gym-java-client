package sbbj_tpg;

import java.util.*;

public class Team implements Comparable<Team>
{
	// Static variable holding the next ID to be used for a new Team
	protected static long count = 0;
	
	// Unique ID of this Team
	protected long ID = 0;
	
	// Time step at which this Team was generated
	protected long birthday = 0;
	
	// An array list of Learners attached to this Team
	protected ArrayList<Learner> learners = new ArrayList<Learner>();
	
	// A mapping of String -> Double, where an input session is named by the String and the score is stored as a Double
	protected HashMap<String, Double> outcomes = new HashMap<String, Double>();
	
	// This key is used for sorting during Pareto calculations. They're in the SCM class.
	protected double key = 0;
	
	// The number of Learners currently referencing this Team
	protected int learnerReferenceCount = 0;
	
	public int genId; // Id of the team in current generation for parallelization purposes
		
	// Reconstruct a Team from primary data. WE'VE CREATED A MONSTER!
	public Team( long ID, long birthday, double key, ArrayList<Learner> learners, HashMap<String, Double> outcomes )
	{
		this.ID = ID;
		this.birthday = birthday;
		this.key = key;
		this.learners = learners;
		this.learnerReferenceCount = 0;
	}
	
	// Create a new Team and set its creation time
	public Team( long birthday )
	{
		this.birthday = birthday;
		this.learnerReferenceCount = 0;
		ID = count++;		
	}
	
	// The size of a Team is equal to the number of Learners attached to it
	public int size()
	{
		return learners.size();
	}
	
	public long getBirthday()
	{
		return birthday;
	}
	
	public long getID()
	{
		return ID;
	}
	
	public double getKey()
	{
		return key;
	}
	
	public void setKey( double key )
	{
		this.key = key;
	}
	
	// Add a Leaner to this Team. If the Learner is already a member, skip it and return false.
	public boolean addLearner( Learner learner )
	{
		// If we already have this learner stored, don't add it
		if( learners.contains(learner) )
			return false;
		
		// It's not already in there, so add it
		learners.add(learner);
		
		// Add successful; return true
		return true;	
	}
	
	// If a Learner is on this Team, remove it. Otherwise throw an error
	// because we never even attempt to remove a Learner that isn't attached.
	// This is basically a hold-over from a previous version and doesn't need to
	// work this way. C'est la vie. 
	public void removeLearner( Learner learner )
	{
		if( !learners.contains(learner) )
			throw new RuntimeException("The program tried to remove a Learner that does not exist.");
		
		learners.remove( learners.indexOf(learner) );
	}
	
	// Add this Team's Learners to a given list
	public ArrayList<Learner> getLearners()
	{
		return learners;
	}
	
	// Get the outcome for a data input session label. If we haven't done any learning on
	// that label, return false.
	public boolean getOutcome( String name, OpenDouble out )
	{
		// If we haven't tried this activity, return false.
		if( !outcomes.containsKey(name) )
			return false;
		
		// Otherwise get the score and store it in the out object
		out.setValue( outcomes.get(name) );
		
		// Return true if we returned a value in the out object
		return true;
	}
	
	// This Team is receiving a reward value. Store it in the outcomes map.
	// This version can't perform the same labelled activity more than once.
	// Change the map activity (or incoming label) to do something different.
	public void setOutcome( String name, Double out )
	{
		// We can't do the same activity more than once. If we do, throw an error.
		if( outcomes.containsKey(name) )  
			throw new RuntimeException("Tried to add a duplicate activity label to an outcomes map.");
		
		// If we haven't done this before, store it.
		outcomes.put(name, out);
	}
	
	// Remove a score from a Team's outcomes map. If it doesn't exist, that's bad. Something is very broken.
	public void deleteOutcome( String name )
	{
		// If we didn't do this activity, throw an error. This is a very bad thing. The code needs to be fixed.
		if( !outcomes.containsKey(name) )
			throw new RuntimeException("Tried to delete an activity label that does not exist from an outcomes map.");
		
		// If it does exist, remove it.
		outcomes.remove(name);
	}
	
	// Return the number of scores this Team has stored so far.
	public int numOutcomes()
	{
		return outcomes.size();
	}
	
	// Retrieve all outcomes along with the corresponding points
	public void outcomes( ArrayList<String> names, ArrayList<Double> scores )
	{		
		// For every name->score pair in the map, store the name and score in a list.
		// The lists are updated in-place in memory and don't need to be returned.
		for( String name : outcomes.keySet() )
		{
			names.add( name );
			scores.add( outcomes.get(name) );
		}
	}
	
	// Provide this Team with an input state set and return an action
	public long getAction( HashSet<Team> visited, double[] state, int rootTeamNum)
	{
		Learner bestLearner = null;
		double maxBid = 0;
		double nextBid = 0;

		// Add this Team to the visited set
		visited.add(this);
		
		// Create an integer for iteration
		int i = 0;
		
		// Get the first bid from the Learners based on their Action object
		for( i=0; i < learners.size(); i++ )
		{
			// Get the next Learner from the list
			bestLearner = learners.get(i);
			
			// If this Learner's Action is a Team and we've visited that Team before, skip this Learner
			if( !bestLearner.getActionObject().isAtomic() && visited.contains(bestLearner.getActionObject().team) )
				continue;
						
			// Otherwise we can get the Learner's bid
			maxBid = learners.get(i).bid(state, rootTeamNum, 0);

			// We've found our starting Learner, so break
			break;			
		}
				
		// Query the rest of the Learners to get the highest bid from the Learner pool
		for( i += 1 ; i < learners.size(); i++ )
		{
			// If this Learner's Action is a Team and we've visited that Team before, skip this Learner
			if( !learners.get(i).getActionObject().isAtomic() && visited.contains(learners.get(i).getActionObject().team) )
				continue;
			
			// Otherwise get the bid from this Learner
			nextBid = learners.get(i).bid( state, rootTeamNum, i);

			// If this bid is higher than the previous highest bid, store it and the Learner
			if( nextBid > maxBid )
			{
				maxBid = nextBid;
				bestLearner = learners.get(i);
			}
		}

		// Return the action of the best Learner
		return bestLearner.getActionObject().getAction(visited,state,rootTeamNum);
	}
	
	// This Team is being deleted. Make sure Learner references are decreased before it's gone!
	public void erase()
	{
		// For each Learner attached to this Team, reduce their number of references by 1.
		for( Learner learner : learners )
			learner.decreaseReferences();
	}
	
	// Increase the number of references to this Team and return the new value
	public int increaseReferences()
	{
		return ++learnerReferenceCount;
	}
	
	// Decrease the number of references to this Team and return the new value
	public int decreaseReferences()
	{
		return --learnerReferenceCount;
	}
	
	// Return the number of references to this Learner
	public int getReferences()
	{
		return learnerReferenceCount;
	}
	
	// Return a string representation of this Team
	public String toString()
	{
		String output = "ID " + ID + " Size " + learners.size();
		output += " Birthday " + birthday;
		
		for( Learner learner : learners )
		{
			output += " " + learner;
		}
		
		return output;
	}
	
	// Compare two Teams by key. 
	public int compareTo( Team other )
    {
    	double check = other.getKey() - getKey();
			
		if( check == 0.0 )
			return 0;
		else if( check < 0.0 )
			return 1;
		else
			return -1;
    }
	
	// Originally did a partial sort, but it's less complicated in Java to do
 	// a full sort because breaking down lists in Java takes too long.
 	public static void sortListByKey( List<Team> list )
 	{
 		Collections.sort(list, new Comparator<Team>() 
 		{
 			@Override
 			public int compare(Team o1, Team o2) 
 			{
 				double check = o2.getKey() - o1.getKey();
 				
 				if( check == 0.0 )
 					return 0;
 				else if( check < 0.0 )
 					return 1;
 				else
 					return -1;
 			}
 		} );
 	}
}

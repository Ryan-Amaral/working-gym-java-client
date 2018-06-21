package sbbj_tpg;

import java.util.*;

public class Miscellaneous {
	public static final double EPSILON = 1 / 100000; // 1e-5

	// Get Euclidean Distance Squared between two double arrays
	public static double euclideanDistSqrd(Double[] x, Double[] y, int dim) {
		double dist = 0;

		for (int i = 0; i < dim; i++)
			dist += (x[i] - y[i]) * (x[i] - y[i]);

		return dist;
	}

	// Get Euclidean Distance Squared between two double arraylists
	public static double euclideanDistSqrd(ArrayList<Double> x, ArrayList<Double> y) {
		return euclideanDistSqrd((Double[]) x.toArray(), (Double[]) y.toArray(), x.size());
	}

	// Get the Hamming distance between two integer arraylists
	public static int hammingDist(ArrayList<Integer> x, ArrayList<Integer> y) {
		int dist = 0;

		for (int i = 0; i < x.size(); i++)
			dist += x.get(i) == y.get(i) ? 0 : 1;

		return dist;
	}

	// Check if these two doubles are within EPSILON of each other
	public static boolean isEqual(double x, double y) {
		return Math.abs(x - y) < EPSILON;
	}

	// Check if these two doubles are within the given epsilon of each other
	public static boolean isEqual(double x, double y, double epsilon) {
		return Math.abs(x - y) < epsilon;
	}

	// Check if these two array lists have the same integers in the same indexes
	public static boolean isEqual(ArrayList<Integer> x, ArrayList<Integer> y) {
		if (x.size() != y.size())
			return false;

		for (int i = 0; i < x.size(); i++)
			if (x.get(i) != y.get(i))
				return false;

		return true;
	}

	// Check if these two array lists have the same doubles within epsilon
	public static boolean isEqual(ArrayList<Double> x, ArrayList<Double> y, double epsilon) {
		if (x.size() != y.size())
			return false;

		for (int i = 0; i < x.size(); i++)
			if (!isEqual(x.get(i), y.get(i), epsilon))
				return false;

		return true;
	}

	// Get the intersection of two Learner array lists
	public static ArrayList<Learner> intersectionLearner(ArrayList<Learner> first, ArrayList<Learner> second) {
		ArrayList<Learner> newList = new ArrayList<Learner>();

		for (Learner learner : first)
			if (second.contains(learner) && !newList.contains(learner))
				newList.add(learner);

		return newList;
	}

	// Get the difference of two Learner array lists
	public static ArrayList<Learner> symmetricDifferenceLearner(ArrayList<Learner> first, ArrayList<Learner> second) {
		ArrayList<Learner> newList = new ArrayList<Learner>();
		ArrayList<Learner> newList2 = null;

		newList.addAll(first);
		newList.addAll(second);

		newList2 = new ArrayList<Learner>(newList);

		for (Learner learner : newList) {
			if (first.contains(learner) && second.contains(learner)) {
				while (newList2.contains(learner))
					newList2.remove(learner);
			}
		}

		newList = new ArrayList<Learner>();

		for (Learner learner : newList2)
			if (!newList.contains(learner))
				newList.add(learner);

		return newList;
	}

	// Returns true if two input lists are exactly equal. That is, the elements are
	// the same and the cardinality is the same.
	public static boolean equalLists(List<Learner> one, List<Learner> two) {
		// If either list is null, return false. I don't play this game.
		if (one == null || two == null)
			return false;

		// If one of the lists is longer than the other, then return false because
		// they're not equal
		if (one.size() < two.size() || one.size() > two.size())
			return false;

		// If both lists are empty, return true. This is faster than doing the rest.
		if (one.isEmpty() && two.isEmpty())
			return true;

		// We first make a copy of the lists because Collections.sort() works in-place
		// (modifies the original list)
		one = new ArrayList<Learner>(one);
		two = new ArrayList<Learner>(two);

		// Sort the first list with a custom comparator
		Collections.sort(one, new Comparator<Learner>() {
			@Override
			public int compare(Learner a, Learner b) {
				return (a.ID == b.ID ? 0 : (a.ID < b.ID ? -1 : 1));
			}
		});

		// Sort the second list with a custom comparator
		Collections.sort(two, new Comparator<Learner>() {
			@Override
			public int compare(Learner a, Learner b) {
				return (a.ID == b.ID ? 0 : (a.ID < b.ID ? -1 : 1));
			}
		});

		// Return true if the lists are equal to each other
		return one.equals(two);
	}

	// Return true if there is an atomic action present in the list
	public static boolean containsAtomicAction(List<Learner> list) {
		// The same as checking the index range [0,size-1]
		return containsAtomicAction(list, list.size() - 1);
	}

	// Return true if there is an atomic action present in the index range [0,end]
	// of the list
	public static boolean containsAtomicAction(List<Learner> list, int end) {
		// An empty list (or no list at all) never has an atomic action.
		// If the end point goes beyond the range of the list, this is also false.
		if (list == null || list.isEmpty() || list.size() < end + 1)
			return false;

		// Check every Learner's action. If we find an atomic action, we can return
		// true.
		for (int i = 0; i < end + 1; i++)
			if (list.get(i).getActionObject().isAtomic())
				return true;

		// If no atomic actions were found, return false
		return false;
	}

	// Return true if there is an atomic action present in the index range [0,end]
	// of the list
	public static int countAtomicActions(List<Learner> list) {
		// An empty list (or no list at all) never has an atomic action.
		if (list == null || list.isEmpty())
			return 0;

		// Create a variable for storing the atomic action count
		int count = 0;

		// Check every Learner's action. If we find an atomic action, count increases by
		// 1.
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getActionObject().isAtomic())
				count++;

		// Return the number of atomic actions found
		return count;
	}

	public static boolean sortTeamsBySingleOutcome(ArrayList<Team> teams, HashMap<Team, ArrayList<Double>> outcomes) {
		// If either inputs are null, we can't sort anything
		if (teams == null || outcomes == null)
			return false;

		// If number of Teams is 0 or 1 it's already sorted
		if (teams.size() <= 1)
			return true;

		// Create a list to store the individual values
		ArrayList<Double> outcomeValues = new ArrayList<Double>();

		// Since we're working with single outcomes per Team, extract the single values
		// from the map
		for (int i = 0; i < teams.size(); i++) {
			outcomeValues.add(outcomes.get(teams.get(i)).get(0));
		}

		// Now do a shitty bubble sort! You can change this to something else, but for
		// 450 elements it's still fast and easy to write.
		// Want to know why I do this? Here:
		// 1. Sorting a map with built-in things sucks. We can use a Tree Map by taking
		// our outcomes map and swapping the key/value pair.
		// So in the hash map we take key->value and switch it in the tree map to
		// value->key. That would auto-sort it for us.
		// However, if two keys have the same value then one will get overwritten and
		// lost. Bad idea.
		// 2. We could sort using built-in list algorithms, but they'll never keep the
		// connection between key and value so that's out.
		// 3. I'm lazy and don't feel like writing a better sorting algorithm. Sue me.
		// This will probably happen twice per "generation"
		// and only really slows down if you're using thousands and thousands of Teams,
		// which rarely happens.
		Team tempTeam = null;
		double tempDouble = 0;

		for (int i = 0; i < outcomeValues.size(); i++) {
			for (int j = 1; j < (outcomeValues.size() - i); j++) {
				if ((tempDouble = outcomeValues.get(j - 1)) < outcomeValues.get(j)) {
					outcomeValues.set(j - 1, outcomeValues.get(j));
					outcomeValues.set(j, tempDouble);

					tempTeam = teams.get(j - 1);
					teams.set(j - 1, teams.get(j));
					teams.set(j, tempTeam);
				}
			}
		}

		// We finished, so return true
		return true;
	}
}

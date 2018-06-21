package sbbj_tpg;

import java.util.*;

public class Action {
	protected Long action = null;
	protected Team team = null;

	// Create a new Action object which holds an atomic action.
	public Action(Long action) {
		this.action = action;
	}

	// Create a new Action object which holds a Team.
	public Action(Team team) {
		this.team = team;
	}

	// Retrieve an action from this object
	public long getAction(HashSet<Team> visited, double[] inputFeatures, int rootTeamNum) {
		// If we are not storing an atomic action, then this action holds a Team.
		// Use the provided feature set to generate an action and return it.
		return action == null ? team.getAction(visited, inputFeatures, rootTeamNum) : action;
	}

	// Returns true if this action is atomic.
	public boolean isAtomic() {
		return team == null;
	}

	public boolean equals(Action other) {
		// If the teams' IDs match, then return true
		if (action == null && other.action == null && team.getID() == other.team.getID())
			return true;

		// If the atomic actions are the same, the return true
		if (team == null && other.team == null && action.equals(other.action))
			return true;

		// Otherwise return false
		return false;
	}

	public String toString() {
		return "[" + (isAtomic() ? action : "T" + team.getID()) + "]";
	}
}

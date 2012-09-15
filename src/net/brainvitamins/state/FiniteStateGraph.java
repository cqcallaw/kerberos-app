package net.brainvitamins.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public final class FiniteStateGraph {

	private static final String LOG_TAG = "FiniteStateGraph";

	private final HashMap<Vertex, Set<Edge>> map;

	public Map<Vertex, Set<Edge>> getMap() {
		return Collections.unmodifiableMap(map);
	}

	private Vertex currentState;

	public Vertex getCurrentState() {
		return currentState;
	}

	public FiniteStateGraph(Map<Vertex, Set<Edge>> map, Vertex startState) {
		super();
		this.map = new HashMap<Vertex, Set<Edge>>(map);
		//TODO: validation (e.g. each edge in the edge set must lead to a different target state)
		this.currentState = startState;
	}

	public synchronized void transition(Vertex targetState) {
		for (Edge edge : map.get(currentState)) {
			if (edge.getEnd() == targetState) // reference equality
			{
				Log.d(LOG_TAG, "Transitioning to state "
						+ edge.getEnd().getName());
				edge.getAction().run();
				currentState = edge.getEnd();
				Log.d(LOG_TAG, "Transition to state " + edge.getEnd().getName()
						+ " complete");
				return;
			}
		}

		Log.e(LOG_TAG, "Cannot transition from state "
				+ currentState.getName() + " to state " + targetState.getName());
	}
}

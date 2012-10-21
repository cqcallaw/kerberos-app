package net.brainvitamins.state;

/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

		// validate input: can't have two edges to the same vertex
		for (Map.Entry<Vertex, Set<Edge>> entry : map.entrySet()) {
			for (Edge edge : entry.getValue()) {
				Set<Edge> edgeSet = new HashSet<Edge>(entry.getValue());
				edgeSet.remove(edge);
				for (Edge otherEdge : edgeSet) {
					if (otherEdge.getEnd() == edge.getEnd()) {
						throw new IllegalArgumentException("Duplicate edge "
								+ entry.getKey() + otherEdge.toString());
					}
				}
			}
		}

		this.map = new HashMap<Vertex, Set<Edge>>(map);
		this.currentState = startState;
	}

	public synchronized void transition(Vertex targetState) {
		if (map.get(currentState) != null) {
			for (Edge edge : map.get(currentState)) {
				if (edge.getEnd() == targetState) { // reference equality
					Log.d(LOG_TAG, "Transitioning to state "
							+ edge.getEnd().getName());
					edge.getAction().run();
					currentState = edge.getEnd();
					Log.d(LOG_TAG, "Transition to state "
							+ edge.getEnd().getName() + " complete");
					return; // successful transition
				}
			}
		}

		// failure: no state matches, or there are no valid transitions away
		// from this state.
		Log.e(LOG_TAG, "Cannot transition from state " + currentState.getName()
				+ " to state " + targetState.getName());
	}
}

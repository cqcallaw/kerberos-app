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

public final class Edge {

	private final Vertex end;

	public Vertex getEnd() {
		return end;
	}

	private final Runnable action;

	public Runnable getAction() {
		return action;
	}

	public Edge(Vertex end, Runnable action) {
		super();
		this.end = end;
		this.action = action;
	}

	@Override
	public String toString() {
		return "-> " + end.toString();
	}
}

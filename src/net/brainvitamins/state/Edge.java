package net.brainvitamins.state;

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

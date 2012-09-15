package net.brainvitamins.state;

public final class Vertex {

	private final String name;

	public String getName() {
		return name;
	}

	public Vertex(String name) {
		super();
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}

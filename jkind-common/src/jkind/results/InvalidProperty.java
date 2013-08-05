package jkind.results;

/**
 * An invalid property
 */
public final class InvalidProperty extends Property {
	private final Counterexample cex;
	private final double runtime;

	public InvalidProperty(String name, Counterexample cex, double runtime) {
		super(name);
		this.runtime = runtime;
		this.cex = cex;
	}

	/**
	 * Counterexample for the property
	 */
	public Counterexample getCounterexample() {
		return cex;
	}

	/**
	 * Runtime of falsification measured in seconds
	 */
	public double getRuntime() {
		return runtime;
	}
	
	@Override
	public Property rename(Renaming renaming) {
		String newName = renaming.rename(name);
		if (newName == null) {
			return null;
		}
		
		return new InvalidProperty(newName, cex.rename(renaming), runtime);
	}
}
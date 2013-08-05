package jkind.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jkind.JKindException;
import jkind.lustre.values.Value;

/**
 * A signal is a trace of values for a specific variable
 * 
 * @param <T>
 *            type of value contained in the signal
 */
public final class Signal<T extends Value> {
	private final String name;
	private final Map<Integer, T> values;

	public Signal(String name) {
		this.name = name;
		this.values = new HashMap<>();
	}

	private Signal(String name, Map<Integer, T> values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Name of the signal
	 */
	public String getName() {
		return name;
	}

	public void putValue(int step, T value) {
		values.put(step, value);
	}

	/**
	 * Get the value of the signal on a specific step
	 * 
	 * @param step
	 *            step to query the value at
	 * @return value at the specified step or <code>null</code> if the signal
	 *         does not have a value on that step
	 */
	public T getValue(int step) {
		return values.get(step);
	}

	/**
	 * Get a time step indexed map containing all values for the signal
	 */
	public Map<Integer, T> getValues() {
		return Collections.unmodifiableMap(values);
	}

	/**
	 * Downcast the signal to a specific signal type
	 */
	public <S extends T> Signal<S> cast(Class<S> klass) {
		Signal<S> castSignal = new Signal<S>(name);
		for (Integer step : values.keySet()) {
			Value value = values.get(step);
			if (klass.isInstance(value)) {
				castSignal.putValue(step, klass.cast(value));
			} else {
				throw new JKindException("Cannot cast " + value.getClass().getSimpleName() + " to "
						+ klass.getSimpleName());
			}
		}
		return castSignal;
	}

	/**
	 * Rename signal
	 * 
	 * @param renaming
	 *            The renaming to use
	 * @return Renamed version of the signal or <code>null</code> if there is no
	 *         renaming for it
	 * @see Renaming
	 */
	public Signal<T> rename(Renaming renaming) {
		String newName = renaming.rename(name);
		if (newName == null) {
			return null;
		}

		HashMap<Integer, T> newValues = new HashMap<>(values);
		return new Signal<T>(newName, newValues);
	}
}
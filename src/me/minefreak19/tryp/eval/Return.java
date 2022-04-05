package me.minefreak19.tryp.eval;

/**
 * Not indicative of an error.
 * <p>
 * Only used to traverse the call-stack backwards to
 * the last `call` method of Interpreter.
 * <p>
 * This is useful to return from Tryp processes.
 */
public class Return extends RuntimeException {
	// package-private so TrypProc can access it
	final Object value;

	public Return(Object value) {
		super(null, null, false, false);
		this.value = value;
	}

}

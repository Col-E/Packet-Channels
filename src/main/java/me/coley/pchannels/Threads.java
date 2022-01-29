package me.coley.pchannels;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread utility.
 *
 * @author Matt Coley
 */
public class Threads {
	/**
	 * @return Cached thread pool with daemon threads.
	 */
	public static ExecutorService pool() {
		return Executors.newCachedThreadPool(r -> {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		});
	}
}

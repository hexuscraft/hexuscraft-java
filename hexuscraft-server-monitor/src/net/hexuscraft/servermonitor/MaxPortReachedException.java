package net.hexuscraft.servermonitor;

public class MaxPortReachedException extends RuntimeException {
	public MaxPortReachedException() {
		super("Max port reached");
	}
}

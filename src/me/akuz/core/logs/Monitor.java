package me.akuz.core.logs;

public interface Monitor {
	
	void write(String message);

	void write(String message, Throwable ex);

}

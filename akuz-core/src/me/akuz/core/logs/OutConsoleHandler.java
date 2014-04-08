package me.akuz.core.logs;

import java.util.logging.ConsoleHandler;

public final class OutConsoleHandler extends ConsoleHandler {

	public OutConsoleHandler() {
		setOutputStream(System.out);
	}
}

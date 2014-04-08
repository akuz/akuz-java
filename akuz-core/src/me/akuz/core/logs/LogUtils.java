package me.akuz.core.logs;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogUtils {

	private static Level _level = null;
	private static final Map<String, Logger> _loggers = new HashMap<>();
	private static final OutConsoleHandler _consoleHandler = new OutConsoleHandler();
	
	public static final void configure(Level level) {
		_level = level;
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s [%1$tc %2$s] %6$s%n");
		System.setProperty("java.util.logging.manager", ManualResetLogManager.class.getName());
	}

	public static final Logger getLogger(String name) {
		if (_level == null) {
			Logger log = Logger.getLogger(name);
			log.warning(LogUtils.class.getName() + " log level not configured");
			return log;
		} else {
			Logger log = _loggers.get(name);
			if (log == null) {
				log = Logger.getLogger(name);
				log.addHandler(_consoleHandler);
				_consoleHandler.setLevel(_level);
				log.setUseParentHandlers(false);
				log.setLevel(_level);
				_loggers.put(name, log);
			}
			return log;
		}
	}
}

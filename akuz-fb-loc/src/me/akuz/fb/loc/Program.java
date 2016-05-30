package me.akuz.fb.loc;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.logs.LogUtils;

public class Program {

	public static void main(String[] args) {
		
		LogUtils.configure(Level.INFO);
		Logger log = LogUtils.getLogger("main");
		
		
		
		log.info("DONE.");
	}

}

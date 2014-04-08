package me.akuz.core.logs;

import java.util.logging.LogManager;

public class ManualResetLogManager extends LogManager {
    
	private static ManualResetLogManager _instance;
    
    public ManualResetLogManager() {
    	_instance = this;
    }
    
    @Override
    public void reset() {
    	// don't reset yet
    }
    
    private void superReset() {
    	super.reset();
    }
    
    public static void resetFinally() {
    	if (_instance != null)  {
    		_instance.superReset();
    		_instance = null;
    	}
    }
}
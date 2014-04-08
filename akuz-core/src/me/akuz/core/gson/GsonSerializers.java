package me.akuz.core.gson;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonSerializers {
	
	public final static Gson Plain = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDateAdapter()).create();
	public final static Gson Pretty = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDateAdapter()).setPrettyPrinting().create();
	public final static Gson NoHtmlEscapingPlain = new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(Date.class, new GsonDateAdapter()).create();
	public final static Gson NoHtmlEscapingPretty = new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(Date.class, new GsonDateAdapter()).setPrettyPrinting().create();
	
	public static final Gson getPlain() {
		return new GsonBuilder()
			.registerTypeAdapter(Date.class, new GsonDateAdapter())
			.create();
	}
	
	public static final Gson getPretty() {
		return new GsonBuilder()
			.registerTypeAdapter(Date.class, new GsonDateAdapter())
			.setPrettyPrinting()
			.create();
	}
	
	public static final Gson getNoHtmlEscapingPlain() {
		return new GsonBuilder()
			.disableHtmlEscaping()
			.registerTypeAdapter(Date.class, new GsonDateAdapter())
			.create();
	}
	
	public static final Gson getNoHtmlEscapingPretty() {
		return new GsonBuilder()
			.disableHtmlEscaping()
			.registerTypeAdapter(Date.class, new GsonDateAdapter())
			.setPrettyPrinting()
			.create();
	}

}

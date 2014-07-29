package me.akuz.ts.io.types;

import java.io.IOException;
import java.util.TimeZone;

import me.akuz.core.DateFmt;

import com.google.gson.JsonObject;

/**
 * Time series IO data type adapter.
 *
 */
public abstract class TSIOType {
	
	public static final TSIOType Date_Standard_UTC        = new TSIOTypeDate(DateFmt.StandardUtcFormat, TimeZone.getTimeZone("UTC"));
	public static final TSIOType Date_YYYYMMDD_UTC        = new TSIOTypeDate(DateFmt.YYYYMMDD, TimeZone.getTimeZone("UTC"));
	public static final TSIOType Date_YYYYMMDD_dashed_UTC = new TSIOTypeDate(DateFmt.YYYYMMDD_dashed, TimeZone.getTimeZone("UTC"));
	public static final TSIOType Date_YYYYMMDDHHMMSS_UTC  = new TSIOTypeDate(DateFmt.YYYYMMDDHHMMSS, TimeZone.getTimeZone("UTC"));
	
	public static final TSIOType BooleanType = new TSIOTypeBoolean();
	public static final TSIOType DoubleType  = new TSIOTypeDouble();
	public static final TSIOType IntegerType = new TSIOTypeInteger();
	public static final TSIOType StringType  = new TSIOTypeString();
	
	public abstract Object fromJsonField(JsonObject obj, String name) throws IOException;
	public abstract void toJsonField(JsonObject obj, String name, Object value);
	
	public abstract Object fromString(String str) throws IOException;
	public abstract String toString(Object value);
	
}

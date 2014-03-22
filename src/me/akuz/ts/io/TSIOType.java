package me.akuz.ts.io;

import java.io.IOException;
import java.util.TimeZone;

import me.akuz.core.DateFmt;

import org.json.JSONObject;

/**
 * Time series IO data type adapter.
 *
 */
public abstract class TSIOType {
	
	public static final TSIOType DateStandardUtc       = new TSIOTypeDate(DateFmt.StandardUtcFormat, TimeZone.getTimeZone("UTC"));
	public static final TSIOType DateYYYYMMDDUtc       = new TSIOTypeDate(DateFmt.YYYYMMDD, TimeZone.getTimeZone("UTC"));
	public static final TSIOType DateYYYYMMDDHHMMSSUtc = new TSIOTypeDate(DateFmt.YYYYMMDDHHMMSS, TimeZone.getTimeZone("UTC"));
	
	public static final TSIOType Boolean = new TSIOTypeBoolean();
	public static final TSIOType Double  = new TSIOTypeDouble();
	public static final TSIOType Integer = new TSIOTypeInteger();
	public static final TSIOType String  = new TSIOTypeString();
	
	public abstract Object fromJson(JSONObject obj, String name) throws IOException;
	public abstract void setJsonField(JSONObject obj, String name, Object value);
	
}

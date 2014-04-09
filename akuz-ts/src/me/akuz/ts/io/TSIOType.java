package me.akuz.ts.io;

import java.io.IOException;
import java.util.TimeZone;

import me.akuz.core.DateFmt;
import me.akuz.ts.io.types.TSIOTypeBoolean;
import me.akuz.ts.io.types.TSIOTypeDate;
import me.akuz.ts.io.types.TSIOTypeDouble;
import me.akuz.ts.io.types.TSIOTypeInteger;
import me.akuz.ts.io.types.TSIOTypeString;

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
	
	public static final TSIOType Boolean = new TSIOTypeBoolean();
	public static final TSIOType Double  = new TSIOTypeDouble();
	public static final TSIOType Integer = new TSIOTypeInteger();
	public static final TSIOType String  = new TSIOTypeString();
	
	public abstract Object fromJson(JsonObject obj, String name) throws IOException;
	public abstract void setJsonField(JsonObject obj, String name, Object value);
	
}
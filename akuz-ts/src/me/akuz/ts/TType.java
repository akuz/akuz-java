package me.akuz.ts;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import me.akuz.core.DateFmt;
import me.akuz.ts.types.TBoolean;
import me.akuz.ts.types.TDate;
import me.akuz.ts.types.TDouble;
import me.akuz.ts.types.TInteger;
import me.akuz.ts.types.TString;

import com.google.gson.JsonObject;

/**
 * Time series IO data type adapter.
 *
 */
public abstract class TType {
	
	public static final TType Date_Standard_UTC        = new TDate(DateFmt.StandardUtcFormat, TimeZone.getTimeZone("UTC"));
	public static final TType Date_YYYYMMDD_UTC        = new TDate(DateFmt.YYYYMMDD, TimeZone.getTimeZone("UTC"));
	public static final TType Date_YYYYMMDD_dashed_UTC = new TDate(DateFmt.YYYYMMDD_dashed, TimeZone.getTimeZone("UTC"));
	public static final TType Date_YYYYMMDDHHMMSS_UTC  = new TDate(DateFmt.YYYYMMDDHHMMSS, TimeZone.getTimeZone("UTC"));
	
	public static final TType BooleanType = new TBoolean();
	public static final TType DoubleType  = new TDouble();
	public static final TType IntegerType = new TInteger();
	public static final TType StringType  = new TString();
	
	public abstract Object fromJsonField(JsonObject obj, String name) throws IOException;
	public abstract void toJsonField(JsonObject obj, String name, Object value);
	
	public abstract Object fromString(String str) throws IOException;
	public abstract String toString(Object value);
	
	public static final TType deriveDataType(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Time-series cannot contain null values");
		} else if (value instanceof Boolean) {
			return BooleanType;
		} else if (value instanceof Date) {
			return Date_Standard_UTC;
		} else if (value instanceof Double) {
			return DoubleType;
		} else if (value instanceof Integer) {
			return IntegerType;
		} else if (value instanceof String) {
			return StringType;
		} else {
			throw new IllegalArgumentException(
					"Value of type '" + 
					value.getClass().getSimpleName() + 
					"' cannot be contained in time-series");
		}
	}
	
}

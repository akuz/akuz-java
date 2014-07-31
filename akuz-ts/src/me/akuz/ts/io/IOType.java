package me.akuz.ts.io;

import java.io.IOException;

import me.akuz.ts.io.types.TBoolean;
import me.akuz.ts.io.types.TDate;
import me.akuz.ts.io.types.TDouble;
import me.akuz.ts.io.types.TInteger;
import me.akuz.ts.io.types.TString;

import com.google.gson.JsonObject;

/**
 * Time series IO data type.
 *
 */
public abstract class IOType {
	
	public static final IOType BooleanType = new TBoolean();
	public static final IOType DateType    = new TDate();
	public static final IOType DoubleType  = new TDouble();
	public static final IOType IntegerType = new TInteger();
	public static final IOType StringType  = new TString();
	
	public abstract Object fromJsonField(JsonObject obj, String name) throws IOException;
	public abstract void toJsonField(JsonObject obj, String name, Object value);
	
	public abstract Object fromString(String str) throws IOException;
	public abstract String toString(Object value);
	
}

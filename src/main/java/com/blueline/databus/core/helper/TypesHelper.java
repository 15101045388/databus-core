package com.blueline.databus.core.helper;

public class TypesHelper {

	public static String typeConvert(String string) {
		if (string.equals("int") || string.equals("INT")) {
			return "INT";
		} else if (string.equals("varchar") || string.equals("VARCHAR")) {
			return "VARCHAR";
		} else if (string.equals("datetime") || string.equals("DATETIME")) {
			return "DATETIME";
		} else if (string.equals("decimal") || string.equals("DECIMAL")) {
			return "DECIMAL";
		} else if (string.equals("float") || string.equals("FLOAT")) {
			return "FLOAT";
		} else if (string.equals("point") || string.equals("POINT")) {
			return "POINT";
		} else if (string.equals("double") || string.equals("DOUBLE")) {
			return "DOUBLE";
		}
		return null;
	}
}

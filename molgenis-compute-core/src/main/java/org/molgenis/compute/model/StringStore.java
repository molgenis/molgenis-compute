package org.molgenis.compute.model;

import java.util.HashMap;

/**
 * Stores the String instances. The parameter combinations repeat the same string keys and values over and over again.
 * By default, the JVM does not use unique values for the strings. This class maps the String instances you feed it to
 * the first instance of that String value so that the other instances can be collected.
 * 
 * It basically does the same as {@link String#intern} except that this class can be garbage collected when Compute has
 * run. This is needed if you want to keep the JVM alive between Compute runs, for example if you're incorporating
 * Compute in a web container.
 */
public class StringStore
{
	private HashMap<String, String> strings = new HashMap<>();

	public String intern(String s)
	{
		strings.putIfAbsent(s, s);
		return strings.get(s);
	}
}

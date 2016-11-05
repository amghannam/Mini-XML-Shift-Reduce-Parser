/*
 * CS 575: Project #2
 * File: ParserException.java
 */
package org.aghannam.parser;

/**
 * This class represents the exception thrown by the parser when a syntax error is reported. 
 * 
 * @author Ahmed Ghannam (amalghannam@crimson.ua.edu) 
 */
@SuppressWarnings("serial")
public class ParserException extends Exception {
	public ParserException(String msg) {
		super(msg); 
	}
}

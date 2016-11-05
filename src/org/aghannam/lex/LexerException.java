/*
 * CS 575: Project #2
 * File: LexerException.java
 */
package org.aghannam.lex;

/**
 * This class represents the exception thrown by the lexer when something is wrong with the file. 
 * 
 * @author Ahmed Ghannam (amalghannam@crimson.ua.edu)
 */
@SuppressWarnings("serial")
public class LexerException extends Exception {
	public LexerException(String msg) {
		super(msg); 
	}
}

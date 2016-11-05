/*
 * CS 575: Project #2
 * File: Token.java
 */
package org.aghannam.lex;

import org.aghannam.lex.Lexer.TokenType;

/**
 * This class implements a token representation.  
 * <p>
 * For simplicity, we define a token in terms of its type and data. The 'type' of a token is used 
 * by both the lexer and the parser for scanning and parsing, respectively--it is how a token is recognized. 
 * On the other hand, the 'data' (or equivalently, 'value', or 'lexeme') of a token is whatever information it holds. 
 * 
 * @author Ahmed Ghannam (amalghannam@crimson.ua.edu) 
 */
public class Token {
	private final TokenType type; 
	private final String lexeme; 
	
	public Token(TokenType type, String lexeme) {
		this.type = type;
		this.lexeme = lexeme; 
	}
	
	/**
	 * Returns the type of this token. 
	 * <p>
	 * This is the input symbol that the parser processes for each individual token. 
	 * 
	 * @return the type of this token
	 */
	public TokenType getType() {
		return this.type; 
	}
	
	/**
	 * Returns the lexical instance associated with this token. 
	 * <p>
	 * This value represents what is known as a 'lexeme' and may or may not be the same for tokens of the same type. 
	 * Although it is automatically paired with every token as part of the lexing process,
	 * only tokens of type NAME actually benefit from having this value, as it is required for name-matching and checking 
	 * attribute names within an XML-- tag. 
	 * 
	 * @return the lexeme that is represented by this token
	 */
	public String getLexeme() {
		return this.lexeme; 
	}

	/**
	 * Returns a string representation of this token, which is its type followed by its lexical instance. 
	 */
	@Override
	public String toString() {
		return String.format("%s %s", this.getType(), this.getLexeme()); 
	}
}

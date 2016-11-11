/* 
 * CS 575: Project #2
 * File: Parser.java 
 */
package org.aghannam.parser;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.HashMap;

import org.aghannam.lex.Token;
import org.aghannam.lex.Lexer.TokenType;

/**
 * This class implements a bottom-up, shift-reduce parser for XML--, a fictional subset of XML. 
 * <p>
 * The parser recognizes the following left-recursive LR(1) grammar (augmented with a special start rule):
 * <p>
 * s'            ::=    document <br>
 * document      ::= 	element <br>
 * element       ::= 	< elementPrefix <br>
 * elementPrefix ::= 	NAME attribute elementSuffix <br>
 * attribute 	 ::= 	attribute NAME = STRING<br>
 * attribute 	 ::= 	EPSILON <br>
 * elementSuffix ::= 	> elementOrData endTag <br>
 * elementSuffix ::= 	/> <br>
 * elementOrData ::= 	elementOrData element <br>
 * elementOrData ::= 	elementOrData DATA <br>
 * elementOrData ::=    EPSILON <br>
 * endTag 	 ::=    &lt;/ NAME > <br>
 * <p>
 * The parser verifies if a given XML-- file is syntactically correct according to the above grammar, in 
 * which case it prints out a rightmost derivation in reverse order with one grammar rule displayed per line. 
 * Essentially, the parser simulates a deterministic pushdown automaton that is equivalent to the above grammar. 
 * Per the grammar, the tokens are NAME, DATA, STRING, <, >, &lt;/, /&gt;, and =. 
 * <p>
 * This is a hand-coded, semi-naive implementation of an LR(1) parser, taking into account all possible parse states. 
 * In total, there are 33 possible states that the parser can be at at any given time, each of which is represented 
 * by its own method. Finally, the actual parse table used by this class was generated by a special tool, along 
 * with the corresponding LR(1) automaton. For more information on the tool, please see the URL linked below. 
 * <p>
 * @see <a href="http://smlweb.cpsc.ucalgary.ca/">The Context-Free Grammar Checker</a>
 * @author Ahmed Ghannam (amalghannam@crimson.ua.edu) 
 * 
 */
public class Parser {
	/* Constant grammar symbols (i.e. nonterminals) */ 
	private static final String START = "S'"; 
	private static final String DOCUMENT = "document"; 
	private static final String ELEMENT = "element";
	private static final String PREFIX = "elementPrefix"; 
	private static final String SUFFIX = "elementSuffix";
	private static final String EOD = "elementOrData"; 
	private static final String ATTR = "attribute"; 
	private static final String ET = "endTag";  
	
	/* Other related constants. */ 
	private static final int MAXIMUM_RHS_LENGTH = 4; 
	private static final int MAXIMUM_GOTO_ROWS = 28;  
	
	/* General variable declaration. */
	private Stack<String> symbols;
	private Stack<Integer> states;
	private Stack<String> tagNames;
	private ArrayList<Token> tokens;
	private HashSet<String> attributeNames;
	private Token lookahead;
	private int currentState;
	private boolean isValid;
	private boolean complete;
	private String gotoTable[][]; 
	private HashMap<String, Integer> nonterminalColumns;
	
	/**
	 * Initializes the necessary variables and data structures in preparation for parsing. 
	 */
	private void init() {
		symbols = new Stack<String>();  // used to hold grammar symbols (terminals and nonterminals) 
		states = new Stack<Integer>();  // used to determine the next parse state at any given 
		tagNames = new Stack<String>(); // to ensure that corresponding tag names match 
		attributeNames = new HashSet<String>(); // to ensure no duplicate attribute names within a tag
		currentState = -1; // holds the current parse state
		isValid = true; // whether or not the input document is a well-formed XML-- file
		complete = false; // success or failure 
		initializeGotoTable(); 
	}
	
	/**
	 * Populates the GOTO portion of the parse table according to the grammar. 
	 */
	private void initializeGotoTable() {
		/* Set each nonterminal and its corresponding column number. */ 
		nonterminalColumns = new HashMap<String, Integer>(); 
		nonterminalColumns.put(START, 1); 
		nonterminalColumns.put(DOCUMENT, 2);
		nonterminalColumns.put(PREFIX, 3);
		nonterminalColumns.put(ATTR, 4);
		nonterminalColumns.put(SUFFIX, 5); 
		nonterminalColumns.put(ELEMENT, 6);
		nonterminalColumns.put(EOD, 7);
		nonterminalColumns.put(ET, 8); 
		
		/* The actual GOTO table. */ 
		gotoTable = new String[][] {{"0", "3", "2", "", "", "", "1", "", ""}, 
									{"1", "", "", "", "", "", "", "", ""},
									{"2", "", "", "", "", "", "", "", ""},
									{"3", "", "", "", "", "", "", "", ""},
									{"4", "", "", "5", "", "", "", "", ""},
									{"5", "", "", "", "", "", "", "", ""},
									{"6", "", "", "", "7", "", "", "", ""},
									{"7", "", "", "", "", "8", "", "", ""},
									{"8", "", "", "", "", "", "", "", ""},
									{"9", "", "", "", "", "", "", "", ""},
									{"10", "", "", "", "", "", "", "", ""},
									{"11", "", "", "", "", "", "", "12", ""},
									{"12", "", "", "", "", "", "16", "", "15"},
									{"13", "", "", "", "", "", "", "", ""}, 
									{"14", "", "", "", "", "", "", "", ""},
									{"15", "", "", "", "", "", "", "", ""},
									{"16", "", "", "", "", "", "", "", ""},
									{"17", "", "", "21", "", "", "", "", ""},
									{"18", "", "", "", "", "", "", "", ""},
									{"19", "", "", "", "", "", "", "", ""},
									{"20", "", "", "", "", "", "", "", ""},
									{"21", "", "", "", "", "", "", "", ""},
									{"22", "", "", "", "23", "", "", "", ""},
									{"23", "", "", "", "", "25", "", "", ""},
									{"24", "", "", "", "", "", "", "", ""},
									{"25", "", "", "", "", "", "", "", ""},
									{"26", "", "", "", "", "", "", "", ""},
									{"27", "", "", "", "", "", "", "28", ""},
									{"28", "", "", "", "", "", "16", "", "29"},
									{"29", "", "", "", "", "", "", "", ""},
									{"30", "", "", "", "", "", "", "", ""},
									{"31", "", "", "", "", "", "", "", ""},
									{"32", "", "", "", "", "", "", "", ""},
		}; 
	}
	
	/**
	 * Parses an XML-- document using shift-reduce and prints out a rightmost derivation that corresponds 
	 * to a parse tree generating the given input token sequence. 
	 * 
	 * @param tokens the token stream returned by the lexical analyzer 
	 * @throws ParserException if any syntax errors are encountered during the parsing process 
	 */
	public void parse(List<Token> tokens) throws ParserException {
		try {
			init();

			states.push(0); // initial state on stack 

			this.tokens = new ArrayList<Token>(tokens);
			this.tokens.add(new Token(TokenType.EOF, "&$"));
			lookahead = this.tokens.get(0);
			
			/* The actual parsing algorithm is triggered within this loop. */ 
			while (!complete && isValid) {
				parse();
			}

			if (isValid) {
				System.out.println("\nDocument parsed successfully!");
			} else {
				throw new ParserException("Syntax error: An unexpected symbol has been encountered!");
			}
		} catch (EmptyStackException e) {
			System.err.println("A syntax error has caused a stack underflow. Parsing terminated...");
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Fatal error while parsing...");
		}
	}
	
	/**
	 * Advances by one token and sets the lookahead to be the next token in the input stream. 
	 * <p>
	 * This method is called frequently by <code>shift()</code>, whenever a terminal is pushed 
	 * into the parse stack. 
	 */
	private void nextToken() {
		tokens.remove(0);

		if (tokens.isEmpty()) {
			lookahead = new Token(TokenType.EOF, "&$"); 
		} else {
			lookahead = tokens.get(0);
		}
	}
	
	/**
	 * Pushes the given token into the parse stack and goes to the specified state.
	 * <p>
	 * Per the algorithm, a shift operation involves pushing a terminal into the stack and then advancing 
	 * the input stream by one token. Accordingly, this method updates both the symbol stack and the state stack,
	 * which collectively represent the parse stack. In addition, whenever it is called, the lookahead is set to 
	 * the next token returned by the scanner. 
	 * 
	 * @param token the terminal to push into the parse stack 
	 * @param state the next state to which to advance the parser 
	 */
	private void shift(String token, int state) {
		symbols.push(token);
		states.push(state);
		nextToken();  
	}
	
	/**
	 * Applies a completed grammar rule by popping the specified number of symbols and pushing the corresponding 
	 * left-hand side nonterminal into the parse stack.
	 * <p>
	 * The number of symbols to pop off the stack is known from the parse table for the grammar, as is the corresponding
	 * left-hand side nonterminal. A reduction simply updates the parse stack and does not advance the input stream. 
	 * At least one right-hand side symbol must be popped. For this reason, this method cannot be used to reduce by
	 * EPSILON rules and will fail if such a call is made. 
	 * 
	 * @param lhs the left-hand side nonterminal to push into the parse stack 
	 * @param numOfSymbols how many symbols appear to the right-hand side of the rule by which to reduce 
	 */
	private void reduce(String lhs, int numOfSymbols) {
		try {
			boolean isValidNonterminal = nonterminalColumns.containsKey(lhs);
			if (isValidNonterminal && numOfSymbols > 0 && numOfSymbols <= MAXIMUM_RHS_LENGTH) {
				int popped = 0;
				do {
					symbols.pop();
					states.pop();
					popped++;
				} while (popped < numOfSymbols);
				symbols.push(lhs);
				states.push(lookupStateAt(states.peek(), nonterminalColumns.get(lhs)));
			} else {
				throw new IllegalArgumentException("Reduction error: No such grammar rule for '" + lhs +"'!"); 
			}
		} catch (NumberFormatException e) {
			System.err.println("Reduction error: No such state found for '" + lhs + "'!");
			System.exit(1); 
		}
	}
	
	/**
	 * Consults the GOTO table to deduce the next state to go to following a reduce operation. 
	 * <p>
	 * When a non-epsilon reduce operation is underway, the removal of right-hand side symbols exposes some old state 
	 * at the top of the stack. This exposed state represents the row at which to find the next appropriate action, which is 
	 * located at the column corresponding to the nonterminal pushed into the parse stack at the end of a reduction. 
	 * <p>
	 * This method is used internally by <code>reduce()</code> whenever a reduction has been applied. 
	 *
	 * @param row the row number at which to lookup the next state
	 * @param column the nonterminal column number at which to lookup the next state
	 * @return the next state to which to advance after a successful reduction 
	 */
	private int lookupStateAt(int row, int column) {
		if (row <= MAXIMUM_GOTO_ROWS && column > 0 && column <= nonterminalColumns.size())
			return Integer.parseInt(gotoTable[row][column]);
		return -1; 
	}
	
	
	/**
	 * Advances the parser to the specified state without changing the lookahead value. 
	 * <p>
	 * This method is only used when a nonterminal has been pushed into the parse stack, otherwise 
	 * <code>nextToken()</code> should be used. 
	 * 
	 * @param state the state to go to after a nonterminal has been pushed into the stack 
	 */
	private void goToState(int state) {
		states.push(state); 
	}
	
	/**
	 * Checks whether the current lookahead token (i.e. terminal) is the same as the specified token.
	 *  
	 * @param type the type of the token against which to compare the lookahead
	 * @return <code>true</code> if there is a match, <code>false</code> otherwise
	 */
	private boolean isTerminal(TokenType type) {
		return lookahead.getType() == type;
	}
	
	/**
	 * Checks whether the specified nonterminal is at the top of the stack. 
	 * 
	 * @param nonterminal the nonterminal to check 
	 * @return <code>true</code> if this nonterminal is at the top of the stack, <code>false</code> otherwise
	 */
	private boolean isNonterminal(String nonterminal) {
		return symbols.peek().equals(nonterminal); 
	}
	
	/**
	 * Temporarily caches the current tag name for a possible future match with an end tag name.
	 * <p>
	 * If the current tag turns out to be an empty tag (i.e. no name appears at the end to match against),
	 * then the cached name is simply discarded and no matching is performed. (This method, however, does not 
	 * explicitly check whether the current tag is an empty tag--that conclusion is inferred by the parser, 
	 * per the grammar.)
	 * 
	 * @param openName the token representing the name at the beginning of this tag
	 */
	private void cacheTagName(Token openName) {
		this.tagNames.push(openName.getLexeme()); 
		System.out.println("*** Encountered start tag '" + openName.getLexeme() + "' ***");
	}
	
	/**
	 * Verifies that a start tag and its corresponding end tag have identical names. 
	 * <p>
	 * This method uses the name previously cached by <code>cacheTagName()</code> to do the matching. 
	 * Should a mismatch be detected, the parser immediately terminates and does not continue parsing
	 * the rest of the document. Case sensitivity counts. 
	 *  
	 * @param endName the token representing the name at the end of the current tag
	 */
	private void matchTagName(Token endName) {
		String openName = this.tagNames.pop(); 
		// Here, the use of equals() in the condition automatically handles the case sensitivity requirement
		if (!openName.equals(endName.getLexeme())) {
			System.out.println("Error - End tag name mismatch. Expected '" + openName 
					+ "' but found '" + endName.getLexeme() + "'.");
			System.out.println("Parsing terminated...");
			System.exit(2);
		} else {
			System.out.println("*** Successfully matched end tag '" + openName + "' ***");
		}
	}
	
	/**
	 * Verifies that no two attributes within a tag share the same name. 
	 * <p>
	 * By design, attributes may have the same name as long as they are in different tags. However,
	 * attributes within the same tag must each have a unique name. This method serves to enforce this 
	 * rule. Should a duplicate name be detected for a given tag's attributes, the parser immediately 
	 * terminates and does not continue parsing the rest of the document. 
	 * 
	 * @param attributeName the token representing the attribute name to check 
	 */
	private void checkDuplicateNames(Token attributeName) {
		// A HashSet automatically returns true if an item is unique, in which case it can safely be added 
		if (!attributeNames.add(attributeName.getLexeme())) {
			System.out.println("Error - Detected duplicate attribute name within current tag!");
			System.out.println("Parsing terminated...");
			System.exit(3); 
		}
	}

	/**
	 * Applies the parse table actions by moving from state to state, until either a success or failure occurs. 
	 * 
	 * @throws ParserException if a syntax error is encountered at any given point during parsing 
	 */
	private void parse() throws ParserException {
		if (states.isEmpty()) {
			currentState = -1;
		} else {
			currentState = states.peek(); // state continually changes as the parser progresses 
		}
		switch (currentState) {
		case 0:
			s0();
			break;
		case 1:
			s1();
			break;
		case 2:
			s2();
			break;
		case 3:
			s3();
			break;
		case 4:
			s4();
			break;
		case 5:
			s5();
			break;
		case 6:
			s6();
			break;
		case 7:
			s7();
			break;
		case 8:
			s8();
			break;
		case 9:
			s9();
			break;
		case 10:
			s10();
			break;
		case 11:
			s11();
			break;
		case 12:
			s12();
			break;
		case 13:
			s13();
			break;
		case 14:
			s14();
			break;
		case 15:
			s15();
			break; 
		case 16:
			s16();
			break; 
		case 17:
			s17();
			break;
		case 18:
			s18();
			break;
		case 19:
			s19();
			break;
		case 20:
			s20();
			break;
		case 21:
			s21();
			break;
		case 22:
			s22();
			break; 
		case 23:
			s23();
			break;
		case 24:
			s24();
			break;
		case 25:
			s25();
			break;
		case 26:
			s26();
			break;
		case 27:
			s27();
			break;
		case 28:
			s28();
			break;
		case 29:
			s29();
			break;
		case 30:
			s30();
			break;
		case 31:
			s31();
			break;
		case 32:
			s32();
			break;
		default:
			throw new ParserException("Parser error: Unrecognized action triggered while parsing.");
		}
	}
	
	/*
	 * From this point onward, every method implements a single state in the LR(1) automaton 
	 * (or equivalently, a row in the parse table), identified by the number in the method's name. 
	 * The parser will traverse any arbitrary combination of these states (not necessarily all) during 
	 * the process until either a successful parse or an error is triggered.   
	 */

	private void s0() {
		if (isTerminal(TokenType.OPEN)) {
			shift(lookahead.toString(), 4);
		} else if (isNonterminal(START)) {
			goToState(3);  
		} else if (isNonterminal(DOCUMENT)) {
			goToState(2);  
		} else if (isNonterminal(ELEMENT)) {
			goToState(1); 
		} else {
			isValid = false;
		}
	}

	private void s1() {
		if (isTerminal(TokenType.EOF)) {
			System.out.println("1.1 document ::= element"); 
			reduce(DOCUMENT, 1); 
		} else {
			isValid = false; 
		}
	}

	private void s2() {
		if (isTerminal(TokenType.EOF)) {
			reduce(START, 1); 
		} else {
			isValid = false; 
		}
	}

	private void s3() {
		if (isTerminal(TokenType.EOF)) {
			complete = true; 
		} else {
			isValid = false; 
		}
	}

	private void s4() {
		if (isTerminal(TokenType.NAME)) {
			cacheTagName(lookahead); 
			shift(lookahead.toString(), 6); 
		} else if (isNonterminal(PREFIX)) {
			goToState(5); 
		} else {
			isValid = false; 
		}
	}

	private void s5() {
		if (isTerminal(TokenType.EOF)) {
			System.out.println("5.1 element ::= < elementPrefix");
			reduce(ELEMENT, 2); 
		} else {
			isValid = false; 
		}
	}

	private void s6() {
		if (isTerminal(TokenType.CLOSE)) {;
			System.out.println("6.1 attribute ::= EPSILON");
			symbols.push(ATTR); 
			goToState(7);  		
		} else if (isTerminal(TokenType.NAME)) {
			System.out.println("6.2 attribute ::= EPSILON");
			symbols.push(ATTR); 
			goToState(7);
		} else if (isTerminal(TokenType.SLGT)) {
			System.out.println("6.3 attribute ::= EPSILON");
			symbols.push(ATTR); 
			goToState(7); 
		} else if (isNonterminal(ATTR)) {
			goToState(7); 
		} else {
			isValid = false; 
		}
	}

	private void s7() {
		if (isTerminal(TokenType.CLOSE)) {
			if (!attributeNames.isEmpty()) attributeNames.clear();
			shift(lookahead.toString(), 11); 
		} else if (isTerminal(TokenType.NAME)) { 
			checkDuplicateNames(lookahead); 
			shift(lookahead.toString(), 10); 
		} else if (isTerminal(TokenType.SLGT)) {
			if (!attributeNames.isEmpty()) attributeNames.clear();
			shift(lookahead.toString(), 9); 
		} else if (isNonterminal(SUFFIX)) {
			goToState(8); 
		} else {
			isValid = false; 
		}
	}
	
	private void s8() {
		if (isTerminal(TokenType.EOF)) {
			System.out.println("8.1 elementPrefix ::= NAME attribute elementSuffix"); 
			reduce(PREFIX, 3);  
		} else {
			isValid = false; 
		}
	}
	
	private void s9() {
		if (isTerminal(TokenType.EOF)) {
			if (!tagNames.isEmpty()) tagNames.pop(); 		
			System.out.println("9.1 elementSuffix ::= />");
			System.out.println("*** Closed with empty tag. No name matching performed. ***");
			reduce(SUFFIX, 1); 
		} else {
			isValid = false; 
		}
	}
	
	private void s10() {
		if (isTerminal(TokenType.ASSIGN)) {
			shift(lookahead.toString(), 13); 
		} else {
			isValid = false; 
		}
	}
	
	private void s11() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("11.1 elementOrData ::= EPSILON");
			symbols.push(EOD);
			goToState(12);
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("11.2 elementOrData ::= EPSILON");
			symbols.push(EOD);
			goToState(12);
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("11.3 elementOrData ::= EPSILON");
			symbols.push(EOD);
			goToState(12);
		} else if (isNonterminal(EOD)) {
			goToState(12);
		} else {
			isValid = false;
		}
	}
	
	private void s12() {
		if (isTerminal(TokenType.LTSL)) {
			shift(lookahead.toString(), 19);
		} else if (isTerminal(TokenType.DATA)) {
			shift(lookahead.toString(), 18);
		} else if (isTerminal(TokenType.OPEN)) {
			shift(lookahead.toString(), 17); 
		} else if (isNonterminal(ELEMENT)) {
			goToState(16); 
		} else if (isNonterminal(ET)) {
			goToState(15); 
		} else {
			isValid = false; 
		}
	}
	
	private void s13() {
		if (isTerminal(TokenType.STRING)) {
			shift(lookahead.toString(), 14); 
		} else {
			isValid = false; 
		}
	}
	
	private void s14() {
		if (isTerminal(TokenType.CLOSE)) {  
			System.out.println("14.1 attribute ::= attribute NAME ASSIGN STRING"); 
			reduce(ATTR, 4); 
		} else if (isTerminal(TokenType.NAME)) {
			System.out.println("14.2 attribute ::= attribute NAME ASSIGN STRING"); 
			reduce(ATTR, 4); 
		} else if (isTerminal(TokenType.SLGT)) {   
			System.out.println("14.3 attribute ::= attribute NAME ASSIGN STRING"); 
			reduce(ATTR, 4); 
		} else {
			isValid = false; 
		}
	}
	
	private void s15() {
		if (isTerminal(TokenType.EOF)) {
			System.out.println("15.1 elementSuffix ::= > elementOrData endTag");
			reduce(SUFFIX, 3); 	
		} else {
			isValid = false; 
		}
	}
	
	private void s16() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("16.1 elementOrData ::= elementOrData element"); 
			reduce(EOD, 2); 
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("16.2 elementOrData ::= elementOrData element"); 
			reduce(EOD, 2);
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("16.3 elementOrData ::= elementOrData element"); 
			reduce(EOD, 2);
		} else {
			isValid = false; 
		}
	}
	
	private void s17() {
		if (isTerminal(TokenType.NAME)) {
			cacheTagName(lookahead); 
			shift(lookahead.toString(), 22); 
		} else if (isNonterminal(PREFIX)) {
			goToState(21);
		} else {
			isValid = false; 
		}
	}
	
	private void s18() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("18.1 elementOrData ::= elementOrData DATA"); 
			reduce(EOD, 2); 
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("18.2 elementOrData ::= elementOrData DATA"); 
			reduce(EOD, 2);
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("18.3 elementOrData ::= elementOrData DATA"); 
			reduce(EOD, 2);
		} else {
			isValid = false; 
		}
	}
	
	private void s19() {
		if (isTerminal(TokenType.NAME)) {
			matchTagName(lookahead); 
			shift(lookahead.toString(), 20); 
		} else {
			isValid = false; 
		}
	}
	
	private void s20() {
		if (isTerminal(TokenType.CLOSE)) {
			shift(lookahead.toString(), 24); 
		} else {
			isValid = false; 
		}
	}
	
	private void s21() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("21.1 element ::= < elementPrefix"); 
			reduce(ELEMENT, 2); 
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("21.2 element ::= < elementPrefix"); 
			reduce(ELEMENT, 2);
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("21.3 element ::= < elementPrefix"); 
			reduce(ELEMENT, 2);
		} else {
			isValid = false; 
		}
	}
	
	private void s22() {
		if (isTerminal(TokenType.CLOSE)) {  
			System.out.println("22.1 attribute ::= EPSILON");
			symbols.push(ATTR);
			goToState(23); 
		} else if (isTerminal(TokenType.NAME)) {
			System.out.println("22.2 attribute ::= EPSILON");
			symbols.push(ATTR);
			goToState(23); 
		} else if (isTerminal(TokenType.SLGT)) {
			System.out.println("22.3 attribute ::= EPSILON");
			symbols.push(ATTR);
			goToState(23); 
		} else if (isNonterminal(ATTR)) {
			goToState(23);
		} else {
			isValid = false; 
		}
	}
	
	private void s23() {
		if (isTerminal(TokenType.CLOSE)) { 
			if (!attributeNames.isEmpty()) attributeNames.clear();
			shift(lookahead.toString(), 27); 
		} else if (isTerminal(TokenType.NAME)) {
			checkDuplicateNames(lookahead); 
			shift(lookahead.toString(), 10);		
		} else if (isTerminal(TokenType.SLGT)) {
			if (!attributeNames.isEmpty()) attributeNames.clear();
			shift(lookahead.toString(), 26); 
		} else if (isNonterminal(SUFFIX)) {
			goToState(25);
		} else {
			isValid = false; 
		}
	}
	
	private void s24() {
		if (isTerminal(TokenType.EOF)) {
			System.out.println("24.1 endTag ::= </ NAME >");
			reduce(ET, 3); 
		} else {
			isValid = false; 
		}
	}
	
	private void s25() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("25.1 elementPrefix ::= NAME attribute elementSuffix");
			reduce(PREFIX, 3);
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("25.2 elementPrefix ::= NAME attribute elementSuffix");
			reduce(PREFIX, 3);
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("25.3 elementPrefix ::= NAME attribute elementSuffix");
			reduce(PREFIX, 3);
		} else {
			isValid = false; 
		}
	}
	
	private void s26() {
		if (isTerminal(TokenType.LTSL)) {
			if (!tagNames.isEmpty()) tagNames.pop(); 
			System.out.println("26.1 elementSuffix ::= />");
			System.out.println("*** Closed with empty tag. No name matching performed. ***");
			reduce(SUFFIX, 1);
		} else if (isTerminal(TokenType.DATA)) {
			if (!tagNames.isEmpty()) tagNames.pop(); 
			System.out.println("26.2 elementSuffix ::= />");
			System.out.println("*** Closed with empty tag. No name matching performed. ***");
			reduce(SUFFIX, 1);
		} else if (isTerminal(TokenType.OPEN)) {
			if (!tagNames.isEmpty()) tagNames.pop(); 
			System.out.println("26.3 elementSuffix ::= />");
			System.out.println("*** Closed with empty tag. No name matching performed. ***");
			reduce(SUFFIX, 1);
		} else {
			isValid = false; 
		}
	}
	
	private void s27() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("27.1 elementOrData ::= EPSILON"); 
			symbols.push(EOD);
			goToState(28); 
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("27.2 elementOrData ::= EPSILON"); 
			symbols.push(EOD);
			goToState(28); 
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("27.3 elementOrData ::= EPSILON"); 
			symbols.push(EOD);
			goToState(28);  
		} else if (isNonterminal(EOD)) {
			goToState(28); 
		} else {
			isValid = false; 
		}
	}
	
	private void s28() {
		if (isTerminal(TokenType.LTSL)) {
			shift(lookahead.toString(), 30);
		} else if (isTerminal(TokenType.DATA)) {
			shift(lookahead.toString(), 18); 
		} else if (isTerminal(TokenType.OPEN)) {
			shift(lookahead.toString(), 17); 
		} else if (isNonterminal(ELEMENT)) {
			goToState(16); 
		} else if (isNonterminal(ET)) {
			goToState(29); 
		} else {
			isValid = false; 
		}
	}
	
	private void s29() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("29.1 elementSuffix ::= > elementOrData endTag");
			reduce(SUFFIX, 3);
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("29.2 elementSuffix ::= > elementOrData endTag");
			reduce(SUFFIX, 3);
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("29.3 elementSuffix ::= > elementOrData endTag");
			reduce(SUFFIX, 3);
		} else {
			isValid = false; 
		}
	}
	
	private void s30() {
		if (isTerminal(TokenType.NAME)) {
			matchTagName(lookahead); 
			shift(lookahead.toString(), 31); 
		} else {
			isValid = false; 
		}
	}
	
	private void s31() {
		if (isTerminal(TokenType.CLOSE)) {
			shift(lookahead.toString(), 32); 
		} else {
			isValid = false; 
		}
	}
	
	private void s32() {
		if (isTerminal(TokenType.LTSL)) {
			System.out.println("32.1 endTag ::= </ NAME >");
			reduce(ET, 3); 
		} else if (isTerminal(TokenType.DATA)) {
			System.out.println("32.2 endTag ::= </ NAME >");
			reduce(ET, 3); 
		} else if (isTerminal(TokenType.OPEN)) {
			System.out.println("32.3 endTag ::= </ NAME >");
			reduce(ET, 3); 
		} else {
			isValid = false; 
		}
	}
}

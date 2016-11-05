/*
 * CS 575: Project #2
 * File: Runner.java
 */
package org.aghannam.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.aghannam.lex.Lexer;
import org.aghannam.lex.Token;
import org.aghannam.parser.Parser;

/**
 * This class is the main driver that runs the shift-reduce parser for XML--. 
 * <p>
 * All dependencies are assumed to be present within the current class path, including the lexer, the parser,
 * and any input files (which should be .xml files stored in src/files). 
 * 
 * @author Ahmed Ghannam (amalghannam@crimson.ua.edu) <br>
 * CS-575: Project #2
 */
public class Runner {

	/**
	 * Main method through which to run the parser. 
	 * 
	 * @param args
	 * @throws Exception if any scanning error is encountered 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("*** Welcome to CS 575 Project #2: A Shift-Reduce Parser for XML-- ***\n");
		System.out.println("* Usage Tip 1: Type in 'grammar' to display the grammar used by this parser.");
		System.out.println("* Usage Tip 2: Type in 'exit' to quit without running the parser.");
		System.out.println("----------------------------------------------------------------------------------\n");
		// First, save the input file in a String 
		String document = read();
		System.out.println(); // skip a line 
		// Then pass it on to the lexer, which tokenizes it and gives it to the parser for processing
		parseDocument(document);
	}
	
	/**
	 * Prompts the user to enter an XML-- file to read.  
	 * <p>
	 * The file must be a valid XML-- document that resides in the src/files directory of this project. 
	 * Optionally, the user may type 'grammar' to display the grammar used by the parser. 
	 * 
	 * @return a <code>String</code> object whose value represents the file that was indicated by the user
	 */
	private static String read() {
		String fileName; 
		String curLine;
		StringBuilder sb = new StringBuilder();
		BufferedReader br;
		Scanner scan = new Scanner(System.in);
		
		try {
			while (true) {
				System.out.print("- Enter the name of the XML-- file to parse (e.g. input0.xml): ");
				fileName = scan.nextLine();
				
				boolean grammar = fileName.equalsIgnoreCase("grammar"); 
				boolean exit = fileName.equalsIgnoreCase("exit");

				if (!isXmlFile(fileName) && !grammar && !exit) {
					System.out.println("* Invalid file name. Please make sure you specify your input as an XML file.\n");
					continue;
				} else if (grammar) {
					System.out.println(getGrammar());
					continue;
				} else if (exit) {
					System.out.println("\nExited...");
					System.exit(0);
				} else {
					File dir = new File("src/files/" + fileName);
					br = new BufferedReader(new FileReader(dir));

					while ((curLine = br.readLine()) != null) {
						sb.append(curLine);
					}
					br.close();
					break;
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			scan.close();
		}
		
		return sb.toString();
	}

	/**
	 * Parses an XML-- document using shift-reduce and prints out a rightmost derivation. 
	 * 
	 * @param document the text representing the XML-- document to be parsed
	 * @throws Exception if a scanning error is encountered
	 */
	private static void parseDocument(String document) throws Exception {
		List<Token> tokens = new Lexer(document).getTokenStream();
		Parser parser = new Parser();
		parser.parse(tokens);
	}

	/**
	 * Verifies that the input file has an .xml extension. 
	 * 
	 * @param fileName the name of the input file indicated by the user (which must contain the extension)
	 * @return <code>true</code> if the file has extension .xml, <code>false</code> otherwise. 
	 */
	private static boolean isXmlFile(String fileName) {
		return fileName.endsWith(".xml");
	}
	
	/**
	 * Returns a string representing the LR(1) grammar used by this shift-reduce parser.
	 * 
	 * @return a string whose value is the grammar 
	 */
	private static String getGrammar() {
		return "\ns' ::= document\ndocument ::= element\nelement ::= < elementPrefix\n"
				+ "elementPrefix ::= NAME attribute elementSuffix\nattribute ::= attribute NAME = STRING\n"
				+ "attribute ::= EPSILON\nelementSuffix ::= > elementOrData endTag\nelementSuffix ::= />\n"
				+ "elementOrData ::= elementOrData element \nelementOrData ::= elementOrData DATA\n"
				+ "elementOrData ::= EPSILON\nendTag ::= </ NAME >\n\n* The grammar is left-recursive LR(1).\n**"
				+ " s' is a special augmented start rule (not printed in the final derivation).\n";
	}
}

## Mini-XML Shift-Reduce Parser
This program implements a shift-reduce parser for a fictional subset of XML. The LR(1) grammar recognized by the parser is the following:

```
s' ::= document
document ::= element
element ::= < elementPrefix
elementPrefix ::= NAME attribute elementSuffix
attribute ::= attribute NAME = STRING
attribute ::= EPSILON
elementSuffix ::= > elementOrData endTag
elementSuffix ::= />
elementOrData ::= elementOrData element 
elementOrData ::= elementOrData DATA
elementOrData ::= EPSILON
```

The grammar can also be rewritten in Extended BNF form as follows:

```
document  ::=  element
element   ::=  start_tag (element | DATA)* end_tag | empty_tag
start_tag ::=  < NAME attribute* >
end_tag   ::=  </ NAME >
empty_tag ::=  < NAME attribute* />
attribute ::=  NAME = STRING
```

Where the tokens/terminal symbols are ```NAME```, ```STRING```, ```DATA```, ```<```, ```>```, ```</```, ```/>```, and ```=```. The LR(1) grammar is left-recursive and is augmented by the special start symbol ```s'``` which is not printed in the final derivation. (That is to say, ```document``` is the actual start symbol.) 

## How to Run

Because all the input files reside within the project directory, it is best to run the parser directly from an IDE, such as Eclipse or NetBeans. You may therefore import the repository to your IDE and simply run the project from there. 

## Usage 

Given an XML document, the parser verifies if the document can be generated from the above grammar, in which case it prints out a rightmost derivation, in reverse order, that corresponds to a parse tree that generates the given input token sequence. This sequence is produced by the lexer, which tokenizes the input XML document into meaningful symbols (i.e. tokens). The derivation consists of the collection of grammar rules used to generate the input document, where each line displays exactly one grammar rule. 

### Example test case 

Consider the following (tiny) XML file:

```
<root.element attr.1="&quot;" attr.2='&apos;' attr.3="'" attr.4='"'>
	&lt; &amp; &gt;
</root.element>
```

When we run the parser on this file, the output is:

```
attribute ::= EPSILON
attribute ::= attribute NAME = STRING
attribute ::= attribute NAME = STRING
attribute ::= attribute NAME = STRING
attribute ::= attribute NAME = STRING
elementOrData ::= EPSILON
elementOrData ::= elementOrData DATA
elementOrData ::= elementOrData DATA
elementOrData ::= elementOrData DATA
endTag ::= </ NAME >
elementSuffix ::= > elementOrData endTag
elementPrefix ::= NAME attribute elementSuffix
element ::= < elementPrefix
document ::= element

Document parsed successfully!
```
This confirms the file is a valid document that can be generated from the specified grammar. Note the bottom-up structure of the output, which is a rightmost derivation that ends with the start symbol (the root of the parse tree). 

## Help 

For questions or inquiries, please contact me at amalghannam@crimson.ua.edu. 

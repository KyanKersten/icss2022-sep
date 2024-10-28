grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
VAR_IDENT: [A-Z] [a-zA-Z0-9\-]*;
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---

// Main Rules
stylesheet: (variableAssignment | stylerule)+;
stylerule: selector OPEN_BRACE declaration+ CLOSE_BRACE;
variableAssignment: variable ASSIGNMENT_OPERATOR expression SEMICOLON;

// Selectors
selector: ID_IDENT #idSelector | CLASS_IDENT #classSelector | LOWER_IDENT #tagSelector;

// Declarations
declaration: property COLON (expression | variable) SEMICOLON;
property: LOWER_IDENT;

// Expressions
expression:
  expression MUL expression #multiplyExpression
  | expression PLUS expression #addExpression
  | expression MIN expression #subtractExpression
  | value #valueExpression
  | variable #variableExpression;

// Values in expxressions
value: COLOR #color
     | PIXELSIZE #pixelSize
     | PERCENTAGE #percentage
     | TRUE #trueBoolean
     | FALSE #falseBoolean
     | SCALAR #scalar;

variable: VAR_IDENT;
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
IDENT: [A-Z] [a-zA-Z0-9\-]*;
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
// -- LEVEL 0, 1 --
stylesheet: stylerule+;
stylerule: variable_declaration*? selector OPEN_BRACE declaration+ CLOSE_BRACE;
variable_declaration: IDENT ASSIGNMENT_OPERATOR value SEMICOLON;
selector: ID_IDENT | CLASS_IDENT | LOWER_IDENT | CAPITAL_IDENT;
declaration: property value SEMICOLON;
property: LOWER_IDENT COLON;
value: variable | COLOR | PIXELSIZE | PERCENTAGE | TRUE | FALSE;
variable: IDENT;


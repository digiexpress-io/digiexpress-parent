lexer grammar HdesLexer;
ReservedKeyWord: 'switch' | 'case';

ScalarType
  : INTEGER
  | DECIMAL
  | DATE_TIME
  | DATE
  | TIME
  | STRING
  | BOOLEAN;
  
StaticMethod
  : SM_SUM
  | SM_IN
  | SM_AVG 
  | SM_MIN
  | SM_MAX;

// BETWEEN/AND/OR/IN operators
BETWEEN: B E T W E E N;
AND: A N D;
OR: O R;

// static method names
SM_SUM: S U M;
SM_AVG: A V G;
SM_MIN: M I N;
SM_MAX: M A X;
SM_IN: I N;

// array/list methods
FILTER: F I L T E R;
SORT: S O R T;
ASC: A S C;
DESC: D E S C;

FIND_FIRST: F I N D F I R S T;
FIND_ALL: F I N D A L L;

// Main definition types
FLOW: F L O W;
DECISION_TABLE: D E C I S I O N '-' T A B L E;
SERVICE_TASK: S E R V I C E '-' T A S K;
EXPRESSION: E X P R E S S I O N;

// data type options
DEBUG_VALUE: 'debug-value';

// possible data types
INTEGER: I N T E G E R;
DECIMAL: D E C I M A L;
DATE_TIME: D A T E T I M E;
DATE: D A T E;
TIME: T I M E;
STRING: S T R I N G;
BOOLEAN: B O O L E A N;

LAMBDA: '->';

// FLOW
AWAIT: A W A I T;
CONTINUE: C O N T I N U E;

// SERVICE
PROMISE: P R O M I S E;
TIMEOUT: T I M E O U T;

WHEN: W H E N;
ADD: A D D;
IF: I F;
ELSE: E L S E;

RETURN: R E T U R N;
AS: A S;
MAP: M A P;
TO: T O;

// MARKS
QUESTION_MARK: '?';
COLON: ':';
SCOLON: ';';
DOT: '.';
COMMA: ',';
NOT: '!';

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment X : [xX];
fragment Y : [yY];
fragment W : [wW];

PARENTHESES_START: '(';
PARENTHESES_END: ')';

ARRAY_START: '[';
ARRAY_END: ']';


// BLOCKS
BLOCK_START: '{';
BLOCK_END: '}';

// mathematical operators
PLUS: '+';
SUBTRACT: '-';
MULTIPLY: '*';
DIVIDE: '/';

// equality operators
EQ_NOTEQUAL: '!=';
EQ_EQUAL: '=';
EQ_LESS: '<';
EQ_LESS_THEN: '<=';
EQ_GREATER: '>';
EQ_GREATER_THEN: '>=';

// integer literal
IntegerLiteral: '0' | NonZeroDigit (Digits? | Underscores Digits);
Placeholder: '_' Digits?;

fragment Digit : '0' | NonZeroDigit;
fragment NonZeroDigit: [1-9];
fragment Digits: Digit (DigitsAndUnderscores? Digit)?;
fragment DigitsAndUnderscores: DigitOrUnderscore+;
fragment DigitOrUnderscore: Digit | '_';
fragment Underscores: '_'+;

// decimal literal
DecimalLiteral: Digits '.' Digits? | '.' Digits | Digits;
fragment SignedInteger: Sign? Digits;
fragment Sign: [+-];

// boolean literals
BooleanLiteral: 'true' | 'false' ;

// string literal
StringLiteral: '\'' Characters? '\'';
fragment Characters: Character+;
fragment Character: ~['\\] | Escape;

// things to escape
fragment Escape: '\\' [btnfr"'\\];

// naming convention
Identifier: Letters LettersAndDigits*;
fragment Letters: [a-zA-Z$_];
fragment LettersAndDigits: [a-zA-Z0-9$_];

// comments and white spaces
WHITE_SPACE : [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT_BLOCK : '/*' .*? '*/' -> channel(HIDDEN);
COMMENT_LINE : '//' ~[\r\n]* -> channel(HIDDEN);

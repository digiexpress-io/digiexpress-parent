grammar DialobRule;

@header {
import io.dialob.rule.parser.api.ValueType;
}

compileUnit: expr? EOF;

expr
    : op=NOT expr                                                       # notExpr
    | left=arithExprRule op=(NE|LE|GE|LT|GT|EQ) right=arithExprRule     # relationExpr
    | idExprRule NOT? op=MATCHES right=arithExprRule                    # matchesExpr
    | arithExprRule NOT? op=IN listExpr                                 # inOperExpr
    | expr op=AND expr                                                  # logicExpr
    | expr op=OR expr                                                   # logicExpr
    | LP expr RP                                                        # groupExpr
    | isExprRule                                                        # isExprRl
    | arithExprRule                                                     # arithExpr
    ;

listExpr
    : LP constExprRule (COMMA constExprRule)* RP
    | idExprRule
    ;

arithExprRule
    : MINUS arithExprRule                                            # negateExpr
    | left=arithExprRule op=(MUL|DIV) right=arithExprRule            # infixExpr
    | left=arithExprRule op=(PLUS|MINUS) right=arithExprRule         # infixExpr
    | func=ID LP (expr (',' expr)*)? RP                              # callExpr
    | left=ID OF right=reducerExprRule                               # ofExpr
    | LP arithExprRule RP                                            # parensExpr
    | constExprRule                                                  # constExpr
    | idExprRule                                                     # idExpr
    ;

isExprRule
    : questionId=ID IS not=NOT? status=('answered'|'valid'|'null'|'blank') # isExpr  
    ;

reducerExprRule
    : isExprRule
    | LP expr RP
    | idExprRule
    ;

idExprRule
    : var=ID
    ;

constExprRule
    locals [ValueType type = null]
    : value=(TRUE|FALSE)
    { $type = ValueType.BOOLEAN; }
    | value=INTEGER unit=(SECONDS|SECOND|MINUTES|MINUTE|HOURS|HOUR|DAYS|DAY|WEEK|WEEKS|MONTHS|MONTH|YEARS|YEAR|PERCENT)?
    {
        if ($unit == null) {
            $type = ValueType.INTEGER;
        } else if ($unit.type == SECONDS || $unit.type == SECOND || $unit.type == MINUTES || $unit.type == MINUTE || $unit.type == HOURS || $unit.type == HOUR) {
            $type = ValueType.DURATION;
        } else if ($unit.type == MONTHS || $unit.type == MONTH || $unit.type == YEARS || $unit.type == YEAR || $unit.type == DAYS || $unit.type == DAY || $unit.type == WEEK || $unit.type == WEEKS) {
            $type = ValueType.PERIOD;
        }
    }
    | value=REAL_NUMBER unit=PERCENT?
    {
        if ($unit == null) {
            $type = ValueType.DECIMAL;
        } else {
            $type = ValueType.PERCENT;
        }
    }
    | value=QUOTED_STRING
    { $type = ValueType.STRING; }
    ;

MATCHES
    : 'matches'
    ;
IN
    : 'in'
    ;
TRUE
    : 'true'
    ;
FALSE
    : 'false'
    ;
AND
    : 'and'
    ;
OR
    : 'or'
    ;
NOT
    : 'not'
    ;
IS
    : 'is'
    ;
OF
    : 'of'
    ;

SECONDS:
    'seconds';
SECOND:
    'second';
MINUTES:
    'minutes';
MINUTE:
    'minute';
HOURS:
    'hours';
HOUR:
    'hour';
DAYS:
    'days';
DAY:
    'day';
WEEKS:
    'weeks';
WEEK:
    'week';
MONTHS:
    'months';
MONTH:
    'month';
YEARS:
    'years';
YEAR:
    'year';
PERCENT:
    '%';

QUOTED_STRING
	:	'\'' ( '\'\'' | ~('\'') )* '\''
	|	'"'  ( '\\"'   | ~('"')  )* '"'
	;

ID
	:	( 'a' .. 'z' | 'A' .. 'Z' )
		( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '$' | '#' )*
	;

LP:
    '(';

RP:
    ')';

COMMA:
    ',';

NE:
    '!=';

GE:
    '>=';

LE:
    '<=';

GT:
    '>';

LT:
    '<';

EQ:
    '=';

PLUS:
    '+';

MINUS:
    '-';

MUL:
    '*';

DIV:
    '/';

POINT:
    '.';


INTEGER
    : N
    ;

REAL_NUMBER
	:	NUMBER_VALUE ( 'e' ( PLUS | MINUS )? N )?
	;

fragment
NUMBER_VALUE
	:	N POINT N
	|	POINT N
	|	N
	;
fragment
N
	: '0' .. '9' ( '0' .. '9' )*
	;

LINE_COMMENT
  : '//' ~[\r\n]* -> skip
  ;

WS	:	(' '|'\r'|'\t'|'\n') -> skip
	;

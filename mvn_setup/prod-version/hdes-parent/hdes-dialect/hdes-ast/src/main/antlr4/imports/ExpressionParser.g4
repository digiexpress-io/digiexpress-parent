parser grammar ExpressionParser;
options { tokenVocab = HdesLexer; }

literal
  : IntegerLiteral
  | DecimalLiteral
  | BooleanLiteral
  | StringLiteral;

placeholderRule: Placeholder;
placeholderTypeName: placeholderRule | simpleTypeName;

simpleTypeName: Identifier;
typeName: placeholderTypeName | typeName '.' placeholderTypeName;

methodName: simpleTypeName;

// method invocation
methodInvocation
  : staticMethod
  | instanceMethod
  | mappingMethod;

staticMethod: StaticMethod '(' methodArgs? ')';
instanceMethod: methodName '(' methodArgs? ')' ('.' instanceMethodChild)?;
instanceMethodChild: mappingMethod | typeName;
mappingMethod: typeName '.' mapMethod ('.' mappingMethodChild)?;
mappingMethodChild: mappingMethod | mapMethod;

mapMethod: MAP '(' lambdaExpression ')'  ('.' filterMethod)* ('.' sortMethod)* ('.' findFirstMethod)?;
filterMethod: FILTER '(' lambdaExpression ')';
sortMethod: SORT '(' lambdaExpression (',' ASC | DESC)? ')';
findFirstMethod: FIND_FIRST '(' ')';

methodArgs: methodArg (',' methodArg)*;
methodArg: expression;

primary
  : literal
  | typeName
  | '(' expression ')'
  | methodInvocation;

// final output
expressionUnit: expression;

// expressions
expression: conditionalExpression | primary | lambdaExpression;

// lambda
lambdaExpression: lambdaParameters '->' lambdaBody;
lambdaParameters: typeName | '(' typeName (',' typeName)* ')';
lambdaBody: expression | primary; 

conditionalExpression
  : conditionalOrExpression
  | conditionalOrExpression StaticMethod '(' expression (',' expression)* ')'
  | conditionalOrExpression BETWEEN expression AND conditionalExpression
  | conditionalOrExpression '?' expression ':' conditionalExpression; 

conditionalOrExpression
  : conditionalAndExpression
  | conditionalOrExpression OR conditionalAndExpression;

conditionalAndExpression
  : andExpression
  | conditionalAndExpression AND conditionalOrExpression;

andExpression
  : equalityExpression
  | andExpression AND equalityExpression;

equalityExpression
  : relationalExpression
  | equalityExpression '=' relationalExpression
  | equalityExpression '!=' relationalExpression;

relationalExpression
  : additiveExpression
  | relationalExpression '<' additiveExpression
  | relationalExpression '<=' additiveExpression
  | relationalExpression '>' additiveExpression
  | relationalExpression '>=' additiveExpression;

additiveExpression
  : multiplicativeExpression
  | additiveExpression '+' multiplicativeExpression
  | additiveExpression '-' multiplicativeExpression;

multiplicativeExpression
  : unaryExpression
  | multiplicativeExpression '*' unaryExpression
  | multiplicativeExpression '/' unaryExpression;

// unary operation is an operation with only one operand
unaryExpression
  : unaryExpressionNotPlusMinus
  | '+' unaryExpression
  | '-' unaryExpression
  | primary;
  
unaryExpressionNotPlusMinus: '!' unaryExpression;



parser grammar FlowParser;
options { tokenVocab = HdesLexer; }
import TypeDefParser, ExpressionParser;

flowUnit: simpleTypeName headers '{' steps '}';
steps: step*;
step: simpleTypeName '(' ')' '{' (iterateAction | callAction) stepAs? pointer '}';
stepAs: '.'? AS '(' mapping ')';

callAction: callDef*;
callDef: callAwait? simpleTypeName '(' mapping ')';
callAwait: AWAIT;

iterateAction: MAP '(' typeName ')' '.'? TO '(' '{' iterateBody '}' ')';
iterateBody: callAction | steps;

pointer: whenThenPointerArgs | thenPointer;
whenThenPointerArgs: (whenThenPointer (whenThenPointer)* elsePointer?) | thenPointer?; 
whenThenPointer: IF '(' expressionUnit ')' thenPointer;
elsePointer: ELSE thenPointer;

thenPointer
  : (RETURN (endAsPointer | simpleTypeName '(' ')')) 
  | continuePointer;
continuePointer: CONTINUE;
endAsPointer: mapping;
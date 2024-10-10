parser grammar ServiceTaskParser;
options { tokenVocab = HdesLexer; }
import TypeDefParser, ExpressionParser;


serviceTaskUnit: simpleTypeName headers '{' externalService '}';
promise: PROMISE '{' promiseTimeout? '}';
promiseTimeout: TIMEOUT ':' expressionUnit;
externalService: promise? typeName mapping; 
parser grammar TypeDefParser;
options { tokenVocab = HdesLexer; }
import ExpressionParser;

scalarType: ScalarType;

headers: '(' headersAccepts ')' ':' headersReturns;
headersAccepts: typeDefs;
headersReturns: typeDefs;

typeDefs: '{' (typeDef ( (',')? typeDef)*)? '}';
typeDef: typeDefNames ':' (arrayType | objectType | simpleType);
typeDefNames: typeDefName (',' typeDefName)*;
typeDefName: simpleTypeName optional?;

simpleType: scalarType (debugValue? | (formula | formulaOverAll)?);
objectType: typeDefs;
arrayType: (simpleType | objectType) '[' ']';

optional: '?';
debugValue: 'debug-value' '=' literal;
formula: '=' expressionUnit;
formulaOverAll: '=' '*' expressionUnit; // apply formula over array of elements


mapping: '{' (mappingArg (',' mappingArg)* )? '}';  
mappingArg: fieldMapping | fastMapping;
fieldMapping: simpleTypeName ':' mappingValue;
fastMapping: typeName;

mappingArray: '[' (mapping (',' mapping)*)? ']';
mappingValue: mapping | mappingArray | expressionUnit;
parser grammar HdesParser;
options { tokenVocab = HdesLexer; }
import DecisionTableParser, ExpressionParser, ServiceTaskParser, FlowParser;

hdesContent: hdesBody* EOF;
hdesBody: (
    FLOW flowUnit
  | DECISION_TABLE decisionTableUnit
  | SERVICE_TASK serviceTaskUnit
  | EXPRESSION '{' expressionUnit '}');
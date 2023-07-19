package io.dialob.rule.parser.node;

public interface ErrorLogger {
    void logError(String errorCode, Span span);

    void logError(String errorCode, Object[] args, Span span);

}

package org.fontory.fontorybe.font.domain.exception;

public class FontSQSProduceExcepetion extends RuntimeException {
    public FontSQSProduceExcepetion(String s) {
        super ("Failed to produce font request message with sqs queue " + s);
    }
}

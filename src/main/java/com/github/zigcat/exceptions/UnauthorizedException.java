package com.github.zigcat.exceptions;

public class UnauthorizedException extends RuntimeException {
    private long chatId;
    public UnauthorizedException(String message, long chatId){
        super(message);
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }
}

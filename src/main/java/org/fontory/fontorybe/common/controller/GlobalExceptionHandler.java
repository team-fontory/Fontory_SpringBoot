package org.fontory.fontorybe.common.controller;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.fontory.fontorybe.font.domain.exception.FontNotFoundException;
import org.fontory.fontorybe.font.domain.exception.FontOwnerMismatchException;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.domain.exception.MemberOwnerMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({MemberNotFoundException.class, FontNotFoundException.class})
    public BaseErrorResponse notFoundException(Exception e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(MemberDuplicateNameExistsException.class)
    public BaseErrorResponse memberDuplicateNameExists(MemberDuplicateNameExistsException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({MemberOwnerMismatchException.class, FontOwnerMismatchException.class})
    public BaseErrorResponse ownerMismatch(Exception e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(MemberAlreadyDisabledException.class)
    public BaseErrorResponse memberAlreadyDisabled(MemberAlreadyDisabledException e) {
        return new BaseErrorResponse(e.getMessage());
    }
}

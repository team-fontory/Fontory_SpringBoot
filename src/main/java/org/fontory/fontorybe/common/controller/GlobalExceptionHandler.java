package org.fontory.fontorybe.common.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.fontory.fontorybe.font.domain.exception.FontNotFoundException;
import org.fontory.fontorybe.font.domain.exception.FontOwnerMismatchException;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.domain.exception.MemberOwnerMismatchException;
import org.fontory.fontorybe.authentication.domain.exception.InvalidRefreshTokenException;
import org.fontory.fontorybe.authentication.domain.exception.TokenNotFoundException;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProvideNotFoundException.class)
    public BaseErrorResponse provideNotFound(ProvideNotFoundException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(MalformedJwtException.class)
    public BaseErrorResponse malformedToken(MalformedJwtException e) {
        return new BaseErrorResponse("Not a valid token");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JwtException.class)
    public BaseErrorResponse invalidToken(JwtException e) {
        return new BaseErrorResponse("Not a valid token");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExpiredJwtException.class)
    public BaseErrorResponse expiredToken(ExpiredJwtException e) {
        return new BaseErrorResponse("Expired token");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public BaseErrorResponse invalidRefreshToken(InvalidRefreshTokenException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(TokenNotFoundException.class)
    public BaseErrorResponse tokenNotFound(TokenNotFoundException e) {
        return new BaseErrorResponse(e.getMessage());
    }
}

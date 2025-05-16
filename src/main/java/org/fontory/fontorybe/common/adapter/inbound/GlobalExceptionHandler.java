package org.fontory.fontorybe.common.adapter.inbound;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.domain.exception.InvalidRefreshTokenException;
import org.fontory.fontorybe.authentication.domain.exception.TokenNotFoundException;
import org.fontory.fontorybe.bookmark.domain.exception.BookmarkAlreadyException;
import org.fontory.fontorybe.bookmark.domain.exception.BookmarkNotFoundException;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.fontory.fontorybe.file.adapter.inbound.exception.FileUploadException;
import org.fontory.fontorybe.file.adapter.inbound.exception.UnsupportedFileTypeException;
import org.fontory.fontorybe.file.domain.exception.FileNotFoundException;
import org.fontory.fontorybe.file.domain.exception.InvalidMultipartRequestException;
import org.fontory.fontorybe.file.domain.exception.SingleFileRequiredException;
import org.fontory.fontorybe.font.domain.exception.FontContainsBadWordException;
import org.fontory.fontorybe.font.domain.exception.FontDuplicateNameExistsException;
import org.fontory.fontorybe.font.domain.exception.FontInvalidStatusException;
import org.fontory.fontorybe.font.domain.exception.FontNotFoundException;
import org.fontory.fontorybe.font.domain.exception.FontOwnerMismatchException;
import org.fontory.fontorybe.font.domain.exception.FontSQSProduceExcepetion;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyExistException;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyJoinedException;
import org.fontory.fontorybe.member.domain.exception.MemberContainsBadWordException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.domain.exception.MemberOwnerMismatchException;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse validationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new BaseErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({MemberNotFoundException.class, FontNotFoundException.class, BookmarkNotFoundException.class})
    public BaseErrorResponse notFoundException(Exception e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({MemberDuplicateNameExistsException.class, FontDuplicateNameExistsException.class})
    public BaseErrorResponse duplicateNameExists(Exception e) {
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

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(MemberAlreadyExistException.class)
    public BaseErrorResponse memberAlreadyExist(MemberAlreadyExistException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FileUploadException.class)
    public BaseErrorResponse fileUploadException(FileUploadException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public BaseErrorResponse maxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return new BaseErrorResponse("Maximum upload size exceeded :" + e.getMaxUploadSize());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(BookmarkAlreadyException.class)
    public BaseErrorResponse bookmarkAlready(BookmarkAlreadyException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(FontSQSProduceExcepetion.class)
    public BaseErrorResponse SQSProduceException(FontSQSProduceExcepetion e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SingleFileRequiredException.class)
    public BaseErrorResponse singleFileRequiredException(SingleFileRequiredException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidMultipartRequestException.class)
    public BaseErrorResponse invalidMultipartRequest(InvalidMultipartRequestException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnsupportedFileTypeException.class)
    public BaseErrorResponse unsupportedFileType(UnsupportedFileTypeException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MemberAlreadyJoinedException.class)
    public BaseErrorResponse memberAlreadyJoined(MemberAlreadyJoinedException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FontInvalidStatusException.class)
    public BaseErrorResponse fontInvalidStatusException(FontInvalidStatusException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FileNotFoundException.class)
    public BaseErrorResponse fileNotFoundException(FileNotFoundException e) {
        return new BaseErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({FontContainsBadWordException.class, MemberContainsBadWordException.class})
    public BaseErrorResponse containsBadWordException(Exception e) {
        return new BaseErrorResponse(e.getMessage());
    }
}

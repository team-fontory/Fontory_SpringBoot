package org.fontory.fontorybe.common.adapter.inbound;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.domain.exception.AuthenticationRequiredException;
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

/**
 * 전역 예외 처리 핸들러
 * HTTP 상태 코드별로 예외를 그룹화하여 관리
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    // ========== 400 BAD REQUEST ==========
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return new BaseErrorResponse(message);
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            FileUploadException.class,
            SingleFileRequiredException.class,
            InvalidMultipartRequestException.class,
            UnsupportedFileTypeException.class,
            MemberAlreadyJoinedException.class,
            FontInvalidStatusException.class,
            FontContainsBadWordException.class,
            MemberContainsBadWordException.class
    })
    public BaseErrorResponse handleBadRequestExceptions(Exception e) {
        log.warn("Bad request: {}", e.getMessage());
        return new BaseErrorResponse(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public BaseErrorResponse handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        String message = String.format("파일 크기가 제한을 초과했습니다. (최대: %d bytes)", e.getMaxUploadSize());
        log.warn("File upload size exceeded: {}", e.getMaxUploadSize());
        return new BaseErrorResponse(message);
    }
    
    // ========== 401 UNAUTHORIZED ==========
    
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({MalformedJwtException.class, JwtException.class})
    public BaseErrorResponse handleInvalidToken(Exception e) {
        log.warn("Invalid JWT token: {}", e.getMessage());
        return new BaseErrorResponse("유효하지 않은 토큰입니다");
    }
    
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExpiredJwtException.class)
    public BaseErrorResponse handleExpiredToken(ExpiredJwtException e) {
        log.warn("Expired JWT token");
        return new BaseErrorResponse("토큰이 만료되었습니다");
    }
    
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
            InvalidRefreshTokenException.class,
            TokenNotFoundException.class,
            AuthenticationRequiredException.class
    })
    public BaseErrorResponse handleAuthenticationExceptions(Exception e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return new BaseErrorResponse(e.getMessage());
    }
    
    // ========== 403 FORBIDDEN ==========
    
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({
            MemberDuplicateNameExistsException.class,
            FontDuplicateNameExistsException.class
    })
    public BaseErrorResponse handleDuplicateNameExceptions(Exception e) {
        log.warn("Duplicate name: {}", e.getMessage());
        return new BaseErrorResponse(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({
            MemberOwnerMismatchException.class,
            FontOwnerMismatchException.class
    })
    public BaseErrorResponse handleOwnerMismatchExceptions(Exception e) {
        log.warn("Owner mismatch: {}", e.getMessage());
        return new BaseErrorResponse(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({
            MemberAlreadyDisabledException.class,
            MemberAlreadyExistException.class,
            BookmarkAlreadyException.class
    })
    public BaseErrorResponse handleAlreadyExistsExceptions(Exception e) {
        log.warn("Resource already exists: {}", e.getMessage());
        return new BaseErrorResponse(e.getMessage());
    }
    
    // ========== 404 NOT FOUND ==========
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            MemberNotFoundException.class,
            FontNotFoundException.class,
            BookmarkNotFoundException.class,
            ProvideNotFoundException.class
    })
    public BaseErrorResponse handleNotFoundExceptions(Exception e) {
        log.warn("Resource not found: {}", e.getMessage());
        return new BaseErrorResponse(e.getMessage());
    }
    
    // ========== 500 INTERNAL SERVER ERROR ==========
    
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FileNotFoundException.class)
    public BaseErrorResponse handleFileNotFoundException(FileNotFoundException e) {
        log.error("File not found in storage: {}", e.getMessage());
        return new BaseErrorResponse("파일 처리 중 오류가 발생했습니다");
    }
    
    // ========== 503 SERVICE UNAVAILABLE ==========
    
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(FontSQSProduceExcepetion.class)
    public BaseErrorResponse handleSQSProduceException(FontSQSProduceExcepetion e) {
        log.error("SQS produce failed: {}", e.getMessage());
        return new BaseErrorResponse("폰트 생성 요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요");
    }
    
}
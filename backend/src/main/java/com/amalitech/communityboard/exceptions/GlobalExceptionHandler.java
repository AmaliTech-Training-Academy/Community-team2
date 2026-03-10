package com.amalitech.communityboard.exceptions;

import com.amalitech.communityboard.dto.ResponseDto;
import jakarta.validation.ConstraintViolationException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.aop.AopInvocationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    private ResponseEntity<ResponseDto<Object>> build(HttpStatus status, String message, Object details) {
        ResponseDto<Object> dto = new ResponseDto<>(status, message, details);
        return new ResponseEntity<>(dto, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDto<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String,Object> details= new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("errors",ex.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
        details.put("path",request.getDescription(false));
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", details);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ResponseDto<Object>> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        Map<String,Object> details= new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("path",request.getDescription(false));
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDto<Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        Map<String,Object> details= new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("path",request.getDescription(false));
        details.put("parameter", ex.getName());
        details.put("requiredType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "");
        return build(HttpStatus.BAD_REQUEST, "Invalid parameter", details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDto<Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String,Object> details= new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("path",request.getDescription(false));
        return build(HttpStatus.BAD_REQUEST, ex.getMessage() == null ? "Invalid parameter" : ex.getMessage(), details);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ResponseDto<Object>> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request){
        Map<String,Object> details = new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("details",ex.getMessage());
        details.put("path",request.getDescription(false));
        return build(HttpStatus.NOT_FOUND, "Resource Not Found", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDto<Object>> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request){
        Map<String,Object> details = new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("details",ex.getMessage());
        details.put("path",request.getDescription(false));
        return build(HttpStatus.BAD_REQUEST, "Validations failed", details);
    }

    @ExceptionHandler(AopInvocationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseDto<Object>> handleAopInvocationException(AopInvocationException ex, WebRequest request){
        Map<String,Object> details = new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("details",ex.getMessage());
        details.put("path",request.getDescription(false));
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", details);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ResponseDto<Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("path", request != null ? request.getDescription(false) : "");
        details.put("code", "ACCESS_DENIED");
        details.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        return build(HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource.",
                details);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseDto<Object>> handleAllExceptions(Exception ex, WebRequest request){
        Map<String,Object> details = new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("message", ex.getMessage());
        details.put("path", request != null ? request.getDescription(false) : "");
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", details);
    }

    @ExceptionHandler(UserExists.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseDto<Object>> handleUserExists(UserExists userExists, WebRequest request){
        Map<String,Object> details = new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("message", userExists.getMessage());
        details.put("path", request != null ? request.getDescription(false) : "");
        return build(HttpStatus.CONFLICT, "User already exists", details);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseDto<Object>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request){
        Map<String,Object> details = new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("message", ex.getMessage());
        details.put("path", request != null ? request.getDescription(false) : "");
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", details);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseDto<Object>> handleAuthorizationDeniedException(AuthorizationDeniedException ex, WebRequest request){
        Map<String,Object> details = new HashMap<>();
        details.put("timestamp",LocalDateTime.now());
        details.put("message", ex.getMessage());
        details.put("path", request != null ? request.getDescription(false) : "");
        return build(HttpStatus.UNAUTHORIZED, "Access Denied", details);
    }
}

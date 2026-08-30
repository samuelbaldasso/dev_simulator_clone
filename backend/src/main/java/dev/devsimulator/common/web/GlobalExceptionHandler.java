package dev.devsimulator.common.web;

import dev.devsimulator.challenge.application.ChallengeNotFoundException;
import dev.devsimulator.challenge.application.ChallengeNotRunnableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
  }

  @ExceptionHandler(ChallengeNotFoundException.class)
  public ResponseEntity<ApiError> handleChallengeNotFound(ChallengeNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
  }

  @ExceptionHandler(ChallengeNotRunnableException.class)
  public ResponseEntity<ApiError> handleChallengeNotRunnable(ChallengeNotRunnableException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage()));
  }
}

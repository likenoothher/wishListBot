package com.aziarets.vividapp.exceptionhandling;

import com.aziarets.vividapp.exception.IllegalOperationException;
import com.aziarets.vividapp.exception.NotFoundUserIdException;
import com.aziarets.vividapp.exception.NotFoundUserNameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundUserNameException.class)
    public ResponseEntity<ApiResponse> handleNotFoundUserNameException(NotFoundUserNameException exception){
        ApiResponse apiResponse = new ApiResponse(exception.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotFoundUserIdException.class)
    public ResponseEntity<ApiResponse> handleNotFoundUserIdException(NotFoundUserIdException exception){
        ApiResponse apiResponse = new ApiResponse(exception.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalOperationException.class)
    public ResponseEntity<ApiResponse> handleIllegalOperationException(IllegalOperationException exception){
        ApiResponse apiResponse = new ApiResponse(exception.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}

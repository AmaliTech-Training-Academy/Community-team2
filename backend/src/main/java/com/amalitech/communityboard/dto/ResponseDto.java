package com.amalitech.communityboard.dto;

import org.springframework.http.HttpStatus;

public record ResponseDto<T>(HttpStatus status, String message, T data) {
}

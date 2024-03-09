package com.pbu.sam.exceptions;

import java.time.LocalDateTime;

public record WendiError(String path, String message, int statusCode, LocalDateTime date) {
}

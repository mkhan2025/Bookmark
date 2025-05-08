package com.example.bookmark.utils;

public class PasswordValidator {
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }

        if (password.length() < 6) {
            return new ValidationResult(false, "Password must be at least 6 characters long");
        }

        boolean hasNumber = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        if (!hasNumber) {
            return new ValidationResult(false, "Password must contain at least one number");
        }

        if (!hasSpecialChar) {
            return new ValidationResult(false, "Password must contain at least one special character");
        }

        return new ValidationResult(true, null);
    }
} 
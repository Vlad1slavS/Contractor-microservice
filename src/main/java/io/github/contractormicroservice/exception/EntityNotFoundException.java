package io.github.contractormicroservice.exception;

/**
 * Кастомный класс ошибки при поиске несуществующего объекта
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

}


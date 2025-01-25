package ec.edu.espe.pos.exception;

public class EncryptionException extends RuntimeException {
    
    private final String data;
    private final String operation;

    public EncryptionException(String data, String operation) {
        super();
        this.data = data;
        this.operation = operation;
    }

    @Override
    public String getMessage() {
        return "Error en operación de encriptación: " + this.operation + ", con datos: " + data;
    }
} 
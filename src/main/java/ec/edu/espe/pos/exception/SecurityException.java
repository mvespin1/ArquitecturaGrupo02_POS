package ec.edu.espe.pos.exception;

public class SecurityException extends RuntimeException {
    
    private final String data;
    private final String securityContext;

    public SecurityException(String data, String securityContext) {
        super();
        this.data = data;
        this.securityContext = securityContext;
    }

    @Override
    public String getMessage() {
        return "Error de seguridad en " + this.securityContext + ": " + this.data;
    }
} 
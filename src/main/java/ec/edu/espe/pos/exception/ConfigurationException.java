package ec.edu.espe.pos.exception;

public class ConfigurationException extends RuntimeException {
    
    private final String data;
    private final String configurationType;

    public ConfigurationException(String data, String configurationType) {
        super();
        this.data = data;
        this.configurationType = configurationType;
    }

    @Override
    public String getMessage() {
        return "Error en configuración de " + this.configurationType + ": " + this.data;
    }
} 
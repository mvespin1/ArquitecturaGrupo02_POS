package ec.edu.espe.pos.exception;

public class NotFoundException extends RuntimeException {

    private final String data;
    private final String entity;

    public NotFoundException(String data, String entity) {
        super();
        this.data = data;
        this.entity = entity;
    }

    @Override
    public String getMessage() {
        return "No se encontró ninguna coincidencia para: " + this.entity + ", con el dato: " + data;
    }
} 
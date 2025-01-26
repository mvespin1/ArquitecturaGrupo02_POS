package ec.edu.espe.pos.exception;

public class DuplicateException extends RuntimeException {

    private final String data;
    private final String entity;

    public DuplicateException(String data, String entity) {
        super();
        this.data = data;
        this.entity = entity;
    }

    @Override
    public String getMessage() {
        return "Ya existe un(a) " + this.entity + " con el dato: " + data;
    }
}

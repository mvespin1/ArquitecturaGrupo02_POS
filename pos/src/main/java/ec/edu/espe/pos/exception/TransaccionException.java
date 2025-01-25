package ec.edu.espe.pos.exception;

public class TransaccionException extends RuntimeException {

    private final String operacion;
    private final String detalle;

    public TransaccionException(String operacion, String detalle) {
        super();
        this.operacion = operacion;
        this.detalle = detalle;
    }

    @Override
    public String getMessage() {
        return String.format("Error en la operación %s: %s", this.operacion, this.detalle);
    }

    public String getOperacion() {
        return operacion;
    }

    public String getDetalle() {
        return detalle;
    }
} 
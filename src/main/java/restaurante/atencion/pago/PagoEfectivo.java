package restaurante.atencion.pago;

public class PagoEfectivo extends MetodoPago {

    public PagoEfectivo() {
        super("EFECTIVO");
    }

    @Override
    public String generarReferencia(String referencia) {
        return "EF-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}

package restaurante.atencion.model;

public class Comensal {

    private final String alias;

    public Comensal(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El alias del comensal no puede estar vacio."
            );
        }

        this.alias = alias.trim();
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return alias;
    }
}

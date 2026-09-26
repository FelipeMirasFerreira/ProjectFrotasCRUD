package aulas.umc.frota.model.valueObjects;

/** Numero de registro da CNH: 11 digitos (pontuacao e ignorada). */
public final class Cnh {
    private final String valor;

    public Cnh(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Numero da CNH obrigatorio.");
        }
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("CNH deve ter 11 digitos.");
        }
        if (digitos.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CNH invalida: " + valor);
        }
        this.valor = digitos;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cnh outra && outra.valor.equals(valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}

package aulas.umc.frota.model.valueObjects;

public final class Nome {
    private final String valor;

    public Nome(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatorio.");
        }
        String limpo = valor.trim().replaceAll("\\s+", " ");
        if (limpo.length() < 3 || limpo.length() > 120) {
            throw new IllegalArgumentException("Nome deve ter entre 3 e 120 caracteres.");
        }
        this.valor = limpo;
    }

    public String getValor() {
        return valor;
    }
}

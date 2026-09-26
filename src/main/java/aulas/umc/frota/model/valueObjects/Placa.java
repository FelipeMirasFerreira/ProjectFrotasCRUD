package aulas.umc.frota.model.valueObjects;

import java.util.regex.Pattern;

/** Placa brasileira: padrao antigo (ABC1234) ou Mercosul (ABC1D23). Armazenada sem hifen. */
public final class Placa {
    private static final Pattern FORMATO = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    private final String valor;

    public Placa(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Placa obrigatoria.");
        }
        String normalizada = valor.replace("-", "").replace(" ", "").toUpperCase();
        if (!FORMATO.matcher(normalizada).matches()) {
            throw new IllegalArgumentException("Placa invalida: " + valor + ". Use ABC1234 ou ABC1D23.");
        }
        this.valor = normalizada;
    }

    public String getValor() {
        return valor;
    }

    /** ABC-1234 para o padrao antigo; ABC1D23 (sem hifen) para o Mercosul. */
    public String formatada() {
        boolean padraoAntigo = Character.isDigit(valor.charAt(4));
        return padraoAntigo ? valor.substring(0, 3) + "-" + valor.substring(3) : valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Placa outra && outra.valor.equals(valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}

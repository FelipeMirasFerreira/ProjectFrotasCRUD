package aulas.umc.frota.model.valueObjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Litros {
    private static final BigDecimal MAXIMO = new BigDecimal("1000");

    private final BigDecimal valor;

    public Litros(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Litros obrigatorio.");
        }
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("Litros deve ser maior que zero.");
        }
        if (valor.compareTo(MAXIMO) > 0) {
            throw new IllegalArgumentException("Litros acima do limite de " + MAXIMO + " por abastecimento.");
        }
        this.valor = valor.setScale(3, RoundingMode.HALF_UP);
    }

    public BigDecimal getValor() {
        return valor;
    }
}

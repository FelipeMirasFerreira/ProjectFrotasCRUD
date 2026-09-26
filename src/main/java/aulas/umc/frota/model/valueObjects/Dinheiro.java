package aulas.umc.frota.model.valueObjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Dinheiro {
    private final BigDecimal valor;

    public Dinheiro(BigDecimal valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(campo + " obrigatorio.");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(campo + " nao pode ser negativo.");
        }
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValor() {
        return valor;
    }

    public boolean ehZero() {
        return valor.signum() == 0;
    }
}

package aulas.umc.frota.model.valueObjects;

public final class Quilometragem {
    private static final long MAXIMO = 9_999_999L;

    private final long valor;

    public Quilometragem(long valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Quilometragem nao pode ser negativa.");
        }
        if (valor > MAXIMO) {
            throw new IllegalArgumentException("Quilometragem acima do limite do hodometro (" + MAXIMO + ").");
        }
        this.valor = valor;
    }

    public static Quilometragem de(Long valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(campo + " obrigatoria.");
        }
        return new Quilometragem(valor);
    }

    public long getValor() {
        return valor;
    }

    public boolean menorQue(Quilometragem outra) {
        return valor < outra.valor;
    }

    public boolean maiorQue(Quilometragem outra) {
        return valor > outra.valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Quilometragem outra && outra.valor == valor;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(valor);
    }
}

package aulas.umc.frota.model;

public enum TipoCombustivel {
    GASOLINA, ETANOL, DIESEL, GNV;

    public static TipoCombustivel de(String valor) {
        return Enums.de(TipoCombustivel.class, valor, "Combustivel");
    }
}

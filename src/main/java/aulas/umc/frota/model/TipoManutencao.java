package aulas.umc.frota.model;

public enum TipoManutencao {
    /** Revisao periodica: reinicia o contador do alerta de revisao. */
    REVISAO,
    PREVENTIVA,
    CORRETIVA;

    public static TipoManutencao de(String valor) {
        return Enums.de(TipoManutencao.class, valor, "Tipo de manutencao");
    }
}

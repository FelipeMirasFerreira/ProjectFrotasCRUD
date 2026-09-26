package aulas.umc.frota.model;

public enum SituacaoRevisao {
    /** Ainda falta mais que a antecedencia configurada. */
    EM_DIA,
    /** Faltam antecedencia-km ou menos para a revisao. */
    PROXIMA,
    /** A quilometragem da revisao ja foi atingida ou ultrapassada. */
    VENCIDA
}

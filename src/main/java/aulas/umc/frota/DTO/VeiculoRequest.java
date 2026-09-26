package aulas.umc.frota.DTO;

/**
 * @param intervaloRevisaoKm opcional; padrao 10.000 km
 * @param kmUltimaRevisao    opcional; hodometro na ultima revisao feita antes do cadastro (padrao 0)
 */
public record VeiculoRequest(
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Long quilometragemAtual,
        Integer intervaloRevisaoKm,
        Long kmUltimaRevisao) {
}

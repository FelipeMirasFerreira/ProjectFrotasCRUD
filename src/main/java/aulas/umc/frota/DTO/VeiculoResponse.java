package aulas.umc.frota.DTO;

import aulas.umc.frota.model.SituacaoRevisao;
import aulas.umc.frota.model.Veiculo;

import java.time.LocalDate;
import java.util.UUID;

public record VeiculoResponse(
        UUID id,
        String placa,
        String marca,
        String modelo,
        int ano,
        long quilometragemAtual,
        int intervaloRevisaoKm,
        long kmUltimaRevisao,
        LocalDate dataUltimaRevisao,
        long proximaRevisaoKm,
        long kmRestanteRevisao,
        SituacaoRevisao situacaoRevisao) {

    public static VeiculoResponse de(Veiculo veiculo, long antecedenciaAlertaKm) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getPlaca().formatada(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getQuilometragemAtual().getValor(),
                veiculo.getIntervaloRevisaoKm(),
                veiculo.getKmUltimaRevisao().getValor(),
                veiculo.getDataUltimaRevisao(),
                veiculo.proximaRevisaoKm(),
                veiculo.kmRestanteParaRevisao(),
                veiculo.situacaoRevisao(antecedenciaAlertaKm));
    }
}

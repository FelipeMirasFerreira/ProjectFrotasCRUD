package aulas.umc.frota.DTO;

import aulas.umc.frota.model.SituacaoRevisao;
import aulas.umc.frota.model.Veiculo;

import java.util.UUID;

public record AlertaRevisaoResponse(
        UUID veiculoId,
        String placa,
        String veiculo,
        long quilometragemAtual,
        long proximaRevisaoKm,
        long kmRestante,
        SituacaoRevisao situacao,
        String mensagem) {

    public static AlertaRevisaoResponse de(Veiculo v, long antecedenciaAlertaKm) {
        SituacaoRevisao situacao = v.situacaoRevisao(antecedenciaAlertaKm);
        long restante = v.kmRestanteParaRevisao();
        String placa = v.getPlaca().formatada();

        String mensagem = switch (situacao) {
            case VENCIDA -> "Revisao do " + placa + " vencida ha " + formatar(-restante) + " km. Agende imediatamente.";
            case PROXIMA -> "Revisao do " + placa + " em " + formatar(restante) + " km (aos " + formatar(v.proximaRevisaoKm()) + " km).";
            case EM_DIA -> "Revisao do " + placa + " em dia. Proxima aos " + formatar(v.proximaRevisaoKm()) + " km.";
        };

        return new AlertaRevisaoResponse(
                v.getId(),
                placa,
                v.getMarca() + " " + v.getModelo() + " (" + v.getAno() + ")",
                v.getQuilometragemAtual().getValor(),
                v.proximaRevisaoKm(),
                restante,
                situacao,
                mensagem);
    }

    private static String formatar(long km) {
        return String.format("%,d", km).replace(',', '.');
    }
}

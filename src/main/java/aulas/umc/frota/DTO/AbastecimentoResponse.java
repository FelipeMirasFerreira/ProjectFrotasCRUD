package aulas.umc.frota.DTO;

import aulas.umc.frota.model.Abastecimento;
import aulas.umc.frota.model.TipoCombustivel;
import aulas.umc.frota.model.valueObjects.Placa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @param consumoKmPorLitro km rodados desde o abastecimento anterior do mesmo veiculo / litros deste
 *                          abastecimento (metodo "tanque cheio"). Nulo no primeiro abastecimento.
 */
public record AbastecimentoResponse(
        UUID id,
        UUID veiculoId,
        String placa,
        UUID motoristaId,
        String motoristaNome,
        LocalDate data,
        long quilometragem,
        BigDecimal litros,
        BigDecimal valorTotal,
        BigDecimal precoPorLitro,
        TipoCombustivel combustivel,
        BigDecimal consumoKmPorLitro) {

    public static AbastecimentoResponse de(Abastecimento a, String placa, String motoristaNome, BigDecimal consumo) {
        return new AbastecimentoResponse(
                a.getId(),
                a.getVeiculoId(),
                new Placa(placa).formatada(),
                a.getMotoristaId(),
                motoristaNome,
                a.getData(),
                a.getQuilometragem().getValor(),
                a.getLitros().getValor(),
                a.getValorTotal().getValor(),
                a.precoPorLitro(),
                a.getCombustivel(),
                consumo);
    }
}

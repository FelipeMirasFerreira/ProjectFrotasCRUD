package aulas.umc.frota.DTO;

import aulas.umc.frota.model.Manutencao;
import aulas.umc.frota.model.TipoManutencao;
import aulas.umc.frota.model.valueObjects.Placa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ManutencaoResponse(
        UUID id,
        UUID veiculoId,
        String placa,
        LocalDate data,
        long quilometragem,
        TipoManutencao tipo,
        String descricao,
        BigDecimal custo,
        String oficina) {

    public static ManutencaoResponse de(Manutencao m, String placa) {
        return new ManutencaoResponse(
                m.getId(),
                m.getVeiculoId(),
                new Placa(placa).formatada(),
                m.getData(),
                m.getQuilometragem().getValor(),
                m.getTipo(),
                m.getDescricao(),
                m.getCusto().getValor(),
                m.getOficina());
    }
}

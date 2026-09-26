package aulas.umc.frota.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** @param tipo REVISAO, PREVENTIVA ou CORRETIVA. Somente REVISAO reinicia o alerta de revisao. */
public record ManutencaoRequest(
        UUID veiculoId,
        LocalDate data,
        Long quilometragem,
        String tipo,
        String descricao,
        BigDecimal custo,
        String oficina) {
}

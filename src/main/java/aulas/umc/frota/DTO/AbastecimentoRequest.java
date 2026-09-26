package aulas.umc.frota.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AbastecimentoRequest(
        UUID veiculoId,
        UUID motoristaId,
        LocalDate data,
        Long quilometragem,
        BigDecimal litros,
        BigDecimal valorTotal,
        String combustivel) {
}

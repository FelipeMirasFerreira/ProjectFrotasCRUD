package aulas.umc.frota.DTO;

import java.time.LocalDate;

public record MotoristaRequest(
        String nome,
        String cnh,
        String categoriaCnh,
        LocalDate validadeCnh,
        String telefone) {
}

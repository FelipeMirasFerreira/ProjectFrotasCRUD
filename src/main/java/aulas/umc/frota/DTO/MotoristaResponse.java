package aulas.umc.frota.DTO;

import aulas.umc.frota.model.CategoriaCnh;
import aulas.umc.frota.model.Motorista;

import java.time.LocalDate;
import java.util.UUID;

public record MotoristaResponse(
        UUID id,
        String nome,
        String cnh,
        CategoriaCnh categoriaCnh,
        LocalDate validadeCnh,
        String telefone,
        boolean cnhVencida) {

    public static MotoristaResponse de(Motorista motorista, LocalDate hoje) {
        return new MotoristaResponse(
                motorista.getId(),
                motorista.getNome().getValor(),
                motorista.getCnh().getValor(),
                motorista.getCategoriaCnh(),
                motorista.getValidadeCnh(),
                motorista.getTelefone(),
                motorista.cnhVencidaEm(hoje));
    }
}

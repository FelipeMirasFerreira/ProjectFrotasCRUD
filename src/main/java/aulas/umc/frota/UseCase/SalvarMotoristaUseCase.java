package aulas.umc.frota.UseCase;

import aulas.umc.frota.DTO.MotoristaRequest;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.exception.RegraNegocioException;
import aulas.umc.frota.model.CategoriaCnh;
import aulas.umc.frota.model.Motorista;
import aulas.umc.frota.model.valueObjects.Cnh;
import aulas.umc.frota.model.valueObjects.Nome;
import aulas.umc.frota.repository.MotoristaJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SalvarMotoristaUseCase {

    private final MotoristaJdbcRepository motoristaRepository;

    public SalvarMotoristaUseCase(MotoristaJdbcRepository motoristaRepository) {
        this.motoristaRepository = motoristaRepository;
    }

    public Motorista cadastrar(MotoristaRequest request) {
        Cnh cnh = new Cnh(request.cnh());
        validarCnhUnica(cnh, null);

        Motorista motorista = new Motorista(
                new Nome(request.nome()),
                cnh,
                CategoriaCnh.de(request.categoriaCnh()),
                request.validadeCnh(),
                request.telefone());

        motoristaRepository.inserir(motorista);
        return motorista;
    }

    public Motorista atualizar(UUID id, MotoristaRequest request) {
        Motorista motorista = motoristaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista nao encontrado: " + id));

        Cnh cnh = new Cnh(request.cnh());
        validarCnhUnica(cnh, id);

        motorista.alterarDados(
                new Nome(request.nome()),
                cnh,
                CategoriaCnh.de(request.categoriaCnh()),
                request.validadeCnh(),
                request.telefone());

        motoristaRepository.atualizar(motorista);
        return motorista;
    }

    private void validarCnhUnica(Cnh cnh, UUID ignorarId) {
        if (motoristaRepository.existeCnh(cnh, ignorarId)) {
            throw new RegraNegocioException("Ja existe um motorista com a CNH " + cnh.getValor() + ".");
        }
    }
}

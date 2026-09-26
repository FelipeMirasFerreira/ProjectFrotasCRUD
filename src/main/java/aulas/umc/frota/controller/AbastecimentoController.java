package aulas.umc.frota.controller;

import aulas.umc.frota.DTO.AbastecimentoRequest;
import aulas.umc.frota.DTO.AbastecimentoResponse;
import aulas.umc.frota.UseCase.ConsultarAbastecimentosUseCase;
import aulas.umc.frota.UseCase.RegistrarAbastecimentoUseCase;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.repository.AbastecimentoJdbcRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Abastecimentos sao eventos: podem ser registrados, consultados e excluidos (logicamente),
 * mas nao editados. Para corrigir, exclua e registre de novo.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/abastecimentos")
public class AbastecimentoController {

    private final RegistrarAbastecimentoUseCase registrarAbastecimento;
    private final ConsultarAbastecimentosUseCase consultarAbastecimentos;
    private final AbastecimentoJdbcRepository abastecimentoRepository;

    public AbastecimentoController(RegistrarAbastecimentoUseCase registrarAbastecimento,
                                   ConsultarAbastecimentosUseCase consultarAbastecimentos,
                                   AbastecimentoJdbcRepository abastecimentoRepository) {
        this.registrarAbastecimento = registrarAbastecimento;
        this.consultarAbastecimentos = consultarAbastecimentos;
        this.abastecimentoRepository = abastecimentoRepository;
    }

    @PostMapping
    public ResponseEntity<AbastecimentoResponse> registrar(@RequestBody AbastecimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarAbastecimento.executar(request));
    }

    @GetMapping
    public List<AbastecimentoResponse> listar(@RequestParam(required = false) UUID veiculoId) {
        return consultarAbastecimentos.listar(veiculoId);
    }

    @GetMapping("/{id}")
    public AbastecimentoResponse buscarPorId(@PathVariable UUID id) {
        return consultarAbastecimentos.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        if (!abastecimentoRepository.excluir(id)) {
            throw new RecursoNaoEncontradoException("Abastecimento nao encontrado: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}

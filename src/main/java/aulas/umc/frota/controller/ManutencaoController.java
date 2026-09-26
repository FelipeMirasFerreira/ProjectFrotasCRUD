package aulas.umc.frota.controller;

import aulas.umc.frota.DTO.ManutencaoRequest;
import aulas.umc.frota.DTO.ManutencaoResponse;
import aulas.umc.frota.UseCase.RegistrarManutencaoUseCase;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.repository.ManutencaoJdbcRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/manutencoes")
public class ManutencaoController {

    private final RegistrarManutencaoUseCase registrarManutencao;
    private final ManutencaoJdbcRepository manutencaoRepository;

    public ManutencaoController(RegistrarManutencaoUseCase registrarManutencao,
                                ManutencaoJdbcRepository manutencaoRepository) {
        this.registrarManutencao = registrarManutencao;
        this.manutencaoRepository = manutencaoRepository;
    }

    @PostMapping
    public ResponseEntity<ManutencaoResponse> registrar(@RequestBody ManutencaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarManutencao.executar(request));
    }

    @GetMapping
    public List<ManutencaoResponse> listar(@RequestParam(required = false) UUID veiculoId) {
        return manutencaoRepository.listar(veiculoId).stream()
                .map(d -> ManutencaoResponse.de(d.manutencao(), d.placa()))
                .toList();
    }

    @GetMapping("/{id}")
    public ManutencaoResponse buscarPorId(@PathVariable UUID id) {
        return manutencaoRepository.buscarPorId(id)
                .map(d -> ManutencaoResponse.de(d.manutencao(), d.placa()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutencao nao encontrada: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        if (!manutencaoRepository.excluir(id)) {
            throw new RecursoNaoEncontradoException("Manutencao nao encontrada: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}

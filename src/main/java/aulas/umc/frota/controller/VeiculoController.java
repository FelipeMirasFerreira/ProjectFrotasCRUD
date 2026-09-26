package aulas.umc.frota.controller;

import aulas.umc.frota.DTO.VeiculoRequest;
import aulas.umc.frota.DTO.VeiculoResponse;
import aulas.umc.frota.UseCase.ConsultarAlertasRevisaoUseCase;
import aulas.umc.frota.UseCase.SalvarVeiculoUseCase;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.model.Veiculo;
import aulas.umc.frota.repository.VeiculoJdbcRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final SalvarVeiculoUseCase salvarVeiculo;
    private final VeiculoJdbcRepository veiculoRepository;
    private final long antecedenciaKm;

    public VeiculoController(SalvarVeiculoUseCase salvarVeiculo,
                             VeiculoJdbcRepository veiculoRepository,
                             ConsultarAlertasRevisaoUseCase alertas) {
        this.salvarVeiculo = salvarVeiculo;
        this.veiculoRepository = veiculoRepository;
        this.antecedenciaKm = alertas.getAntecedenciaKm();
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@RequestBody VeiculoRequest request) {
        Veiculo veiculo = salvarVeiculo.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponse.de(veiculo, antecedenciaKm));
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        return veiculoRepository.listar().stream()
                .map(v -> VeiculoResponse.de(v, antecedenciaKm))
                .toList();
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscarPorId(@PathVariable UUID id) {
        return veiculoRepository.buscarPorId(id)
                .map(v -> VeiculoResponse.de(v, antecedenciaKm))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + id));
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable UUID id, @RequestBody VeiculoRequest request) {
        return VeiculoResponse.de(salvarVeiculo.atualizar(id, request), antecedenciaKm);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        if (!veiculoRepository.excluir(id)) {
            throw new RecursoNaoEncontradoException("Veiculo nao encontrado: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}

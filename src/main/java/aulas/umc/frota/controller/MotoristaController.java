package aulas.umc.frota.controller;

import aulas.umc.frota.DTO.MotoristaRequest;
import aulas.umc.frota.DTO.MotoristaResponse;
import aulas.umc.frota.UseCase.SalvarMotoristaUseCase;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.model.Motorista;
import aulas.umc.frota.repository.MotoristaJdbcRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/motoristas")
public class MotoristaController {

    private final SalvarMotoristaUseCase salvarMotorista;
    private final MotoristaJdbcRepository motoristaRepository;
    private final Clock clock;

    public MotoristaController(SalvarMotoristaUseCase salvarMotorista,
                               MotoristaJdbcRepository motoristaRepository,
                               Clock clock) {
        this.salvarMotorista = salvarMotorista;
        this.motoristaRepository = motoristaRepository;
        this.clock = clock;
    }

    @PostMapping
    public ResponseEntity<MotoristaResponse> cadastrar(@RequestBody MotoristaRequest request) {
        Motorista motorista = salvarMotorista.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MotoristaResponse.de(motorista, hoje()));
    }

    @GetMapping
    public List<MotoristaResponse> listar() {
        LocalDate hoje = hoje();
        return motoristaRepository.listar().stream()
                .map(m -> MotoristaResponse.de(m, hoje))
                .toList();
    }

    @GetMapping("/{id}")
    public MotoristaResponse buscarPorId(@PathVariable UUID id) {
        return motoristaRepository.buscarPorId(id)
                .map(m -> MotoristaResponse.de(m, hoje()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista nao encontrado: " + id));
    }

    @PutMapping("/{id}")
    public MotoristaResponse atualizar(@PathVariable UUID id, @RequestBody MotoristaRequest request) {
        return MotoristaResponse.de(salvarMotorista.atualizar(id, request), hoje());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        if (!motoristaRepository.excluir(id)) {
            throw new RecursoNaoEncontradoException("Motorista nao encontrado: " + id);
        }
        return ResponseEntity.noContent().build();
    }

    private LocalDate hoje() {
        return LocalDate.now(clock);
    }
}

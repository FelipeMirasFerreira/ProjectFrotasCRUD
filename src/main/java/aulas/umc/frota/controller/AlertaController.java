package aulas.umc.frota.controller;

import aulas.umc.frota.DTO.AlertaRevisaoResponse;
import aulas.umc.frota.UseCase.ConsultarAlertasRevisaoUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/alertas")
public class AlertaController {

    private final ConsultarAlertasRevisaoUseCase consultarAlertas;

    public AlertaController(ConsultarAlertasRevisaoUseCase consultarAlertas) {
        this.consultarAlertas = consultarAlertas;
    }

    /**
     * Veiculos com revisao VENCIDA ou PROXIMA, do mais urgente para o menos urgente.
     * Use ?todos=true para incluir tambem os que estao EM_DIA.
     */
    @GetMapping("/revisao")
    public List<AlertaRevisaoResponse> revisao(@RequestParam(defaultValue = "false") boolean todos) {
        return consultarAlertas.executar(todos);
    }
}

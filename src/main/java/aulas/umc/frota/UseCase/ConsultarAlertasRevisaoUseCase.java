package aulas.umc.frota.UseCase;

import aulas.umc.frota.DTO.AlertaRevisaoResponse;
import aulas.umc.frota.model.SituacaoRevisao;
import aulas.umc.frota.model.Veiculo;
import aulas.umc.frota.repository.VeiculoJdbcRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ConsultarAlertasRevisaoUseCase {

    private final VeiculoJdbcRepository veiculoRepository;
    private final long antecedenciaKm;

    public ConsultarAlertasRevisaoUseCase(VeiculoJdbcRepository veiculoRepository,
                                          @Value("${frota.revisao.antecedencia-km:1000}") long antecedenciaKm) {
        this.veiculoRepository = veiculoRepository;
        this.antecedenciaKm = antecedenciaKm;
    }

    /**
     * @param incluirEmDia false = somente VENCIDA e PROXIMA (o que exige acao)
     * @return ordenado do mais urgente (menor km restante) para o menos urgente
     */
    public List<AlertaRevisaoResponse> executar(boolean incluirEmDia) {
        return veiculoRepository.listar().stream()
                .filter(Veiculo::estaAtivo)
                .map(v -> AlertaRevisaoResponse.de(v, antecedenciaKm))
                .filter(a -> incluirEmDia || a.situacao() != SituacaoRevisao.EM_DIA)
                .sorted(Comparator.comparingLong(AlertaRevisaoResponse::kmRestante))
                .toList();
    }

    public long getAntecedenciaKm() {
        return antecedenciaKm;
    }
}

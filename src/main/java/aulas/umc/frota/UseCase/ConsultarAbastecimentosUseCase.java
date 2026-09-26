package aulas.umc.frota.UseCase;

import aulas.umc.frota.DTO.AbastecimentoResponse;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.model.Abastecimento;
import aulas.umc.frota.repository.AbastecimentoJdbcRepository;
import aulas.umc.frota.repository.AbastecimentoJdbcRepository.AbastecimentoDetalhe;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultarAbastecimentosUseCase {

    private final AbastecimentoJdbcRepository abastecimentoRepository;

    public ConsultarAbastecimentosUseCase(AbastecimentoJdbcRepository abastecimentoRepository) {
        this.abastecimentoRepository = abastecimentoRepository;
    }

    public List<AbastecimentoResponse> listar(UUID veiculoId) {
        List<AbastecimentoDetalhe> detalhes = abastecimentoRepository.listar(veiculoId);
        Map<UUID, BigDecimal> consumos = calcularConsumos(detalhes);
        return detalhes.stream()
                .map(d -> paraResponse(d, consumos))
                .toList();
    }

    public AbastecimentoResponse buscarPorId(UUID id) {
        AbastecimentoDetalhe detalhe = abastecimentoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Abastecimento nao encontrado: " + id));
        // o consumo depende do abastecimento anterior do mesmo veiculo
        Map<UUID, BigDecimal> consumos = calcularConsumos(
                abastecimentoRepository.listar(detalhe.abastecimento().getVeiculoId()));
        return paraResponse(detalhe, consumos);
    }

    /**
     * Metodo "tanque cheio": consumo = (km deste - km do abastecimento anterior) / litros deste.
     * Calculado por veiculo, em ordem de quilometragem. O primeiro abastecimento de cada veiculo fica sem consumo.
     */
    static Map<UUID, BigDecimal> calcularConsumos(List<AbastecimentoDetalhe> detalhes) {
        Map<UUID, List<Abastecimento>> porVeiculo = detalhes.stream()
                .map(AbastecimentoDetalhe::abastecimento)
                .collect(Collectors.groupingBy(Abastecimento::getVeiculoId));

        Map<UUID, BigDecimal> consumos = new HashMap<>();
        for (List<Abastecimento> lista : porVeiculo.values()) {
            lista.sort(Comparator.comparingLong((Abastecimento a) -> a.getQuilometragem().getValor())
                    .thenComparing(Abastecimento::getData));
            for (int i = 1; i < lista.size(); i++) {
                Abastecimento atual = lista.get(i);
                long kmRodados = atual.getQuilometragem().getValor() - lista.get(i - 1).getQuilometragem().getValor();
                if (kmRodados > 0) {
                    consumos.put(atual.getId(), BigDecimal.valueOf(kmRodados)
                            .divide(atual.getLitros().getValor(), 2, RoundingMode.HALF_UP));
                }
            }
        }
        return consumos;
    }

    private static AbastecimentoResponse paraResponse(AbastecimentoDetalhe d, Map<UUID, BigDecimal> consumos) {
        return AbastecimentoResponse.de(d.abastecimento(), d.placa(), d.motoristaNome(),
                consumos.get(d.abastecimento().getId()));
    }
}

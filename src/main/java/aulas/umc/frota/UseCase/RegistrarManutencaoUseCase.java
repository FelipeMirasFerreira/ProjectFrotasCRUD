package aulas.umc.frota.UseCase;

import aulas.umc.frota.DTO.ManutencaoRequest;
import aulas.umc.frota.DTO.ManutencaoResponse;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.exception.RegraNegocioException;
import aulas.umc.frota.model.Manutencao;
import aulas.umc.frota.model.TipoManutencao;
import aulas.umc.frota.model.Veiculo;
import aulas.umc.frota.model.valueObjects.Dinheiro;
import aulas.umc.frota.model.valueObjects.Quilometragem;
import aulas.umc.frota.repository.ManutencaoJdbcRepository;
import aulas.umc.frota.repository.VeiculoJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class RegistrarManutencaoUseCase {

    private final VeiculoJdbcRepository veiculoRepository;
    private final ManutencaoJdbcRepository manutencaoRepository;
    private final Clock clock;

    public RegistrarManutencaoUseCase(VeiculoJdbcRepository veiculoRepository,
                                      ManutencaoJdbcRepository manutencaoRepository,
                                      Clock clock) {
        this.veiculoRepository = veiculoRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.clock = clock;
    }

    /**
     * Grava a manutencao e atualiza o veiculo na mesma transacao.
     * REVISAO reinicia o ciclo do alerta (km da ultima revisao = km desta manutencao).
     */
    @Transactional
    public ManutencaoResponse executar(ManutencaoRequest request) {
        if (request.veiculoId() == null) {
            throw new IllegalArgumentException("Veiculo obrigatorio.");
        }

        LocalDate hoje = LocalDate.now(clock);
        LocalDate data = request.data() != null ? request.data() : hoje;
        if (data.isAfter(hoje)) {
            throw new RegraNegocioException("Data da manutencao nao pode ser futura.");
        }

        Veiculo veiculo = veiculoRepository.buscarPorIdParaAtualizar(request.veiculoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + request.veiculoId()));

        Manutencao manutencao = new Manutencao(
                veiculo.getId(),
                data,
                Quilometragem.de(request.quilometragem(), "Quilometragem"),
                TipoManutencao.de(request.tipo()),
                request.descricao(),
                new Dinheiro(request.custo(), "Custo"),
                request.oficina());

        if (manutencao.ehRevisao()) {
            veiculo.registrarRevisao(manutencao.getQuilometragem(), data);
        } else {
            veiculo.atualizarQuilometragemSeMaior(manutencao.getQuilometragem());
        }

        veiculoRepository.atualizar(veiculo);
        manutencaoRepository.inserir(manutencao);

        return ManutencaoResponse.de(manutencao, veiculo.getPlaca().getValor());
    }
}

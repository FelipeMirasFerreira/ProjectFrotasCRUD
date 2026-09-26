package aulas.umc.frota.UseCase;

import aulas.umc.frota.DTO.AbastecimentoRequest;
import aulas.umc.frota.DTO.AbastecimentoResponse;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.exception.RegraNegocioException;
import aulas.umc.frota.model.Abastecimento;
import aulas.umc.frota.model.Motorista;
import aulas.umc.frota.model.TipoCombustivel;
import aulas.umc.frota.model.Veiculo;
import aulas.umc.frota.model.valueObjects.Dinheiro;
import aulas.umc.frota.model.valueObjects.Litros;
import aulas.umc.frota.model.valueObjects.Quilometragem;
import aulas.umc.frota.repository.AbastecimentoJdbcRepository;
import aulas.umc.frota.repository.MotoristaJdbcRepository;
import aulas.umc.frota.repository.VeiculoJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class RegistrarAbastecimentoUseCase {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VeiculoJdbcRepository veiculoRepository;
    private final MotoristaJdbcRepository motoristaRepository;
    private final AbastecimentoJdbcRepository abastecimentoRepository;
    private final ConsultarAbastecimentosUseCase consultarAbastecimentos;
    private final Clock clock;

    public RegistrarAbastecimentoUseCase(VeiculoJdbcRepository veiculoRepository,
                                         MotoristaJdbcRepository motoristaRepository,
                                         AbastecimentoJdbcRepository abastecimentoRepository,
                                         ConsultarAbastecimentosUseCase consultarAbastecimentos,
                                         Clock clock) {
        this.veiculoRepository = veiculoRepository;
        this.motoristaRepository = motoristaRepository;
        this.abastecimentoRepository = abastecimentoRepository;
        this.consultarAbastecimentos = consultarAbastecimentos;
        this.clock = clock;
    }

    /**
     * Grava o abastecimento e avanca o hodometro do veiculo na MESMA transacao:
     * se qualquer passo falhar, nada e gravado.
     */
    @Transactional
    public AbastecimentoResponse executar(AbastecimentoRequest request) {
        if (request.veiculoId() == null || request.motoristaId() == null) {
            throw new IllegalArgumentException("Veiculo e motorista sao obrigatorios.");
        }

        LocalDate hoje = LocalDate.now(clock);
        LocalDate data = request.data() != null ? request.data() : hoje;
        if (data.isAfter(hoje)) {
            throw new RegraNegocioException("Data do abastecimento nao pode ser futura.");
        }

        Veiculo veiculo = veiculoRepository.buscarPorIdParaAtualizar(request.veiculoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + request.veiculoId()));
        if (!veiculo.estaAtivo()) {
            throw new RegraNegocioException("Veiculo " + veiculo.getPlaca().formatada() + " nao esta ativo.");
        }

        Motorista motorista = motoristaRepository.buscarPorId(request.motoristaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista nao encontrado: " + request.motoristaId()));
        if (!motorista.estaAtivo()) {
            throw new RegraNegocioException("Motorista " + motorista.getNome().getValor() + " nao esta ativo.");
        }
        if (motorista.cnhVencidaEm(data)) {
            throw new RegraNegocioException("CNH de " + motorista.getNome().getValor() + " vencida em "
                    + motorista.getValidadeCnh().format(DATA_BR) + ".");
        }

        Abastecimento abastecimento = new Abastecimento(
                veiculo.getId(),
                motorista.getId(),
                data,
                Quilometragem.de(request.quilometragem(), "Quilometragem"),
                new Litros(request.litros()),
                new Dinheiro(request.valorTotal(), "Valor total"),
                TipoCombustivel.de(request.combustivel()));

        veiculo.registrarQuilometragem(abastecimento.getQuilometragem());

        veiculoRepository.atualizar(veiculo);
        abastecimentoRepository.inserir(abastecimento);

        return consultarAbastecimentos.buscarPorId(abastecimento.getId());
    }
}

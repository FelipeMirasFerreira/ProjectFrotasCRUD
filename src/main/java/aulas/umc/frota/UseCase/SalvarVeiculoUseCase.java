package aulas.umc.frota.UseCase;

import aulas.umc.frota.DTO.VeiculoRequest;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.exception.RegraNegocioException;
import aulas.umc.frota.model.Veiculo;
import aulas.umc.frota.model.valueObjects.Placa;
import aulas.umc.frota.model.valueObjects.Quilometragem;
import aulas.umc.frota.repository.VeiculoJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SalvarVeiculoUseCase {

    private final VeiculoJdbcRepository veiculoRepository;

    public SalvarVeiculoUseCase(VeiculoJdbcRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    public Veiculo cadastrar(VeiculoRequest request) {
        Placa placa = new Placa(request.placa());
        validarPlacaUnica(placa, null);

        Veiculo veiculo = new Veiculo(
                placa,
                request.marca(),
                request.modelo(),
                obrigatorio(request.ano(), "Ano"),
                Quilometragem.de(request.quilometragemAtual(), "Quilometragem atual"),
                request.intervaloRevisaoKm() != null ? request.intervaloRevisaoKm() : Veiculo.INTERVALO_REVISAO_PADRAO_KM,
                new Quilometragem(request.kmUltimaRevisao() != null ? request.kmUltimaRevisao() : 0));

        veiculoRepository.inserir(veiculo);
        return veiculo;
    }

    /**
     * Atualiza os dados cadastrais. A quilometragem so pode aumentar (use para corrigir o hodometro
     * sem registrar abastecimento); o km da ultima revisao pode ser corrigido manualmente.
     */
    @Transactional
    public Veiculo atualizar(UUID id, VeiculoRequest request) {
        Veiculo veiculo = veiculoRepository.buscarPorIdParaAtualizar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + id));

        Placa placa = new Placa(request.placa());
        validarPlacaUnica(placa, id);

        veiculo.alterarDados(
                placa,
                request.marca(),
                request.modelo(),
                obrigatorio(request.ano(), "Ano"),
                request.intervaloRevisaoKm() != null ? request.intervaloRevisaoKm() : veiculo.getIntervaloRevisaoKm());

        if (request.quilometragemAtual() != null) {
            veiculo.registrarQuilometragem(new Quilometragem(request.quilometragemAtual()));
        }
        if (request.kmUltimaRevisao() != null) {
            veiculo.corrigirUltimaRevisao(new Quilometragem(request.kmUltimaRevisao()));
        }

        veiculoRepository.atualizar(veiculo);
        return veiculo;
    }

    private void validarPlacaUnica(Placa placa, UUID ignorarId) {
        if (veiculoRepository.existePlaca(placa, ignorarId)) {
            throw new RegraNegocioException("Ja existe um veiculo com a placa " + placa.formatada() + ".");
        }
    }

    private static int obrigatorio(Integer valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(campo + " obrigatorio.");
        }
        return valor;
    }
}

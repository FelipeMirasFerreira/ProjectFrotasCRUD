package aulas.umc.frota.model;

import aulas.umc.frota.model.valueObjects.Dinheiro;
import aulas.umc.frota.model.valueObjects.Quilometragem;

import java.time.LocalDate;
import java.util.UUID;

public class Manutencao extends Domain {
    private final UUID veiculoId;
    private final LocalDate data;
    private final Quilometragem quilometragem;
    private final TipoManutencao tipo;
    private final String descricao;
    private final Dinheiro custo;
    private final String oficina;

    public Manutencao(UUID veiculoId, LocalDate data, Quilometragem quilometragem, TipoManutencao tipo,
                      String descricao, Dinheiro custo, String oficina) {
        this(null, veiculoId, data, quilometragem, tipo, descricao, custo, oficina, Status.ATIVO);
    }

    public Manutencao(UUID id, UUID veiculoId, LocalDate data, Quilometragem quilometragem, TipoManutencao tipo,
                      String descricao, Dinheiro custo, String oficina, Status status) {
        super(id, status);
        if (veiculoId == null) {
            throw new IllegalArgumentException("Veiculo obrigatorio.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data da manutencao obrigatoria.");
        }
        if (quilometragem == null || tipo == null || custo == null) {
            throw new IllegalArgumentException("Quilometragem, tipo e custo sao obrigatorios.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descricao obrigatoria.");
        }
        if (descricao.trim().length() > 500) {
            throw new IllegalArgumentException("Descricao deve ter no maximo 500 caracteres.");
        }
        this.veiculoId = veiculoId;
        this.data = data;
        this.quilometragem = quilometragem;
        this.tipo = tipo;
        this.descricao = descricao.trim();
        this.custo = custo;
        this.oficina = oficina != null && !oficina.isBlank() ? oficina.trim() : null;
    }

    public boolean ehRevisao() {
        return tipo == TipoManutencao.REVISAO;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public LocalDate getData() {
        return data;
    }

    public Quilometragem getQuilometragem() {
        return quilometragem;
    }

    public TipoManutencao getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Dinheiro getCusto() {
        return custo;
    }

    public String getOficina() {
        return oficina;
    }
}

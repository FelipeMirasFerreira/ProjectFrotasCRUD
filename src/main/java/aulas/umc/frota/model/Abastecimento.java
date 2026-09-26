package aulas.umc.frota.model;

import aulas.umc.frota.model.valueObjects.Dinheiro;
import aulas.umc.frota.model.valueObjects.Litros;
import aulas.umc.frota.model.valueObjects.Quilometragem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class Abastecimento extends Domain {
    private final UUID veiculoId;
    private final UUID motoristaId;
    private final LocalDate data;
    private final Quilometragem quilometragem;
    private final Litros litros;
    private final Dinheiro valorTotal;
    private final TipoCombustivel combustivel;

    public Abastecimento(UUID veiculoId, UUID motoristaId, LocalDate data, Quilometragem quilometragem,
                         Litros litros, Dinheiro valorTotal, TipoCombustivel combustivel) {
        this(null, veiculoId, motoristaId, data, quilometragem, litros, valorTotal, combustivel, Status.ATIVO);
    }

    public Abastecimento(UUID id, UUID veiculoId, UUID motoristaId, LocalDate data, Quilometragem quilometragem,
                         Litros litros, Dinheiro valorTotal, TipoCombustivel combustivel, Status status) {
        super(id, status);
        if (veiculoId == null || motoristaId == null) {
            throw new IllegalArgumentException("Veiculo e motorista sao obrigatorios.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data do abastecimento obrigatoria.");
        }
        if (quilometragem == null || litros == null || valorTotal == null || combustivel == null) {
            throw new IllegalArgumentException("Quilometragem, litros, valor e combustivel sao obrigatorios.");
        }
        if (valorTotal.ehZero()) {
            throw new IllegalArgumentException("Valor total deve ser maior que zero.");
        }
        this.veiculoId = veiculoId;
        this.motoristaId = motoristaId;
        this.data = data;
        this.quilometragem = quilometragem;
        this.litros = litros;
        this.valorTotal = valorTotal;
        this.combustivel = combustivel;
    }

    public BigDecimal precoPorLitro() {
        return valorTotal.getValor().divide(litros.getValor(), 3, RoundingMode.HALF_UP);
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public UUID getMotoristaId() {
        return motoristaId;
    }

    public LocalDate getData() {
        return data;
    }

    public Quilometragem getQuilometragem() {
        return quilometragem;
    }

    public Litros getLitros() {
        return litros;
    }

    public Dinheiro getValorTotal() {
        return valorTotal;
    }

    public TipoCombustivel getCombustivel() {
        return combustivel;
    }
}

package aulas.umc.frota.repository;

import aulas.umc.frota.model.Abastecimento;
import aulas.umc.frota.model.Status;
import aulas.umc.frota.model.TipoCombustivel;
import aulas.umc.frota.model.valueObjects.Dinheiro;
import aulas.umc.frota.model.valueObjects.Litros;
import aulas.umc.frota.model.valueObjects.Quilometragem;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AbastecimentoJdbcRepository extends JdbcRepository {

    /** Abastecimento + dados de exibicao vindos do JOIN (placa e nome do motorista). */
    public record AbastecimentoDetalhe(Abastecimento abastecimento, String placa, String motoristaNome) {
    }

    private static final String INSERT = "INSERT INTO abastecimento (id, veiculo_id, motorista_id, data, quilometragem, litros, "
            + "valor_total, combustivel, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT = "SELECT a.id, a.veiculo_id, a.motorista_id, a.data, a.quilometragem, a.litros, "
            + "a.valor_total, a.combustivel, a.status, v.placa, m.nome AS motorista_nome "
            + "FROM abastecimento a "
            + "JOIN veiculo v ON v.id = a.veiculo_id "
            + "JOIN motorista m ON m.id = a.motorista_id "
            + "WHERE a.status != 3";

    private static final String ORDEM = " ORDER BY a.data DESC, a.quilometragem DESC";

    public AbastecimentoJdbcRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void inserir(Abastecimento abastecimento) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(INSERT)) {
            ps.setObject(1, abastecimento.getId());
            ps.setObject(2, abastecimento.getVeiculoId());
            ps.setObject(3, abastecimento.getMotoristaId());
            ps.setDate(4, Date.valueOf(abastecimento.getData()));
            ps.setLong(5, abastecimento.getQuilometragem().getValor());
            ps.setBigDecimal(6, abastecimento.getLitros().getValor());
            ps.setBigDecimal(7, abastecimento.getValorTotal().getValor());
            ps.setString(8, abastecimento.getCombustivel().name());
            ps.setShort(9, abastecimento.getStatus().getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Erro inserindo abastecimento", e);
        }
    }

    /** @param veiculoId filtro opcional (null = todos os veiculos) */
    public List<AbastecimentoDetalhe> listar(UUID veiculoId) {
        String sql = SELECT + (veiculoId != null ? " AND a.veiculo_id = ?" : "") + ORDEM;
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (veiculoId != null) {
                ps.setObject(1, veiculoId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<AbastecimentoDetalhe> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando abastecimentos", e);
        }
    }

    public Optional<AbastecimentoDetalhe> buscarPorId(UUID id) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + " AND a.id = ?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando abastecimento por id", e);
        }
    }

    public boolean excluir(UUID id) {
        return excluirLogico("abastecimento", id);
    }

    private AbastecimentoDetalhe mapRow(ResultSet rs) throws SQLException {
        Abastecimento abastecimento = new Abastecimento(
                rs.getObject("id", UUID.class),
                rs.getObject("veiculo_id", UUID.class),
                rs.getObject("motorista_id", UUID.class),
                rs.getDate("data").toLocalDate(),
                new Quilometragem(rs.getLong("quilometragem")),
                new Litros(rs.getBigDecimal("litros")),
                new Dinheiro(rs.getBigDecimal("valor_total"), "Valor total"),
                TipoCombustivel.de(rs.getString("combustivel")),
                Status.deCodigo(rs.getShort("status")));
        return new AbastecimentoDetalhe(abastecimento, rs.getString("placa"), rs.getString("motorista_nome"));
    }
}

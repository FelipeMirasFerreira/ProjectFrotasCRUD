package aulas.umc.frota.repository;

import aulas.umc.frota.model.Manutencao;
import aulas.umc.frota.model.Status;
import aulas.umc.frota.model.TipoManutencao;
import aulas.umc.frota.model.valueObjects.Dinheiro;
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
public class ManutencaoJdbcRepository extends JdbcRepository {

    /** Manutencao + placa do veiculo (JOIN), para exibicao. */
    public record ManutencaoDetalhe(Manutencao manutencao, String placa) {
    }

    private static final String INSERT = "INSERT INTO manutencao (id, veiculo_id, data, quilometragem, tipo, descricao, custo, "
            + "oficina, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT = "SELECT mt.id, mt.veiculo_id, mt.data, mt.quilometragem, mt.tipo, mt.descricao, "
            + "mt.custo, mt.oficina, mt.status, v.placa "
            + "FROM manutencao mt JOIN veiculo v ON v.id = mt.veiculo_id "
            + "WHERE mt.status != 3";

    private static final String ORDEM = " ORDER BY mt.data DESC, mt.quilometragem DESC";

    public ManutencaoJdbcRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void inserir(Manutencao manutencao) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(INSERT)) {
            ps.setObject(1, manutencao.getId());
            ps.setObject(2, manutencao.getVeiculoId());
            ps.setDate(3, Date.valueOf(manutencao.getData()));
            ps.setLong(4, manutencao.getQuilometragem().getValor());
            ps.setString(5, manutencao.getTipo().name());
            ps.setString(6, manutencao.getDescricao());
            ps.setBigDecimal(7, manutencao.getCusto().getValor());
            ps.setString(8, manutencao.getOficina());
            ps.setShort(9, manutencao.getStatus().getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Erro inserindo manutencao", e);
        }
    }

    /** @param veiculoId filtro opcional (null = todos os veiculos) */
    public List<ManutencaoDetalhe> listar(UUID veiculoId) {
        String sql = SELECT + (veiculoId != null ? " AND mt.veiculo_id = ?" : "") + ORDEM;
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (veiculoId != null) {
                ps.setObject(1, veiculoId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ManutencaoDetalhe> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando manutencoes", e);
        }
    }

    public Optional<ManutencaoDetalhe> buscarPorId(UUID id) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + " AND mt.id = ?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando manutencao por id", e);
        }
    }

    public boolean excluir(UUID id) {
        return excluirLogico("manutencao", id);
    }

    private ManutencaoDetalhe mapRow(ResultSet rs) throws SQLException {
        Manutencao manutencao = new Manutencao(
                rs.getObject("id", UUID.class),
                rs.getObject("veiculo_id", UUID.class),
                rs.getDate("data").toLocalDate(),
                new Quilometragem(rs.getLong("quilometragem")),
                TipoManutencao.de(rs.getString("tipo")),
                rs.getString("descricao"),
                new Dinheiro(rs.getBigDecimal("custo"), "Custo"),
                rs.getString("oficina"),
                Status.deCodigo(rs.getShort("status")));
        return new ManutencaoDetalhe(manutencao, rs.getString("placa"));
    }
}

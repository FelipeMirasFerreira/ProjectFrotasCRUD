package aulas.umc.frota.repository;

import aulas.umc.frota.model.Status;
import aulas.umc.frota.model.Veiculo;
import aulas.umc.frota.model.valueObjects.Placa;
import aulas.umc.frota.model.valueObjects.Quilometragem;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VeiculoJdbcRepository extends JdbcRepository {

    private static final String COLUNAS = "id, placa, marca, modelo, ano, quilometragem_atual, intervalo_revisao_km, "
            + "km_ultima_revisao, data_ultima_revisao, status";

    private static final String INSERT = "INSERT INTO veiculo (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE veiculo SET placa = ?, marca = ?, modelo = ?, ano = ?, quilometragem_atual = ?, "
            + "intervalo_revisao_km = ?, km_ultima_revisao = ?, data_ultima_revisao = ? WHERE id = ? AND status != 3";
    private static final String SELECT_ALL = "SELECT " + COLUNAS + " FROM veiculo WHERE status != 3 ORDER BY placa";
    private static final String SELECT_BY_ID = "SELECT " + COLUNAS + " FROM veiculo WHERE id = ? AND status != 3";
    private static final String SELECT_BY_ID_FOR_UPDATE = SELECT_BY_ID + " FOR UPDATE";
    private static final String EXISTS_PLACA = "SELECT 1 FROM veiculo WHERE placa = ? AND id != ? AND status != 3";

    public VeiculoJdbcRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void inserir(Veiculo veiculo) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(INSERT)) {
            ps.setObject(1, veiculo.getId());
            ps.setString(2, veiculo.getPlaca().getValor());
            ps.setString(3, veiculo.getMarca());
            ps.setString(4, veiculo.getModelo());
            ps.setInt(5, veiculo.getAno());
            ps.setLong(6, veiculo.getQuilometragemAtual().getValor());
            ps.setInt(7, veiculo.getIntervaloRevisaoKm());
            ps.setLong(8, veiculo.getKmUltimaRevisao().getValor());
            ps.setDate(9, toSqlDate(veiculo.getDataUltimaRevisao()));
            ps.setShort(10, veiculo.getStatus().getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Erro inserindo veiculo", e);
        }
    }

    public boolean atualizar(Veiculo veiculo) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(UPDATE)) {
            ps.setString(1, veiculo.getPlaca().getValor());
            ps.setString(2, veiculo.getMarca());
            ps.setString(3, veiculo.getModelo());
            ps.setInt(4, veiculo.getAno());
            ps.setLong(5, veiculo.getQuilometragemAtual().getValor());
            ps.setInt(6, veiculo.getIntervaloRevisaoKm());
            ps.setLong(7, veiculo.getKmUltimaRevisao().getValor());
            ps.setDate(8, toSqlDate(veiculo.getDataUltimaRevisao()));
            ps.setObject(9, veiculo.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Erro atualizando veiculo", e);
        }
    }

    public List<Veiculo> listar() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            List<Veiculo> veiculos = new ArrayList<>();
            while (rs.next()) {
                veiculos.add(mapRow(rs));
            }
            return veiculos;
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando veiculos", e);
        }
    }

    public Optional<Veiculo> buscarPorId(UUID id) {
        return buscar(SELECT_BY_ID, id);
    }

    /**
     * Busca travando a linha ate o fim da transacao (SELECT ... FOR UPDATE).
     * Evita que dois abastecimentos simultaneos leiam o mesmo km e um sobrescreva o outro.
     */
    public Optional<Veiculo> buscarPorIdParaAtualizar(UUID id) {
        return buscar(SELECT_BY_ID_FOR_UPDATE, id);
    }

    public boolean existePlaca(Placa placa, UUID ignorarId) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(EXISTS_PLACA)) {
            ps.setString(1, placa.getValor());
            ps.setObject(2, ignorarId != null ? ignorarId : new UUID(0, 0));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro verificando placa", e);
        }
    }

    public boolean excluir(UUID id) {
        return excluirLogico("veiculo", id);
    }

    private Optional<Veiculo> buscar(String sql, UUID id) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando veiculo por id", e);
        }
    }

    private Veiculo mapRow(ResultSet rs) throws SQLException {
        Date dataUltimaRevisao = rs.getDate("data_ultima_revisao");
        return new Veiculo(
                rs.getObject("id", UUID.class),
                new Placa(rs.getString("placa")),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("ano"),
                new Quilometragem(rs.getLong("quilometragem_atual")),
                rs.getInt("intervalo_revisao_km"),
                new Quilometragem(rs.getLong("km_ultima_revisao")),
                dataUltimaRevisao != null ? dataUltimaRevisao.toLocalDate() : null,
                Status.deCodigo(rs.getShort("status")));
    }

    private static Date toSqlDate(LocalDate data) {
        return data != null ? Date.valueOf(data) : null;
    }
}

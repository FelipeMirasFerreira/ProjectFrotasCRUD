package aulas.umc.frota.repository;

import aulas.umc.frota.model.CategoriaCnh;
import aulas.umc.frota.model.Motorista;
import aulas.umc.frota.model.Status;
import aulas.umc.frota.model.valueObjects.Cnh;
import aulas.umc.frota.model.valueObjects.Nome;
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
public class MotoristaJdbcRepository extends JdbcRepository {

    private static final String COLUNAS = "id, nome, cnh, categoria_cnh, validade_cnh, telefone, status";

    private static final String INSERT = "INSERT INTO motorista (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE motorista SET nome = ?, cnh = ?, categoria_cnh = ?, validade_cnh = ?, telefone = ? "
            + "WHERE id = ? AND status != 3";
    private static final String SELECT_ALL = "SELECT " + COLUNAS + " FROM motorista WHERE status != 3 ORDER BY nome";
    private static final String SELECT_BY_ID = "SELECT " + COLUNAS + " FROM motorista WHERE id = ? AND status != 3";
    private static final String EXISTS_CNH = "SELECT 1 FROM motorista WHERE cnh = ? AND id != ? AND status != 3";

    public MotoristaJdbcRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void inserir(Motorista motorista) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(INSERT)) {
            ps.setObject(1, motorista.getId());
            ps.setString(2, motorista.getNome().getValor());
            ps.setString(3, motorista.getCnh().getValor());
            ps.setString(4, motorista.getCategoriaCnh().name());
            ps.setDate(5, Date.valueOf(motorista.getValidadeCnh()));
            ps.setString(6, motorista.getTelefone());
            ps.setShort(7, motorista.getStatus().getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Erro inserindo motorista", e);
        }
    }

    public boolean atualizar(Motorista motorista) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(UPDATE)) {
            ps.setString(1, motorista.getNome().getValor());
            ps.setString(2, motorista.getCnh().getValor());
            ps.setString(3, motorista.getCategoriaCnh().name());
            ps.setDate(4, Date.valueOf(motorista.getValidadeCnh()));
            ps.setString(5, motorista.getTelefone());
            ps.setObject(6, motorista.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Erro atualizando motorista", e);
        }
    }

    public List<Motorista> listar() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            List<Motorista> motoristas = new ArrayList<>();
            while (rs.next()) {
                motoristas.add(mapRow(rs));
            }
            return motoristas;
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando motoristas", e);
        }
    }

    public Optional<Motorista> buscarPorId(UUID id) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro consultando motorista por id", e);
        }
    }

    public boolean existeCnh(Cnh cnh, UUID ignorarId) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(EXISTS_CNH)) {
            ps.setString(1, cnh.getValor());
            ps.setObject(2, ignorarId != null ? ignorarId : new UUID(0, 0));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erro verificando CNH", e);
        }
    }

    public boolean excluir(UUID id) {
        return excluirLogico("motorista", id);
    }

    private Motorista mapRow(ResultSet rs) throws SQLException {
        return new Motorista(
                rs.getObject("id", UUID.class),
                new Nome(rs.getString("nome")),
                new Cnh(rs.getString("cnh")),
                CategoriaCnh.de(rs.getString("categoria_cnh")),
                rs.getDate("validade_cnh").toLocalDate(),
                rs.getString("telefone"),
                Status.deCodigo(rs.getShort("status")));
    }
}

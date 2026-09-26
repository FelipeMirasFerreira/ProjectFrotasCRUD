package aulas.umc.frota.repository;

import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Base dos repositorios JDBC.
 *
 * O proxy "TransactionAware" faz o getConnection() devolver a MESMA conexao quando existe
 * uma transacao aberta por @Transactional no UseCase. Assim, gravar o abastecimento e
 * atualizar o km do veiculo acontecem juntos (ou nenhum dos dois), sem mudar o estilo
 * try-with-resources do JDBC puro.
 */
public abstract class JdbcRepository {

    protected final DataSource dataSource;

    protected JdbcRepository(DataSource dataSource) {
        this.dataSource = new TransactionAwareDataSourceProxy(dataSource);
    }

    protected boolean excluirLogico(String tabela, UUID id) {
        String sql = "UPDATE " + tabela + " SET status = 3 WHERE id = ? AND status != 3";
        try (var c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Erro excluindo registro de " + tabela, e);
        }
    }
}

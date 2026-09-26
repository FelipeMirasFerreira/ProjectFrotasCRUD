package aulas.umc.frota.model;

import aulas.umc.frota.exception.RegraNegocioException;
import aulas.umc.frota.model.valueObjects.Placa;
import aulas.umc.frota.model.valueObjects.Quilometragem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoTest {

    private static Veiculo veiculo(long kmAtual, int intervalo, long kmUltimaRevisao) {
        return new Veiculo(new Placa("ABC1D23"), "Fiat", "Strada", 2023,
                new Quilometragem(kmAtual), intervalo, new Quilometragem(kmUltimaRevisao));
    }

    @Test
    void calculaProximaRevisaoEKmRestante() {
        Veiculo v = veiculo(15_000, 10_000, 10_000);
        assertEquals(20_000, v.proximaRevisaoKm());
        assertEquals(5_000, v.kmRestanteParaRevisao());
        assertEquals(SituacaoRevisao.EM_DIA, v.situacaoRevisao(1_000));
    }

    @Test
    void situacaoProximaDentroDaAntecedencia() {
        Veiculo v = veiculo(19_000, 10_000, 10_000);
        assertEquals(SituacaoRevisao.PROXIMA, v.situacaoRevisao(1_000));
        assertEquals(SituacaoRevisao.EM_DIA, v.situacaoRevisao(999));
    }

    @Test
    void situacaoVencidaAoAtingirOuPassarDoKm() {
        assertEquals(SituacaoRevisao.VENCIDA, veiculo(20_000, 10_000, 10_000).situacaoRevisao(1_000));
        Veiculo passou = veiculo(21_500, 10_000, 10_000);
        assertEquals(SituacaoRevisao.VENCIDA, passou.situacaoRevisao(1_000));
        assertEquals(-1_500, passou.kmRestanteParaRevisao());
    }

    @Test
    void revisaoReiniciaOCiclo() {
        Veiculo v = veiculo(21_500, 10_000, 10_000);
        v.registrarRevisao(new Quilometragem(21_600), LocalDate.of(2026, 9, 1));
        assertEquals(21_600, v.getQuilometragemAtual().getValor());
        assertEquals(31_600, v.proximaRevisaoKm());
        assertEquals(SituacaoRevisao.EM_DIA, v.situacaoRevisao(1_000));
    }

    @Test
    void revisaoRetroativaMaisAntigaNaoAlteraOCiclo() {
        Veiculo v = veiculo(30_000, 10_000, 25_000);
        v.registrarRevisao(new Quilometragem(20_000), LocalDate.of(2025, 1, 1));
        assertEquals(25_000, v.getKmUltimaRevisao().getValor());
        assertEquals(30_000, v.getQuilometragemAtual().getValor());
    }

    @Test
    void hodometroNaoVoltaNoAbastecimento() {
        Veiculo v = veiculo(15_000, 10_000, 10_000);
        assertThrows(RegraNegocioException.class, () -> v.registrarQuilometragem(new Quilometragem(14_999)));
        v.registrarQuilometragem(new Quilometragem(15_000));
        assertEquals(15_000, v.getQuilometragemAtual().getValor());
    }

    @Test
    void ultimaRevisaoNaoPodeSerMaiorQueKmAtual() {
        assertThrows(IllegalArgumentException.class, () -> veiculo(5_000, 10_000, 6_000));
    }

    @Test
    void placaAceitaFormatosAntigoEMercosul() {
        assertEquals("ABC-1234", new Placa("abc-1234").formatada());
        assertEquals("ABC1D23", new Placa("abc 1d23").formatada());
        assertThrows(IllegalArgumentException.class, () -> new Placa("AB12345"));
    }
}

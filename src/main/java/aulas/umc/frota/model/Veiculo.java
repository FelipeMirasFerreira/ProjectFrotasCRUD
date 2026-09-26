package aulas.umc.frota.model;

import aulas.umc.frota.exception.RegraNegocioException;
import aulas.umc.frota.model.valueObjects.Placa;
import aulas.umc.frota.model.valueObjects.Quilometragem;

import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

public class Veiculo extends Domain {
    public static final int INTERVALO_REVISAO_PADRAO_KM = 10_000;

    private Placa placa;
    private String marca;
    private String modelo;
    private int ano;
    private Quilometragem quilometragemAtual;
    private int intervaloRevisaoKm;
    private Quilometragem kmUltimaRevisao;
    private LocalDate dataUltimaRevisao;

    /** Veiculo novo na frota. kmUltimaRevisao = hodometro na ultima revisao feita antes do cadastro. */
    public Veiculo(Placa placa, String marca, String modelo, int ano, Quilometragem quilometragemAtual,
                   int intervaloRevisaoKm, Quilometragem kmUltimaRevisao) {
        this(null, placa, marca, modelo, ano, quilometragemAtual, intervaloRevisaoKm, kmUltimaRevisao, null, Status.ATIVO);
    }

    /** Reconstrucao a partir do banco. */
    public Veiculo(UUID id, Placa placa, String marca, String modelo, int ano, Quilometragem quilometragemAtual,
                   int intervaloRevisaoKm, Quilometragem kmUltimaRevisao, LocalDate dataUltimaRevisao, Status status) {
        super(id, status);
        alterarDados(placa, marca, modelo, ano, intervaloRevisaoKm);
        if (quilometragemAtual == null) {
            throw new IllegalArgumentException("Quilometragem atual obrigatoria.");
        }
        this.quilometragemAtual = quilometragemAtual;
        this.kmUltimaRevisao = kmUltimaRevisao != null ? kmUltimaRevisao : new Quilometragem(0);
        validarUltimaRevisao(this.kmUltimaRevisao);
        this.dataUltimaRevisao = dataUltimaRevisao;
    }

    public void alterarDados(Placa placa, String marca, String modelo, int ano, int intervaloRevisaoKm) {
        if (placa == null) {
            throw new IllegalArgumentException("Placa obrigatoria.");
        }
        this.placa = placa;
        this.marca = textoObrigatorio(marca, "Marca", 60);
        this.modelo = textoObrigatorio(modelo, "Modelo", 80);

        int anoMaximo = Year.now().getValue() + 1;
        if (ano < 1950 || ano > anoMaximo) {
            throw new IllegalArgumentException("Ano deve estar entre 1950 e " + anoMaximo + ".");
        }
        this.ano = ano;

        if (intervaloRevisaoKm < 1_000 || intervaloRevisaoKm > 100_000) {
            throw new IllegalArgumentException("Intervalo de revisao deve estar entre 1.000 e 100.000 km.");
        }
        this.intervaloRevisaoKm = intervaloRevisaoKm;
    }

    /** O hodometro so anda para frente: leituras menores que a atual indicam erro de digitacao. */
    public void registrarQuilometragem(Quilometragem novaLeitura) {
        if (novaLeitura.menorQue(quilometragemAtual)) {
            throw new RegraNegocioException("Quilometragem informada (" + novaLeitura.getValor()
                    + " km) e menor que a atual do veiculo " + placa.formatada()
                    + " (" + quilometragemAtual.getValor() + " km).");
        }
        this.quilometragemAtual = novaLeitura;
    }

    /** Corrige manualmente o km da ultima revisao (ex.: revisao feita fora do sistema). */
    public void corrigirUltimaRevisao(Quilometragem km) {
        validarUltimaRevisao(km);
        this.kmUltimaRevisao = km;
    }

    /**
     * Leitura vinda de um lancamento que pode ser retroativo (ex.: manutencao lancada dias depois):
     * so avanca o hodometro, nunca reduz.
     */
    public void atualizarQuilometragemSeMaior(Quilometragem leitura) {
        if (leitura.maiorQue(quilometragemAtual)) {
            this.quilometragemAtual = leitura;
        }
    }

    /**
     * Chamado quando uma manutencao do tipo REVISAO e registrada: reinicia o ciclo do alerta.
     * Uma revisao mais antiga que a ultima conhecida fica so no historico e nao altera o ciclo.
     */
    public void registrarRevisao(Quilometragem km, LocalDate data) {
        atualizarQuilometragemSeMaior(km);
        if (!km.menorQue(kmUltimaRevisao)) {
            this.kmUltimaRevisao = km;
            this.dataUltimaRevisao = data;
        }
    }

    public long proximaRevisaoKm() {
        return kmUltimaRevisao.getValor() + intervaloRevisaoKm;
    }

    /** Negativo quando a revisao ja passou do prazo. */
    public long kmRestanteParaRevisao() {
        return proximaRevisaoKm() - quilometragemAtual.getValor();
    }

    public SituacaoRevisao situacaoRevisao(long antecedenciaAlertaKm) {
        long restante = kmRestanteParaRevisao();
        if (restante <= 0) {
            return SituacaoRevisao.VENCIDA;
        }
        if (restante <= antecedenciaAlertaKm) {
            return SituacaoRevisao.PROXIMA;
        }
        return SituacaoRevisao.EM_DIA;
    }

    public boolean estaAtivo() {
        return status == Status.ATIVO;
    }

    private void validarUltimaRevisao(Quilometragem km) {
        if (km.maiorQue(quilometragemAtual)) {
            throw new IllegalArgumentException("Km da ultima revisao nao pode ser maior que a quilometragem atual.");
        }
    }

    private static String textoObrigatorio(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " obrigatorio.");
        }
        String limpo = valor.trim();
        if (limpo.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(campo + " deve ter no maximo " + tamanhoMaximo + " caracteres.");
        }
        return limpo;
    }

    public Placa getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getAno() {
        return ano;
    }

    public Quilometragem getQuilometragemAtual() {
        return quilometragemAtual;
    }

    public int getIntervaloRevisaoKm() {
        return intervaloRevisaoKm;
    }

    public Quilometragem getKmUltimaRevisao() {
        return kmUltimaRevisao;
    }

    public LocalDate getDataUltimaRevisao() {
        return dataUltimaRevisao;
    }
}

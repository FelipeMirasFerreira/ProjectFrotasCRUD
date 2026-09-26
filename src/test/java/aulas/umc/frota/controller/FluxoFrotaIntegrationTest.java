package aulas.umc.frota.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Percorre o fluxo completo pela API, com banco H2 em memoria. */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoFrotaIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private ResultActions post(String url, String body) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String id(ResultActions resultado) throws Exception {
        JsonNode node = json.readTree(resultado.andReturn().getResponse().getContentAsString());
        return node.get("id").asText();
    }

    @Test
    void fluxoCompletoComAlertaDeRevisaoEConsumo() throws Exception {
        String hoje = LocalDate.now().toString();

        // veiculo com revisao a cada 10.000 km, ultima aos 10.000 e hodometro em 18.500
        String veiculoId = id(post("/api/veiculos", """
                {"placa":"fro-1a23","marca":"Fiat","modelo":"Strada","ano":2023,
                 "quilometragemAtual":18500,"intervaloRevisaoKm":10000,"kmUltimaRevisao":10000}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("FRO1A23"))
                .andExpect(jsonPath("$.situacaoRevisao").value("EM_DIA")));

        String motoristaId = id(post("/api/motoristas", """
                {"nome":"Joana Silva","cnh":"123.456.789-01","categoriaCnh":"b","validadeCnh":"%s"}"""
                .formatted(LocalDate.now().plusYears(2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnh").value("12345678901"))
                .andExpect(jsonPath("$.cnhVencida").value(false)));

        // 1o abastecimento: 19.200 km -> faltam 800 km -> PROXIMA
        post("/api/abastecimentos", """
                {"veiculoId":"%s","motoristaId":"%s","data":"%s","quilometragem":19200,
                 "litros":40,"valorTotal":240.00,"combustivel":"gasolina"}""".formatted(veiculoId, motoristaId, hoje))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precoPorLitro").value(6.0))
                .andExpect(jsonPath("$.consumoKmPorLitro").doesNotExist());

        mvc.perform(get("/api/alertas/revisao"))
                .andExpect(jsonPath("$[?(@.placa == 'FRO1A23')].situacao").value(contains("PROXIMA")))
                .andExpect(jsonPath("$[?(@.placa == 'FRO1A23')].kmRestante").value(contains(800)));

        // 2o abastecimento: +500 km com 40 L -> 12,5 km/L; passa de 20.000 -> VENCIDA
        post("/api/abastecimentos", """
                {"veiculoId":"%s","motoristaId":"%s","data":"%s","quilometragem":19700,
                 "litros":40,"valorTotal":240.00,"combustivel":"GASOLINA"}""".formatted(veiculoId, motoristaId, hoje))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumoKmPorLitro").value(12.5));

        post("/api/abastecimentos", """
                {"veiculoId":"%s","motoristaId":"%s","quilometragem":20100,
                 "litros":30,"valorTotal":180.00,"combustivel":"GASOLINA"}""".formatted(veiculoId, motoristaId))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/veiculos/" + veiculoId))
                .andExpect(jsonPath("$.quilometragemAtual").value(20100))
                .andExpect(jsonPath("$.situacaoRevisao").value("VENCIDA"))
                .andExpect(jsonPath("$.kmRestanteRevisao").value(-100));

        // hodometro nao pode voltar: 422 com mensagem
        post("/api/abastecimentos", """
                {"veiculoId":"%s","motoristaId":"%s","quilometragem":20000,
                 "litros":30,"valorTotal":180.00,"combustivel":"GASOLINA"}""".formatted(veiculoId, motoristaId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("menor que a atual")));

        // revisao reinicia o ciclo: proxima aos 30.150
        post("/api/manutencoes", """
                {"veiculoId":"%s","quilometragem":20150,"tipo":"REVISAO","descricao":"Revisao 20 mil",
                 "custo":650.00,"oficina":"Oficina Centro"}""".formatted(veiculoId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("REVISAO"));

        mvc.perform(get("/api/veiculos/" + veiculoId))
                .andExpect(jsonPath("$.quilometragemAtual").value(20150))
                .andExpect(jsonPath("$.kmUltimaRevisao").value(20150))
                .andExpect(jsonPath("$.proximaRevisaoKm").value(30150))
                .andExpect(jsonPath("$.situacaoRevisao").value("EM_DIA"));

        mvc.perform(get("/api/alertas/revisao"))
                .andExpect(jsonPath("$[?(@.placa == 'FRO1A23')]").isEmpty());

        mvc.perform(get("/api/abastecimentos").param("veiculoId", veiculoId))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void recusaAbastecimentoComCnhVencida() throws Exception {
        String veiculoId = id(post("/api/veiculos", """
                {"placa":"CNH1234","marca":"VW","modelo":"Gol","ano":2020,"quilometragemAtual":1000}"""));
        String motoristaId = id(post("/api/motoristas", """
                {"nome":"Carlos Souza","cnh":"98765432100","categoriaCnh":"B","validadeCnh":"%s"}"""
                .formatted(LocalDate.now().minusDays(1)))
                .andExpect(jsonPath("$.cnhVencida").value(true)));

        post("/api/abastecimentos", """
                {"veiculoId":"%s","motoristaId":"%s","quilometragem":1100,
                 "litros":30,"valorTotal":180.00,"combustivel":"ETANOL"}""".formatted(veiculoId, motoristaId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("vencida")));

        // a transacao nao gravou nada: km continua 1000
        mvc.perform(get("/api/veiculos/" + veiculoId)).andExpect(jsonPath("$.quilometragemAtual").value(1000));
    }

    @Test
    void validacoesRetornamMensagem() throws Exception {
        post("/api/veiculos", """
                {"placa":"XYZ","marca":"Fiat","modelo":"Uno","ano":2010,"quilometragemAtual":0}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("Placa invalida")));

        post("/api/veiculos", """
                {"placa":"DUP1234","marca":"Fiat","modelo":"Uno","ano":2010,"quilometragemAtual":0}""")
                .andExpect(status().isCreated());
        post("/api/veiculos", """
                {"placa":"DUP-1234","marca":"Fiat","modelo":"Uno","ano":2010,"quilometragemAtual":0}""")
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/api/veiculos/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void exclusaoLogicaRemoveDaListagem() throws Exception {
        String veiculoId = id(post("/api/veiculos", """
                {"placa":"DEL1234","marca":"Ford","modelo":"Ka","ano":2019,"quilometragemAtual":500}"""));

        mvc.perform(delete("/api/veiculos/" + veiculoId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/veiculos/" + veiculoId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/veiculos")).andExpect(jsonPath("$[?(@.placa == 'DEL-1234')]").isEmpty());
    }
}

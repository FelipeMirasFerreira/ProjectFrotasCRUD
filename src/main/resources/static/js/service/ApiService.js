/**
 * Cliente HTTP de um recurso REST (ex.: /api/veiculos).
 * Em caso de erro, lanca Error com a mensagem enviada pelo backend (ErroResponse.mensagem).
 */
export class ApiService {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    listar(filtros = {}) {
        const params = new URLSearchParams();
        for (const [chave, valor] of Object.entries(filtros)) {
            if (valor !== undefined && valor !== null && valor !== '') {
                params.append(chave, valor);
            }
        }
        const query = params.toString();
        return this.enviar(query ? '?' + query : '', 'GET');
    }

    buscarPorId(id) {
        return this.enviar('/' + id, 'GET');
    }

    cadastrar(body) {
        return this.enviar('', 'POST', body);
    }

    atualizar(id, body) {
        return this.enviar('/' + id, 'PUT', body);
    }

    excluir(id) {
        return this.enviar('/' + id, 'DELETE');
    }

    async enviar(caminho, metodo, body = null) {
        const opcoes = { method: metodo, headers: {} };

        if (body) {
            opcoes.headers['Content-Type'] = 'application/json';
            opcoes.body = JSON.stringify(body);
        }

        const response = await fetch(this.baseUrl + caminho, opcoes);

        if (!response.ok) {
            let mensagem = 'Erro HTTP ' + response.status;
            try {
                const erro = await response.json();
                if (erro.mensagem) {
                    mensagem = erro.mensagem;
                }
            } catch {
                // resposta sem corpo JSON: mantem a mensagem generica
            }
            throw new Error(mensagem);
        }

        return response.status === 204 ? null : response.json();
    }
}

export const veiculoService = new ApiService('/api/veiculos');
export const motoristaService = new ApiService('/api/motoristas');
export const abastecimentoService = new ApiService('/api/abastecimentos');
export const manutencaoService = new ApiService('/api/manutencoes');
export const alertaService = new ApiService('/api/alertas/revisao');

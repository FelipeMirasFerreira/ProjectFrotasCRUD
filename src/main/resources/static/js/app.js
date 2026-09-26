import {
    abastecimentoService,
    alertaService,
    manutencaoService,
    motoristaService,
    veiculoService
} from './service/ApiService.js';

// ---------- utilitarios ----------

const $ = (id) => document.getElementById(id);
const valor = (id) => $(id).value.trim();
const numero = (id) => (valor(id) === '' ? null : Number(valor(id)));

const fmtKm = (km) => Number(km).toLocaleString('pt-BR') + ' km';
const fmtReais = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const fmtDecimal = (v, casas) => Number(v).toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas });
const fmtData = (iso) => (iso ? iso.split('-').reverse().join('/') : '—');
const hojeIso = () => new Date().toLocaleDateString('sv-SE'); // aaaa-mm-dd no fuso local

const ROTULO_SITUACAO = { VENCIDA: 'Vencida', PROXIMA: 'Próxima', EM_DIA: 'Em dia' };
const ROTULO_TIPO = { REVISAO: 'Revisão', PREVENTIVA: 'Preventiva', CORRETIVA: 'Corretiva' };

/** Cria elemento com texto seguro (textContent), evitando injecao de HTML. */
function el(tag, props = {}, ...filhos) {
    const elemento = document.createElement(tag);
    Object.assign(elemento, props);
    for (const filho of filhos) {
        if (filho !== null && filho !== undefined) {
            elemento.append(filho instanceof Node ? filho : String(filho));
        }
    }
    return elemento;
}

const etiqueta = (situacao) => el('span', { className: 'etiqueta ' + situacao }, ROTULO_SITUACAO[situacao]);

function botao(texto, aoClicar, perigo = false) {
    const b = el('button', { type: 'button', className: 'link' + (perigo ? ' perigo' : '') }, texto);
    b.addEventListener('click', aoClicar);
    return b;
}

function preencherTabela(tbody, linhas, colunas, textoVazio) {
    tbody.replaceChildren();
    if (linhas.length === 0) {
        tbody.append(el('tr', {}, el('td', { colSpan: colunas, className: 'vazio' }, textoVazio)));
        return;
    }
    tbody.append(...linhas);
}

let timerMensagem;
function mostrarMensagem(texto, tipo = 'sucesso') {
    const caixa = $('mensagem');
    caixa.textContent = texto;
    caixa.className = 'mensagem ' + tipo;
    caixa.hidden = false;
    clearTimeout(timerMensagem);
    timerMensagem = setTimeout(() => { caixa.hidden = true; }, tipo === 'erro' ? 8000 : 4000);
}

/** Executa uma acao assincrona mostrando o erro do backend, se houver. */
async function executar(acao, mensagemSucesso) {
    try {
        await acao();
        if (mensagemSucesso) {
            mostrarMensagem(mensagemSucesso);
        }
        return true;
    } catch (erro) {
        mostrarMensagem(erro.message, 'erro');
        return false;
    }
}

// ---------- abas ----------

const carregarAba = {
    painel: carregarPainel,
    veiculos: carregarVeiculos,
    motoristas: carregarMotoristas,
    abastecimentos: carregarAbastecimentos,
    manutencoes: carregarManutencoes
};

document.querySelectorAll('.aba').forEach((aba) => {
    aba.addEventListener('click', () => abrirAba(aba.dataset.aba));
});

function abrirAba(nome) {
    document.querySelectorAll('.aba').forEach((a) => a.classList.toggle('ativa', a.dataset.aba === nome));
    document.querySelectorAll('.painel-aba').forEach((p) => { p.hidden = p.id !== nome; });
    executar(carregarAba[nome]);
}

// ---------- painel ----------

$('alertasTodos').addEventListener('change', () => executar(carregarPainel));

async function carregarPainel() {
    const [alertas, todos, motoristas] = await Promise.all([
        alertaService.listar({ todos: $('alertasTodos').checked }),
        alertaService.listar({ todos: true }),
        motoristaService.listar()
    ]);

    $('resumoVeiculos').textContent = todos.length;
    $('resumoVencidas').textContent = todos.filter((a) => a.situacao === 'VENCIDA').length;
    $('resumoProximas').textContent = todos.filter((a) => a.situacao === 'PROXIMA').length;
    $('resumoCnh').textContent = motoristas.filter((m) => m.cnhVencida).length;

    const lista = $('listaAlertas');
    lista.replaceChildren();
    if (alertas.length === 0) {
        lista.append(el('li', { className: 'vazio' }, 'Nenhuma revisão vencida ou próxima. Frota em dia!'));
        return;
    }
    for (const a of alertas) {
        lista.append(el('li', { className: 'alerta ' + a.situacao },
            el('div', {}, el('strong', {}, a.placa), ' · ', a.veiculo),
            etiqueta(a.situacao),
            el('p', {}, a.mensagem + ' Hodômetro: ' + fmtKm(a.quilometragemAtual) + '.')));
    }
}

// ---------- veiculos ----------

const formVeiculo = $('formVeiculo');

formVeiculo.addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = valor('veiculoId');
    const request = {
        placa: valor('veiculoPlaca'),
        marca: valor('veiculoMarca'),
        modelo: valor('veiculoModelo'),
        ano: numero('veiculoAno'),
        quilometragemAtual: numero('veiculoKm'),
        intervaloRevisaoKm: numero('veiculoIntervalo'),
        kmUltimaRevisao: numero('veiculoKmRevisao')
    };
    const ok = await executar(
        () => (id ? veiculoService.atualizar(id, request) : veiculoService.cadastrar(request)),
        id ? 'Veículo atualizado.' : 'Veículo cadastrado.');
    if (ok) {
        limparFormVeiculo();
        await executar(carregarVeiculos);
    }
});

$('cancelarVeiculo').addEventListener('click', limparFormVeiculo);

function limparFormVeiculo() {
    formVeiculo.reset();
    $('veiculoId').value = '';
    $('tituloFormVeiculo').textContent = 'Cadastrar veículo';
    $('cancelarVeiculo').hidden = true;
}

function editarVeiculo(v) {
    $('veiculoId').value = v.id;
    $('veiculoPlaca').value = v.placa;
    $('veiculoMarca').value = v.marca;
    $('veiculoModelo').value = v.modelo;
    $('veiculoAno').value = v.ano;
    $('veiculoKm').value = v.quilometragemAtual;
    $('veiculoIntervalo').value = v.intervaloRevisaoKm;
    $('veiculoKmRevisao').value = v.kmUltimaRevisao;
    $('tituloFormVeiculo').textContent = 'Editar veículo ' + v.placa;
    $('cancelarVeiculo').hidden = false;
    formVeiculo.scrollIntoView({ behavior: 'smooth' });
}

async function excluirVeiculo(v) {
    if (!confirm('Excluir o veículo ' + v.placa + '? O histórico de abastecimentos e manutenções é mantido.')) {
        return;
    }
    if (await executar(() => veiculoService.excluir(v.id), 'Veículo excluído.')) {
        await executar(carregarVeiculos);
    }
}

async function carregarVeiculos() {
    const veiculos = await veiculoService.listar();
    const linhas = veiculos.map((v) => el('tr', {},
        el('td', {}, el('strong', {}, v.placa)),
        el('td', {}, `${v.marca} ${v.modelo} (${v.ano})`),
        el('td', { className: 'num' }, fmtKm(v.quilometragemAtual)),
        el('td', { className: 'num', title: 'Faltam ' + fmtKm(v.kmRestanteRevisao) }, fmtKm(v.proximaRevisaoKm)),
        el('td', {}, etiqueta(v.situacaoRevisao)),
        el('td', { className: 'botoes' },
            botao('Editar', () => editarVeiculo(v)),
            botao('Excluir', () => excluirVeiculo(v), true))));
    preencherTabela($('tabelaVeiculos'), linhas, 6, 'Nenhum veículo cadastrado.');
}

// ---------- motoristas ----------

const formMotorista = $('formMotorista');

formMotorista.addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = valor('motoristaId');
    const request = {
        nome: valor('motoristaNome'),
        cnh: valor('motoristaCnh'),
        categoriaCnh: valor('motoristaCategoria'),
        validadeCnh: valor('motoristaValidade'),
        telefone: valor('motoristaTelefone')
    };
    const ok = await executar(
        () => (id ? motoristaService.atualizar(id, request) : motoristaService.cadastrar(request)),
        id ? 'Motorista atualizado.' : 'Motorista cadastrado.');
    if (ok) {
        limparFormMotorista();
        await executar(carregarMotoristas);
    }
});

$('cancelarMotorista').addEventListener('click', limparFormMotorista);

function limparFormMotorista() {
    formMotorista.reset();
    $('motoristaId').value = '';
    $('tituloFormMotorista').textContent = 'Cadastrar motorista';
    $('cancelarMotorista').hidden = true;
}

function editarMotorista(m) {
    $('motoristaId').value = m.id;
    $('motoristaNome').value = m.nome;
    $('motoristaCnh').value = m.cnh;
    $('motoristaCategoria').value = m.categoriaCnh;
    $('motoristaValidade').value = m.validadeCnh;
    $('motoristaTelefone').value = m.telefone ?? '';
    $('tituloFormMotorista').textContent = 'Editar motorista';
    $('cancelarMotorista').hidden = false;
    formMotorista.scrollIntoView({ behavior: 'smooth' });
}

async function excluirMotorista(m) {
    if (!confirm('Excluir o motorista ' + m.nome + '?')) {
        return;
    }
    if (await executar(() => motoristaService.excluir(m.id), 'Motorista excluído.')) {
        await executar(carregarMotoristas);
    }
}

async function carregarMotoristas() {
    const motoristas = await motoristaService.listar();
    const linhas = motoristas.map((m) => el('tr', {},
        el('td', {}, m.nome),
        el('td', {}, m.cnh),
        el('td', {}, m.categoriaCnh),
        el('td', {}, fmtData(m.validadeCnh), ' ',
            m.cnhVencida ? el('span', { className: 'etiqueta VENCIDA' }, 'Vencida') : null),
        el('td', {}, m.telefone ?? '—'),
        el('td', { className: 'botoes' },
            botao('Editar', () => editarMotorista(m)),
            botao('Excluir', () => excluirMotorista(m), true))));
    preencherTabela($('tabelaMotoristas'), linhas, 6, 'Nenhum motorista cadastrado.');
}

// ---------- selects de veiculo/motorista ----------

async function carregarSelects() {
    const [veiculos, motoristas] = await Promise.all([veiculoService.listar(), motoristaService.listar()]);

    document.querySelectorAll('.selectVeiculo').forEach((select) => {
        const selecionado = select.value;
        const primeira = select.dataset.todos
            ? el('option', { value: '' }, 'Todos os veículos')
            : el('option', { value: '', disabled: true }, veiculos.length ? 'Selecione...' : 'Cadastre um veículo');
        select.replaceChildren(primeira, ...veiculos.map((v) =>
            el('option', { value: v.id }, `${v.placa} — ${v.modelo} (${fmtKm(v.quilometragemAtual)})`)));
        select.value = veiculos.some((v) => v.id === selecionado) ? selecionado : '';
    });

    const selectMotorista = $('abastMotorista');
    const selecionado = selectMotorista.value;
    selectMotorista.replaceChildren(
        el('option', { value: '', disabled: true }, motoristas.length ? 'Selecione...' : 'Cadastre um motorista'),
        ...motoristas.map((m) => el('option', { value: m.id, disabled: m.cnhVencida },
            m.nome + (m.cnhVencida ? ' (CNH vencida)' : ''))));
    selectMotorista.value = motoristas.some((m) => m.id === selecionado) ? selecionado : '';

    return veiculos;
}

/** Ao escolher o veiculo, sugere o km atual como ponto de partida. */
function sugerirKm(selectId, inputId, veiculos) {
    const v = veiculos.find((x) => x.id === $(selectId).value);
    if (v && !$(inputId).value) {
        $(inputId).value = v.quilometragemAtual;
    }
}

let veiculosCache = [];
$('abastVeiculo').addEventListener('change', () => sugerirKm('abastVeiculo', 'abastKm', veiculosCache));
$('manutVeiculo').addEventListener('change', () => sugerirKm('manutVeiculo', 'manutKm', veiculosCache));

// ---------- abastecimentos ----------

$('formAbastecimento').addEventListener('submit', async (event) => {
    event.preventDefault();
    const request = {
        veiculoId: valor('abastVeiculo'),
        motoristaId: valor('abastMotorista'),
        data: valor('abastData'),
        quilometragem: numero('abastKm'),
        litros: numero('abastLitros'),
        valorTotal: numero('abastValor'),
        combustivel: valor('abastCombustivel')
    };
    const ok = await executar(() => abastecimentoService.cadastrar(request), 'Abastecimento registrado.');
    if (ok) {
        ['abastKm', 'abastLitros', 'abastValor'].forEach((id) => { $(id).value = ''; });
        await executar(carregarAbastecimentos);
    }
});

$('filtroAbastVeiculo').addEventListener('change', () => executar(carregarHistoricoAbastecimentos));

async function carregarAbastecimentos() {
    veiculosCache = await carregarSelects();
    if (!$('abastData').value) {
        $('abastData').value = hojeIso();
    }
    $('abastData').max = hojeIso();
    await carregarHistoricoAbastecimentos();
}

async function carregarHistoricoAbastecimentos() {
    const lista = await abastecimentoService.listar({ veiculoId: $('filtroAbastVeiculo').value });
    const linhas = lista.map((a) => el('tr', {},
        el('td', {}, fmtData(a.data)),
        el('td', {}, el('strong', {}, a.placa)),
        el('td', {}, a.motoristaNome),
        el('td', { className: 'num' }, fmtKm(a.quilometragem)),
        el('td', { className: 'num' }, fmtDecimal(a.litros, 2)),
        el('td', { className: 'num' }, fmtReais(a.valorTotal)),
        el('td', { className: 'num' }, fmtDecimal(a.precoPorLitro, 3)),
        el('td', { className: 'num' }, a.consumoKmPorLitro != null ? fmtDecimal(a.consumoKmPorLitro, 2) : '—'),
        el('td', { className: 'botoes' }, botao('Excluir', async () => {
            if (confirm('Excluir este abastecimento? O km do veículo não é alterado.')
                && await executar(() => abastecimentoService.excluir(a.id), 'Abastecimento excluído.')) {
                await executar(carregarHistoricoAbastecimentos);
            }
        }, true))));
    preencherTabela($('tabelaAbastecimentos'), linhas, 9, 'Nenhum abastecimento registrado.');
}

// ---------- manutencoes ----------

$('formManutencao').addEventListener('submit', async (event) => {
    event.preventDefault();
    const request = {
        veiculoId: valor('manutVeiculo'),
        data: valor('manutData'),
        quilometragem: numero('manutKm'),
        tipo: valor('manutTipo'),
        descricao: valor('manutDescricao'),
        custo: numero('manutCusto'),
        oficina: valor('manutOficina')
    };
    const ok = await executar(() => manutencaoService.cadastrar(request), 'Manutenção registrada.');
    if (ok) {
        ['manutKm', 'manutCusto', 'manutOficina', 'manutDescricao'].forEach((id) => { $(id).value = ''; });
        await executar(carregarManutencoes);
    }
});

$('filtroManutVeiculo').addEventListener('change', () => executar(carregarHistoricoManutencoes));

async function carregarManutencoes() {
    veiculosCache = await carregarSelects();
    if (!$('manutData').value) {
        $('manutData').value = hojeIso();
    }
    $('manutData').max = hojeIso();
    await carregarHistoricoManutencoes();
}

async function carregarHistoricoManutencoes() {
    const lista = await manutencaoService.listar({ veiculoId: $('filtroManutVeiculo').value });
    const linhas = lista.map((m) => el('tr', {},
        el('td', {}, fmtData(m.data)),
        el('td', {}, el('strong', {}, m.placa)),
        el('td', {}, ROTULO_TIPO[m.tipo]),
        el('td', { className: 'quebra' }, m.descricao),
        el('td', { className: 'num' }, fmtKm(m.quilometragem)),
        el('td', { className: 'num' }, fmtReais(m.custo)),
        el('td', {}, m.oficina ?? '—'),
        el('td', { className: 'botoes' }, botao('Excluir', async () => {
            if (confirm('Excluir esta manutenção do histórico?')
                && await executar(() => manutencaoService.excluir(m.id), 'Manutenção excluída.')) {
                await executar(carregarHistoricoManutencoes);
            }
        }, true))));
    preencherTabela($('tabelaManutencoes'), linhas, 8, 'Nenhuma manutenção registrada.');
}

// ---------- inicio ----------

$('veiculoAno').max = new Date().getFullYear() + 1;
abrirAba('painel');

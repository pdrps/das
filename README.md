# Sistema de Chamados - API REST

API REST em **Java 21** com **Spring Boot**, **Spring Data JPA** e **H2**, organizada em camadas (Controller, Service, Repository, Entity e DTO).

## Índice

- [Como executar](#como-executar)
- [Rotas](#rotas)
- [Popular o banco pelo Postman](#popular-o-banco-pelo-postman)
- [Testar as rotas PATCH de chamados](#testar-as-rotas-patch-de-chamados)
- [Filtros, paginação e ordenação](#filtros-paginação-e-ordenação)
- [Resumo dos resultados esperados](#resumo-dos-resultados-esperados)

---

## Como executar

Na pasta raiz do projeto (onde ficam o `pom.xml` e o `mvnw`), execute no terminal:

```bash
# Linux / macOS
./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

---

## Rotas

| Recurso    | Rota          | Métodos                                                 |
|------------|---------------|---------------------------------------------------------|
| Clientes   | `/clientes`   | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Técnicos   | `/tecnicos`   | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Categorias | `/categorias` | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Chamados   | `/chamados`   | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |

Rotas de ação e de consulta:

| Método  | Rota                      | Ação                                         |
|---------|---------------------------|----------------------------------------------|
| `PATCH` | `/chamados/{id}/atribuir` | Atribui um técnico ao chamado                |
| `PATCH` | `/chamados/{id}/status`   | Altera o status do chamado                   |
| `GET`   | `/clientes/{id}/chamados` | Lista os chamados de um cliente (paginado)   |

---

## Popular o banco pelo Postman

Com a aplicação rodando, envie as requisições abaixo **nesta ordem**, pois o chamado depende das outras três tabelas.

Em todas: método **POST**, aba **Body** → **raw** → **JSON**. Envie **um corpo por requisição** (cada linha dos blocos abaixo é uma requisição).

> [!NOTE]
> O banco H2 é em memória. Os dados são perdidos a cada reinicialização, então repita o processo sempre que reiniciar a aplicação. Os ids usados nos chamados (`clienteId`, `categoriaId`, `tecnicoId`) assumem que as tabelas estavam vazias ao começar.

### 1. Clientes (ids 1 a 5)

`POST http://localhost:8080/clientes`

```json
{ "nome": "Maria Teste", "email": "maria@teste.com", "telefone": "47911111111" }
{ "nome": "João Teste", "email": "joao@teste.com", "telefone": "47922222222" }
{ "nome": "Ana Costa", "email": "ana.costa@teste.com", "telefone": "47933333333" }
{ "nome": "Bruno Alves", "email": "bruno@teste.com", "telefone": "47944444444" }
{ "nome": "Carla Dias", "email": "carla@teste.com", "telefone": "47955555555" }
```

### 2. Categorias (ids 1 a 4)

`POST http://localhost:8080/categorias`

```json
{ "nome": "Hardware", "descricao": "Problemas com equipamentos físicos" }
{ "nome": "Software", "descricao": "Erros e dúvidas de programas e sistemas" }
{ "nome": "Rede", "descricao": "Falhas de conexão e acesso à internet" }
{ "nome": "Acesso", "descricao": "Senhas, permissões e contas de usuário" }
```

### 3. Técnicos (ids 1 a 3)

`POST http://localhost:8080/tecnicos`

```json
{ "nome": "Carlos Lima", "email": "carlos@teste.com", "perfil": "TECNICO" }
{ "nome": "Paula Admin", "email": "paula@teste.com", "perfil": "ADMIN" }
{ "nome": "Rafael Souza", "email": "rafael@teste.com", "perfil": "TECNICO" }
```

### 4. Chamados (ids 1 a 8)

`POST http://localhost:8080/chamados`

```json
{ "titulo": "Notebook não liga", "descricao": "Sem sinal de energia", "prioridade": "ALTA", "clienteId": 1, "categoriaId": 1 }
{ "titulo": "Sistema travando", "descricao": "O programa fecha sozinho ao abrir relatórios", "prioridade": "MEDIA", "clienteId": 2, "categoriaId": 2, "tecnicoId": 1 }
{ "titulo": "Sem acesso à internet", "descricao": "Setor inteiro sem conexão", "prioridade": "CRITICA", "clienteId": 3, "categoriaId": 3, "tecnicoId": 3 }
{ "titulo": "Senha expirada", "descricao": "Não consigo redefinir minha senha", "prioridade": "BAIXA", "clienteId": 4, "categoriaId": 4 }
{ "titulo": "Impressora não imprime", "descricao": "Fila de impressão travada", "prioridade": "MEDIA", "clienteId": 1, "categoriaId": 1, "tecnicoId": 1 }
{ "titulo": "Erro ao gerar relatório", "descricao": "Mensagem de erro ao exportar para PDF", "prioridade": "ALTA", "clienteId": 2, "categoriaId": 2 }
{ "titulo": "Wi-Fi instável", "descricao": "A conexão cai várias vezes ao dia", "prioridade": "ALTA", "clienteId": 5, "categoriaId": 3, "tecnicoId": 3 }
{ "titulo": "Solicitar acesso ao sistema", "descricao": "Novo colaborador precisa de login", "prioridade": "BAIXA", "clienteId": 3, "categoriaId": 4 }
```

**Resultado esperado em todas as requisições:** `201 Created`.
Nos chamados, o `status` volta como `ABERTO` e a `dataAbertura` vem preenchida automaticamente.

---

## Testar as rotas PATCH de chamados

Pré-requisito: banco populado conforme a seção anterior, com os chamados 1 e 2 em status `ABERTO`.

Em todas: método **PATCH**, aba **Body** → **raw** → **JSON**. Execute **nesta ordem**.

### 1. Atribuir técnico

`PATCH http://localhost:8080/chamados/1/atribuir`

```json
{
  "tecnicoId": 1
}
```

**Esperado:** `200 OK`, com `tecnicoId` e `tecnicoNome` preenchidos.

### 2. Alterar status: transições válidas (chamado 1)

`PATCH http://localhost:8080/chamados/1/status`

Envie um corpo por vez:

```json
{ "status": "EM_ATENDIMENTO" }
```

```json
{ "status": "AGUARDANDO_CLIENTE" }
```

```json
{ "status": "EM_ATENDIMENTO" }
```

**Esperado nas três:** `200 OK`, com o novo `status` na resposta.

### 3. Fechar o chamado (chamado 1)

`PATCH http://localhost:8080/chamados/1/status`

```json
{
  "status": "FECHADO"
}
```

**Esperado:** `200 OK`, com `status` `FECHADO` e `dataFechamento` preenchida.

### 4. Alterar chamado fechado (chamado 1)

Repita a requisição do passo 3, ou tente atribuir um técnico de novo:

`PATCH http://localhost:8080/chamados/1/atribuir`

```json
{
  "tecnicoId": 1
}
```

**Esperado:** `409 Conflict`, com a mensagem "Chamado fechado não pode ser alterado".

> [!IMPORTANT]
> O passo 4 só funciona depois do passo 3, porque depende do chamado 1 estar fechado.

---

## Filtros, paginação e ordenação

Em todas: método **GET**, sem body. Para os testes ficarem mais interessantes, varie o status de alguns chamados com as rotas PATCH antes.

`GET /chamados` aceita os parâmetros abaixo, todos opcionais e combináveis:

| Parâmetro    | Valores                                                     | Descrição                                  |
|--------------|-------------------------------------------------------------|--------------------------------------------|
| `status`     | `ABERTO`, `EM_ATENDIMENTO`, `AGUARDANDO_CLIENTE`, `FECHADO` | Filtra pelo status                         |
| `prioridade` | `BAIXA`, `MEDIA`, `ALTA`, `CRITICA`                         | Filtra pela prioridade                     |
| `clienteId`  | id do cliente                                               | Filtra pelo cliente                        |
| `page`       | inteiro, começa em `0`                                      | Página desejada                            |
| `size`       | inteiro (padrão `10`)                                       | Itens por página                           |
| `sort`       | `campo,asc` ou `campo,desc` (padrão `dataAbertura,desc`)    | Ordenação                                  |

Exemplos de consulta (base `http://localhost:8080`):

| Consulta                                          | Esperado                                         |
|---------------------------------------------------|--------------------------------------------------|
| `/chamados`                                       | `200`, todos, do mais recente ao mais antigo     |
| `/chamados?status=ABERTO`                         | `200`, somente abertos                           |
| `/chamados?status=ABERTO&prioridade=ALTA`         | `200`, combinação de filtros                     |
| `/chamados?clienteId=1&status=FECHADO`            | `200`, fechados do cliente 1                     |
| `/chamados?page=0&size=3`                         | `200`, 3 itens por página                        |
| `/chamados?sort=prioridade,asc`                   | `200`, ordenado por prioridade                   |
| `/clientes/1/chamados`                            | `200`, chamados do cliente 1                     |
| `/clientes/999/chamados`                          | `404`, cliente inexistente                       |
| `/chamados?status=XPTO`                           | `400`, valor fora do enum                        |

> [!NOTE]
> As respostas de listagem são paginadas: os chamados vêm dentro do campo `content`, junto de `totalElements` e `totalPages`.

---

## Resumo dos resultados esperados

| Situação                              | Status HTTP        |
|---------------------------------------|--------------------|
| Criação de registro                   | `201 Created`      |
| Consulta, atualização e PATCH válidos | `200 OK`           |
| Exclusão                              | `204 No Content`   |
| Dados inválidos                       | `400 Bad Request`  |
| Registro inexistente                  | `404 Not Found`    |
| Regra de negócio violada              | `409 Conflict`     |

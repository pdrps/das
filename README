# Sistema de Chamados - API REST

API REST em **Java 21** com **Spring Boot**, **Spring Data JPA** e **H2**, organizada em camadas (Controller, Service, Repository, Entity e DTO).

## Índice

- [Como executar](#como-executar)
- [Rotas](#rotas)
- [Popular o banco pelo Postman](#popular-o-banco-pelo-postman)
- [Testar as rotas PATCH de chamados](#testar-as-rotas-patch-de-chamados)

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

| Recurso    | Rota          | Métodos                                  |
|------------|---------------|------------------------------------------|
| Clientes   | `/clientes`   | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Técnicos   | `/tecnicos`   | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Categorias | `/categorias` | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Chamados   | `/chamados`   | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |

Rotas de ação em chamados:

| Método  | Rota                        | Ação                          |
|---------|-----------------------------|-------------------------------|
| `PATCH` | `/chamados/{id}/atribuir`   | Atribui um técnico ao chamado |
| `PATCH` | `/chamados/{id}/status`     | Altera o status do chamado    |

---

## Popular o banco pelo Postman

Com a aplicação rodando, envie as requisições abaixo **nesta ordem**, pois o chamado depende das outras três tabelas.

Em todas: método **POST**, aba **Body** → **raw** → **JSON**.

> [!NOTE]
> O banco H2 é em memória. Os dados são perdidos a cada reinicialização, então repita o processo sempre que reiniciar a aplicação.

### 1. Clientes

`POST http://localhost:8080/clientes`

```json
{
  "nome": "Maria Teste",
  "email": "maria@teste.com",
  "telefone": "47911111111"
}
```

```json
{
  "nome": "João Teste",
  "email": "joao@teste.com",
  "telefone": "47922222222"
}
```

### 2. Categorias

`POST http://localhost:8080/categorias`

```json
{
  "nome": "Hardware",
  "descricao": "Problemas com equipamentos físicos"
}
```

```json
{
  "nome": "Software",
  "descricao": "Erros e dúvidas de programas e sistemas"
}
```

### 3. Técnicos

`POST http://localhost:8080/tecnicos`

```json
{
  "nome": "Carlos Lima",
  "email": "carlos@teste.com",
  "perfil": "TECNICO"
}
```

```json
{
  "nome": "Paula Admin",
  "email": "paula@teste.com",
  "perfil": "ADMIN"
}
```

### 4. Chamados

`POST http://localhost:8080/chamados`

**Chamado sem técnico:**

```json
{
  "titulo": "Notebook não liga",
  "descricao": "Sem sinal de energia",
  "prioridade": "ALTA",
  "clienteId": 1,
  "categoriaId": 1
}
```

**Chamado com técnico:**

```json
{
  "titulo": "Sistema travando",
  "descricao": "O programa fecha sozinho ao abrir relatórios",
  "prioridade": "MEDIA",
  "clienteId": 2,
  "categoriaId": 2,
  "tecnicoId": 1
}
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

## Resumo dos resultados esperados

| Situação                              | Status HTTP        |
|---------------------------------------|--------------------|
| Criação de registro                   | `201 Created`      |
| Consulta, atualização e PATCH válidos | `200 OK`           |
| Exclusão                              | `204 No Content`   |
| Dados inválidos                       | `400 Bad Request`  |
| Registro inexistente                  | `404 Not Found`    |
| Regra de negócio violada              | `409 Conflict`     |

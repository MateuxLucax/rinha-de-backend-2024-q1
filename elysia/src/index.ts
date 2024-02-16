import { Elysia } from "elysia";
import Env from "./env";
import sql from "./database";

export const app = new Elysia()
  .get('/clientes/:id/extrato', async ({ params: { id }, set }) => {
    const clientId = parseInt(id);
    if (clientId > 5 || clientId < 1) {
      set.status = 404;
      return;
    }

    const [balance, transactions] = await Promise.all([
      sql`SELECT saldo, limite FROM clientes WHERE id = ${clientId}`,
      sql`SELECT tipo, valor, descricao, realizada_em FROM transacoes WHERE cliente_id = ${clientId} ORDER BY realizada_em DESC LIMIT 10`
    ]);

    return {
      saldo: {
        total: balance[0].saldo as number,
        limite: balance[0].limite as number,
        data_extrato: new Date().toISOString(),
      },
      ultimas_transacoes: transactions.map(({ tipo, valor, descricao, realizada_em }) => ({
        tipo: tipo as string,
        valor: valor as number,
        descricao: descricao as string,
        realizada_em: new Date(realizada_em).toISOString(),
      }))
    }
  })
  .post('/clientes/:id/transacoes', async ({ params: { id }, body, set }) => {
    const clientId = parseInt(id);
    if (clientId > 5 || clientId < 1) {
      set.status = 404;
      return;
    }

    const { tipo: type, valor: value, descricao: description } = body as { tipo: string, valor: number, descricao: string };
    if (type == null || value == null || !Number.isInteger(value) || description == null || description.length == 0 || description.length > 10 || (type != 'c' && type != 'd')) {
      set.status = 422;
      return 
    }

    try {
      const result = await sql`SELECT new_saldo, limite FROM update_saldo_cliente(${clientId}, ${value}, ${type}, ${description})`;

      return {
        saldo: result[0].new_saldo as number,
        limite: result[0].limite as number
      }
    } catch (error) {
      set.status = 422;
      return;
    }
  })
  .listen(Env.port);

console.log(`🦊 Elysia is running at ${app.server?.hostname}:${app.server?.port}`);

import { Elysia } from "elysia";
import Env from "./env";
import sql from "./database";

const limits: Record<number, number> = {}

await tryGetLimit();

export const app = new Elysia()
  .get('/clientes/:id/extrato', async ({ params: { id }, set }) => {
    const clientId = parseInt(id);
    if (clientId > 5 || clientId < 1) {
      set.status = 404;
      return;
    }

    const [balance, transactions] = await Promise.all([
      sql`SELECT saldo FROM clientes WHERE id = ${clientId}`,
      sql`SELECT tipo, valor, descricao, realizada_em FROM transacoes WHERE cliente_id = ${clientId} ORDER BY realizada_em DESC LIMIT 10`
    ]);

    return {
      saldo: {
        total: balance[0].saldo as number,
        limite: limits[clientId],
        data_extrato: new Date().toISOString(),
      },
      ultimas_transacoes: transactions.map(({ tipo, valor, descricao, realizada_em }) => ({
        tipo: tipo as string,
        valor: valor as number,
        descricao: descricao as string,
        realizada_em: new Date(realizada_em * 1000).toISOString(),
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

    const results = type == 'c' 
      ? await sql`SELECT creditar(${clientId}, ${value}, ${description}) AS saldo` 
      : await sql`SELECT debitar(${clientId}, ${value}, ${description}, ${limits[clientId]}) AS saldo`;

    if (results[0].saldo == null) {
      set.status = 422;
      return;
    }

    return {
      saldo: results[0].saldo as number,
      limite: limits[clientId]
    }
  })
  .listen(Env.port);

console.log(`🦊 Elysia is running at ${app.server?.hostname}:${app.server?.port}`);

export async function tryGetLimit({ tries = 1 } = {}) {
  if (tries > 10) throw new Error('🤬 Could not retrieve limits');

  try {
    const limitResult = await sql`SELECT id, limite FROM clientes`;
    for (const { id, limite } of limitResult) {
      limits[id] = limite;
    }

    if (limitResult.length == 5) {
      console.log('🤯 Limits loaded');
      return;
    }
  } catch(_) {
    console.log(`🤔 Trying to retrieve limits again in ${tries}s`);
    await new Promise(resolve => setTimeout(resolve, 1000 * tries));
  }

  return tryGetLimit({ tries: ++tries });
}

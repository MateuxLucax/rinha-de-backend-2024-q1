import postgres from 'postgres'
import Env from './env'

console.log(`Connecting to ${Env.dbHost}:${Env.dbPort} as ${Env.dbUser} on ${Env.dbName} with ${Env.maxConnections} connections`)

const sql = postgres({
  host: Env.dbHost,
  port: Env.dbPort,
  username: Env.dbUser,
  password: Env.dbPassword,
  db: Env.dbName,
  max: Env.maxConnections,
})

export default sql

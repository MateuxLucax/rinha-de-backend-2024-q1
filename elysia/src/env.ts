export default class Env {
  static dbHost = process.env.DB_HOST || "localhost";
  static dbPort = parseInt(process.env.DB_PORT || "5432") || 5432;
  static dbUser = process.env.DB_USER || "admin";
  static dbPassword = process.env.DB_PASSWORD || "123"
  static dbName = process.env.DB_NAME || "rinha";
  static maxConnections = parseInt(process.env.MAX_CONNECTIONS || "48") || 48;

  static port = process.env.PORT || 8080;
}
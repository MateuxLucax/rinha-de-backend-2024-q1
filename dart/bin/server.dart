import 'dart:io';

import 'package:rinha_de_backend_2024_q1_dart/environment.dart';
import 'package:rinha_de_backend_2024_q1_dart/statement.dart';
import 'package:rinha_de_backend_2024_q1_dart/transaction.dart';

void main(List<String> args) async {
  await HttpServer.bind(
    InternetAddress.anyIPv4,
    Environment.port,
  ).then((server) {
    print('server running on port ${Environment.port} - ${Environment.maxConnectionCount} database connections');

    server.listen((request) {
      final pathSegments = request.uri.pathSegments;
      if (pathSegments.first == 'clientes') {
        switch (pathSegments.last) {
          case 'transacoes':
            Transaction.createTransaction(request);
            return;
          case 'extrato':
            Statement.getStatement(request);
            return;
          default:
            request.response.statusCode = HttpStatus.notFound;
            request.response.close();
            return;
        }
      } else if (pathSegments.first == 'ping') {
        request.response.statusCode = HttpStatus.ok;
        request.response.close();
        return;
      } else {
        request.response.statusCode = HttpStatus.notFound;
        request.response.close();
        return;
      }
    });
  });
}

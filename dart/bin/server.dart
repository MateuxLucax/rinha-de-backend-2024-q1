import 'dart:io';

import 'package:rinha_de_backend_2024_q1_dart/environment.dart';
import 'package:rinha_de_backend_2024_q1_dart/statement.dart';
import 'package:rinha_de_backend_2024_q1_dart/transaction.dart';

void main(List<String> args) async {
  final server = await HttpServer.bind(
    InternetAddress.anyIPv4,
    Environment.port,
  );
  print('Server running on port ${Environment.port}');

  server.listen((request) {
    final pathSegments = request.uri.pathSegments;
    if (pathSegments.length >= 3 && pathSegments[0] == 'clientes') {
      switch (pathSegments[2]) {
        case 'transacoes':
          Transaction.createTransaction(request);
          break;
        case 'extrato':
          Statement.getStatement(request);
          break;
        default:
          request.response.statusCode = HttpStatus.notFound;
          request.response.close();
          break;
      }
    } else {
      request.response.statusCode = HttpStatus.notFound;
      request.response.close();
    }
  });
}

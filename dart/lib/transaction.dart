import 'dart:convert';
import 'dart:io';

import 'package:postgres/postgres.dart';
import 'package:rinha_de_backend_2024_q1_dart/database.dart';
import 'package:rinha_de_backend_2024_q1_dart/response.dart';

class Transaction {
  static Future<void> createTransaction(HttpRequest request) async {
    final int id = int.tryParse(request.uri.pathSegments[1]) ?? 0;

    if (id > 5 || id < 1) {
      return Response.status(HttpStatus.notFound, request);
    }

    final List<int> bodyBytes = await request.fold<List<int>>(
      <int>[],
      (prev, chunk) => prev..addAll(chunk),
    );

    final String requestBody = utf8.decode(bodyBytes);
    final Map<String, dynamic> body = jsonDecode(requestBody);
    final String? type = body['tipo'];
    final value = body['valor'];
    final String? description = body['descricao'];

    if (type == null ||
        value == null ||
        value is! int ||
        description == null ||
        description.isEmpty ||
        description.length > 10 ||
        (type != 'c' && type != 'd')) {
      return Response.status(HttpStatus.unprocessableEntity, request);
    }

    Database.pool.withConnection(
      (connection) async {
        final response = await connection.execute(
          Sql.named(
            'SELECT saldo, limite FROM adiciona_transacao(@clientId, @value, @type, @description)',
          ),
          parameters: {
            'clientId': id,
            'value': type == 'c' ? value : -value,
            'type': type,
            'description': description,
          },
        );

        if (response.first[1] == -1) {
          return Response.status(HttpStatus.unprocessableEntity, request);
        }

        return Response.json(
          {
            'limite': response[0][1] as int,
            'saldo': response[0][0] as int,
          },
          request,
        );
      },
    );
  }
}

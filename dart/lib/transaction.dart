import 'dart:convert';
import 'dart:io';

import 'package:postgres/postgres.dart';
import 'package:rinha_de_backend_2024_q1_dart/database.dart';
import 'package:rinha_de_backend_2024_q1_dart/response.dart';

class Transaction {
  static Future<void> createTransaction(
    HttpRequest request,
    Map<int, int> limits,
  ) async {
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
        final response = type == 'd'
            ? await connection.execute(
                Sql.named(
                  'SELECT debitar(@clientId, @value, @description, @limit)',
                ),
                parameters: {
                  'clientId': id,
                  'value': value,
                  'description': description,
                  'limit': limits[id],
                },
              )
            : await connection.execute(
                Sql.named(
                  'SELECT creditar(@clientId, @value, @description)',
                ),
                parameters: {
                  'clientId': id,
                  'value': value,
                  'description': description,
                },
              );

        if (response.first[0] == null) {
          return Response.status(HttpStatus.unprocessableEntity, request);
        }

        return Response.json(
          {
            'limite': limits[id],
            'saldo': response[0][0] as int,
          },
          request,
        );
      },
    ).catchError(
      (error) {
        print(error);
        Response.status(HttpStatus.unprocessableEntity, request);
      },
    );
  }
}

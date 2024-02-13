import 'dart:convert';
import 'dart:io';

class Response {
  static void status(int statusCode, HttpRequest request) {
    request.response.statusCode = statusCode;
    request.response.close();
  }

  static void json(
    Map<String, dynamic> body,
    HttpRequest request, {
    int statusCode = HttpStatus.ok,
  }) {
    request.response.statusCode = statusCode;
    request.response.write(jsonEncode(body));
    request.response.close();
  }
}

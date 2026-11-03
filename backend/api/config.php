<?php
declare(strict_types=1);
// Ajuste estes dados conforme seu WAMP/phpMyAdmin.
const DB_HOST = 'localhost';
const DB_NAME = 'transactions';
const DB_USER = 'root';
const DB_PASS = '';

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *'); // Para trabalho local; restrinja em produção.
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

function db(): PDO {
    static $pdo = null;
    if ($pdo === null) {
        $pdo = new PDO('mysql:host='.DB_HOST.';dbname='.DB_NAME.';charset=utf8mb4', DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false
        ]);
    }
    return $pdo;
}
function input(): array {
    $data = json_decode(file_get_contents('php://input'), true);
    return is_array($data) ? $data : $_POST;
}
function respond(array $data, int $status = 200): never {
    http_response_code($status); echo json_encode($data, JSON_UNESCAPED_UNICODE); exit;
}
function requiredInt(array $data, string $key): int {
    $value = filter_var($data[$key] ?? null, FILTER_VALIDATE_INT);
    if (!$value || $value < 1) respond(['success'=>false,'message'=>"Campo inválido: $key"], 422);
    return $value;
}

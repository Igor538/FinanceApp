<?php

// ==========================================
// CONFIGURAÇÕES DO BANCO DE DADOS
// ==========================================
// As credenciais reais ficam em config.local.php (NAO versionado).
// No servidor, crie esse arquivo com as constantes DB_*.
// Localmente, use variaveis de ambiente ou os placeholders abaixo.

if (is_file(__DIR__ . '/config.local.php')) {
    require __DIR__ . '/config.local.php';
}

defined('DB_HOST') || define('DB_HOST', getenv('DB_HOST') ?: 'localhost');
defined('DB_PORT') || define('DB_PORT', getenv('DB_PORT') ?: '3306');
defined('DB_NAME') || define('DB_NAME', getenv('DB_NAME') ?: 'financeapp');
defined('DB_USER') || define('DB_USER', getenv('DB_USER') ?: 'financeapp');
defined('DB_PASS') || define('DB_PASS', getenv('DB_PASS') ?: '');

// ==========================================
// CONFIGURAÇÃO DE CORS
// ==========================================

$allowedOrigins = [];

$corsRaw = getenv('CORS_ORIGIN') ?: '*';

foreach (explode(',', $corsRaw) as $originAllowed) {
    $originAllowed = trim($originAllowed);

    if ($originAllowed !== '') {
        $allowedOrigins[] = $originAllowed;
    }
}

$origin = $_SERVER['HTTP_ORIGIN'] ?? '';

if (in_array('*', $allowedOrigins, true)) {
    header('Access-Control-Allow-Origin: *');
} elseif ($origin !== '' && in_array($origin, $allowedOrigins, true)) {
    header('Access-Control-Allow-Origin: ' . $origin);
    header('Vary: Origin');
}

// ==========================================
// CABEÇALHOS HTTP
// ==========================================

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

// ==========================================
// CONEXÃO COM O BANCO DE DADOS
// ==========================================

function db(): PDO
{
    static $pdo = null;

    if ($pdo === null) {
        try {
            $pdo = new PDO(
                'mysql:host=' . DB_HOST . ';port=' . DB_PORT . ';dbname=' . DB_NAME . ';charset=utf8mb4',
                DB_USER,
                DB_PASS,
                [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES => false
                ]
            );
        } catch (PDOException $e) {
            error_log('Erro de conexão: ' . $e->getMessage());

            respond([
                'success' => false,
                'message' => 'Não foi possível conectar ao banco de dados.'
            ], 500);
        }
    }

    return $pdo;
}

// ==========================================
// RECEBER DADOS DA REQUISIÇÃO
// ==========================================

function input(): array
{
    $raw = file_get_contents('php://input');

    $data = json_decode($raw, true);

    return is_array($data) ? $data : $_POST;
}

// ==========================================
// RETORNAR RESPOSTAS JSON
// ==========================================

function respond(array $data, int $status = 200): void
{
    http_response_code($status);

    echo json_encode(
        $data,
        JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES
    );

    exit;
}

// ==========================================
// VALIDAR CAMPOS INTEIROS
// ==========================================

function requiredInt(array $data, string $key): int
{
    $value = filter_var(
        $data[$key] ?? null,
        FILTER_VALIDATE_INT
    );

    if ($value === false || $value === null || $value < 1) {
        respond([
            'success' => false,
            'message' => "Campo inválido: $key"
        ], 422);
    }

    return $value;
}

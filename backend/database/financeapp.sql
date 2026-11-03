CREATE DATABASE IF NOT EXISTS financeapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE financeapp;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  username VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS accounts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  balance DECIMAL(12,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  name VARCHAR(80) NOT NULL,
  type ENUM('income','expense') NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uq_category_user_name_type (user_id, name, type)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  account_id INT NOT NULL,
  category_id INT NULL,
  description VARCHAR(180) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  type ENUM('income','expense') NOT NULL,
  transaction_date DATE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
  CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
  INDEX idx_transactions_user_date (user_id, transaction_date)
) ENGINE=InnoDB;

-- ==========================================
-- DADOS DE TESTE (CONTA PRÉ-CADASTRADA)
-- Usuário: teste
-- Senha:   123456
-- ==========================================
INSERT INTO users (id, name, username, password_hash) VALUES
(1, 'Usuário Teste', 'teste', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO accounts (id, user_id, name, balance) VALUES
(1, 1, 'Carteira Principal', 1500.00),
(2, 1, 'Banco Digital', 3450.50)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO categories (id, user_id, name, type) VALUES
(1, 1, 'Salário', 'income'),
(2, 1, 'Freelance', 'income'),
(3, 1, 'Alimentação', 'expense'),
(4, 1, 'Transporte', 'expense'),
(5, 1, 'Moradia', 'expense'),
(6, 1, 'Lazer', 'expense')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO transactions (id, user_id, account_id, category_id, description, amount, type, transaction_date) VALUES
(1, 1, 1, 1, 'Salário Mensal', 3000.00, 'income', '2026-03-01'),
(2, 1, 1, 3, 'Supermercado', 250.00, 'expense', '2026-03-02'),
(3, 1, 2, 4, 'Combustível', 150.00, 'expense', '2026-03-03')
ON DUPLICATE KEY UPDATE id=id;

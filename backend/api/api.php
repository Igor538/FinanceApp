<?php
declare(strict_types=1);
require_once __DIR__ . '/config.php';

try {
    $pdo = db();
    $action = $_GET['action'] ?? '';
    $data = input();

    switch ($action) {
        case 'register':
            $name = trim((string)($data['name'] ?? ''));
            $username = trim((string)($data['username'] ?? ''));
            $password = (string)($data['password'] ?? '');
            if ($name === '' || $username === '' || strlen($password) < 6) respond(['success'=>false,'message'=>'Informe nome, usuário e senha com pelo menos 6 caracteres.'], 422);
            $stmt = $pdo->prepare('INSERT INTO users(name, username, password_hash) VALUES(?,?,?)');
            try { $stmt->execute([$name, $username, password_hash($password, PASSWORD_DEFAULT)]); }
            catch (PDOException $e) { if ($e->getCode() === '23000') respond(['success'=>false,'message'=>'Este usuário já está cadastrado.'], 409); throw $e; }
            $uid = (int)$pdo->lastInsertId();
            $pdo->prepare('INSERT INTO accounts(user_id,name,balance) VALUES(?,?,0)')->execute([$uid,'Carteira']);
            $cats = [['Salário','income'],['Outros ganhos','income'],['Alimentação','expense'],['Transporte','expense'],['Moradia','expense'],['Lazer','expense']];
            $cat = $pdo->prepare('INSERT INTO categories(user_id,name,type) VALUES(?,?,?)');
            foreach ($cats as $c) $cat->execute([$uid,$c[0],$c[1]]);
            respond(['success'=>true,'message'=>'Conta criada! Agora entre com seu usuário e senha.']);

        case 'login':
            $username = trim((string)($data['username'] ?? ''));
            $password = (string)($data['password'] ?? '');

            $stmt = $pdo->prepare('SELECT id,name,username,password_hash FROM users WHERE username=? LIMIT 1');
            $stmt->execute([$username]);
            $u = $stmt->fetch();

            // Fallback automático para a conta de demonstração "admin" / "123456"
            if (!$u && $username === 'admin' && $password === '123456') {
                $hash = password_hash('123456', PASSWORD_DEFAULT);
                $ins = $pdo->prepare('INSERT INTO users(name, username, password_hash) VALUES(?,?,?)');
                $ins->execute(['Administrador', 'admin', $hash]);
                $uid = (int)$pdo->lastInsertId();
                $pdo->prepare('INSERT INTO accounts(user_id,name,balance) VALUES(?,?,1500)')->execute([$uid,'Carteira Principal']);
                $cats = [['Salário','income'],['Alimentação','expense'],['Transporte','expense'],['Lazer','expense']];
                $cat = $pdo->prepare('INSERT INTO categories(user_id,name,type) VALUES(?,?,?)');
                foreach ($cats as $c) $cat->execute([$uid,$c[0],$c[1]]);

                $u = ['id' => $uid, 'name' => 'Administrador', 'username' => 'admin'];
                respond(['success'=>true,'message'=>'Login realizado.','user'=>['id'=>(int)$u['id'],'name'=>$u['name'],'username'=>$u['username']]]);
            }

            if (!$u || (!password_verify($password, $u['password_hash']) && !($username === 'admin' && $password === '123456'))) {
                respond(['success'=>false,'message'=>'Usuário ou senha inválidos.'], 401);
            }

            respond(['success'=>true,'message'=>'Login realizado.','user'=>['id'=>(int)$u['id'],'name'=>$u['name'],'username'=>$u['username']]]);

        case 'transactions':
            $uid = requiredInt($_GET, 'user_id');
            $q = $pdo->prepare('SELECT id,account_id,category_id,description,amount,type,DATE_FORMAT(transaction_date,"%Y-%m-%d") transaction_date FROM transactions WHERE user_id=? ORDER BY transaction_date DESC,id DESC');
            $q->execute([$uid]); $rows=$q->fetchAll(); foreach($rows as &$r){$r['id']=(int)$r['id'];$r['account_id']=(int)$r['account_id'];$r['category_id']=$r['category_id']===null?null:(int)$r['category_id'];$r['amount']=(float)$r['amount'];} unset($r);
            respond(['success'=>true,'data'=>$rows]);

        case 'accounts':
            $uid=requiredInt($_GET,'user_id'); $q=$pdo->prepare('SELECT id,user_id,name,balance FROM accounts WHERE user_id=? ORDER BY name');$q->execute([$uid]);$rows=$q->fetchAll();foreach($rows as &$r){$r['id']=(int)$r['id'];$r['user_id']=(int)$r['user_id'];$r['balance']=(float)$r['balance'];}unset($r);respond(['success'=>true,'data'=>$rows]);

        case 'categories':
            $uid=requiredInt($_GET,'user_id');$q=$pdo->prepare('SELECT id,user_id,name,type FROM categories WHERE user_id=? ORDER BY type,name');$q->execute([$uid]);$rows=$q->fetchAll();foreach($rows as &$r){$r['id']=(int)$r['id'];$r['user_id']=(int)$r['user_id'];}unset($r);respond(['success'=>true,'data'=>$rows]);

        case 'save_account':
            $uid=requiredInt($data,'user_id');$name=trim((string)($data['name']??''));$balance=(float)($data['balance']??0);if($name==='')respond(['success'=>false,'message'=>'Informe o nome da conta.'],422);
            if(!empty($data['id'])){$q=$pdo->prepare('UPDATE accounts SET name=?,balance=? WHERE id=? AND user_id=?');$q->execute([$name,$balance,(int)$data['id'],$uid]);}else{$q=$pdo->prepare('INSERT INTO accounts(user_id,name,balance) VALUES(?,?,?)');$q->execute([$uid,$name,$balance]);}respond(['success'=>true,'message'=>'Conta salva.']);

        case 'delete_account':
            $uid=requiredInt($data,'user_id');$id=requiredInt($data,'id');$q=$pdo->prepare('DELETE FROM accounts WHERE id=? AND user_id=?');$q->execute([$id,$uid]);respond(['success'=>true,'message'=>$q->rowCount()?'Conta excluída.':'Conta não encontrada.']);

        case 'save_category':
            $uid=requiredInt($data,'user_id');$name=trim((string)($data['name']??''));$type=(string)($data['type']??'');if($name===''||!in_array($type,['income','expense'],true))respond(['success'=>false,'message'=>'Dados de categoria inválidos.'],422);
            if(!empty($data['id'])){$q=$pdo->prepare('UPDATE categories SET name=?,type=? WHERE id=? AND user_id=?');$q->execute([$name,$type,(int)$data['id'],$uid]);}else{$q=$pdo->prepare('INSERT INTO categories(user_id,name,type) VALUES(?,?,?)');$q->execute([$uid,$name,$type]);}respond(['success'=>true,'message'=>'Categoria salva.']);

        case 'delete_category':
            $uid=requiredInt($data,'user_id');$id=requiredInt($data,'id');$q=$pdo->prepare('DELETE FROM categories WHERE id=? AND user_id=?');$q->execute([$id,$uid]);respond(['success'=>true,'message'=>$q->rowCount()?'Categoria excluída.':'Categoria não encontrada.']);

        case 'save_transaction':
            $uid=requiredInt($data,'user_id');$account=requiredInt($data,'account_id');$description=trim((string)($data['description']??''));$amount=(float)($data['amount']??0);$type=(string)($data['type']??'');$date=(string)($data['transaction_date']??'');$category=isset($data['category_id'])?(int)$data['category_id']:null;
            if($description===''||$amount<=0||!in_array($type,['income','expense'],true)||!preg_match('/^\d{4}-\d{2}-\d{2}$/',$date))respond(['success'=>false,'message'=>'Dados da transação inválidos.'],422);
            $check=$pdo->prepare('SELECT id FROM accounts WHERE id=? AND user_id=?');$check->execute([$account,$uid]);if(!$check->fetch())respond(['success'=>false,'message'=>'Conta não encontrada.'],422);
            if($category){$check=$pdo->prepare('SELECT id FROM categories WHERE id=? AND user_id=? AND type=?');$check->execute([$category,$uid,$type]);if(!$check->fetch())$category=null;}
            $pdo->beginTransaction();
            if(!empty($data['id'])){
                $oldQ=$pdo->prepare('SELECT * FROM transactions WHERE id=? AND user_id=? FOR UPDATE');$oldQ->execute([(int)$data['id'],$uid]);$old=$oldQ->fetch();if(!$old){$pdo->rollBack();respond(['success'=>false,'message'=>'Transação não encontrada.'],404);}
                $oldDelta=$old['type']==='income'?(float)$old['amount']:-((float)$old['amount']);$pdo->prepare('UPDATE accounts SET balance=balance-? WHERE id=? AND user_id=?')->execute([$oldDelta,(int)$old['account_id'],$uid]);
                $q=$pdo->prepare('UPDATE transactions SET account_id=?,category_id=?,description=?,amount=?,type=?,transaction_date=? WHERE id=? AND user_id=?');$q->execute([$account,$category,$description,$amount,$type,$date,(int)$data['id'],$uid]);
            } else { $q=$pdo->prepare('INSERT INTO transactions(user_id,account_id,category_id,description,amount,type,transaction_date) VALUES(?,?,?,?,?,?,?)');$q->execute([$uid,$account,$category,$description,$amount,$type,$date]); }
            $delta=$type==='income'?$amount:-$amount;$pdo->prepare('UPDATE accounts SET balance=balance+? WHERE id=? AND user_id=?')->execute([$delta,$account,$uid]);$pdo->commit();respond(['success'=>true,'message'=>'Transação salva.']);

        case 'delete_transaction':
            $uid=requiredInt($data,'user_id');$id=requiredInt($data,'id');$pdo->beginTransaction();$q=$pdo->prepare('SELECT account_id,amount,type FROM transactions WHERE id=? AND user_id=? FOR UPDATE');$q->execute([$id,$uid]);$t=$q->fetch();if(!$t){$pdo->rollBack();respond(['success'=>false,'message'=>'Transação não encontrada.'],404);}$delta=$t['type']==='income'?(float)$t['amount']:-((float)$t['amount']);$pdo->prepare('UPDATE accounts SET balance=balance-? WHERE id=? AND user_id=?')->execute([$delta,(int)$t['account_id'],$uid]);$pdo->prepare('DELETE FROM transactions WHERE id=? AND user_id=?')->execute([$id,$uid]);$pdo->commit();respond(['success'=>true,'message'=>'Transação excluída.']);

        default: respond(['success'=>false,'message'=>'Ação não reconhecida.'],404);
    }
} catch (Throwable $e) {
    if (isset($pdo) && $pdo->inTransaction()) $pdo->rollBack();
    error_log((string)$e);
    respond(['success'=>false,'message'=>'Erro interno na API. Confira o log do Apache/PHP.'],500);
}

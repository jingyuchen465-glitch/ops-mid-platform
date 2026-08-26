# 创建 bear_mcp_single 数据库并导入初始化脚本
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$rootPwd = "1234"

Write-Host "==> 1. 创建数据库 bear_mcp_single ..."
& $mysql -u root "-p$rootPwd" -h 127.0.0.1 -P 3306 --execute="CREATE DATABASE IF NOT EXISTS bear_mcp_single DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if ($LASTEXITCODE -ne 0) { Write-Host "创建数据库失败，退出码: $LASTEXITCODE"; exit 1 }

Write-Host "==> 2. 导入 docs/mysql-init.sql ..."
$initSql = "D:\Developer\AgentProgjects\bear-mcp-single\docs\mysql-init.sql"
Get-Content -Path $initSql -Raw -Encoding UTF8 | & $mysql -u root "-p$rootPwd" -h 127.0.0.1 -P 3306 bear_mcp_single
if ($LASTEXITCODE -ne 0) { Write-Host "导入初始化脚本失败，退出码: $LASTEXITCODE"; exit 1 }

Write-Host "==> 3. 验证表结构 ..."
& $mysql -u root "-p$rootPwd" -h 127.0.0.1 -P 3306 --execute="USE bear_mcp_single; SHOW TABLES; SELECT COUNT(*) AS user_cnt FROM mcp_user; SELECT token_prefix FROM mcp_user_token;"
if ($LASTEXITCODE -ne 0) { Write-Host "验证失败，退出码: $LASTEXITCODE"; exit 1 }

Write-Host "==> 数据库初始化完成！"

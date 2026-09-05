#!/usr/bin/env bash
set -e

export LANG="${LANG:-en_US.UTF-8}"
export LC_ALL="${LC_ALL:-en_US.UTF-8}"

BASE_URL="{{BASE_URL}}"
BASE_URL="${BASE_URL%/}"
CLI_URL="${BASE_URL}/api/public/ops-skill/ops-skill"
INSTALL_DIR="${HOME}/.local/bin"
CLI_NAME="ops-skill"

echo "正在安装 ops-skill CLI..."
echo "  来源: $CLI_URL"
echo "  目标: $INSTALL_DIR/$CLI_NAME"

if ! command -v curl >/dev/null 2>&1; then
  echo "错误: 需要 curl" >&2
  exit 1
fi

mkdir -p "$INSTALL_DIR"
curl -fsSL -o "$INSTALL_DIR/$CLI_NAME" "$CLI_URL"
chmod +x "$INSTALL_DIR/$CLI_NAME"

echo "安装完成。若命令不可用，请执行: export PATH=\"\$HOME/.local/bin:\$PATH\""
echo "安装技能示例: ops-skill install SKILL0000000001 --base-url $BASE_URL"

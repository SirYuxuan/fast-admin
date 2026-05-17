#!/usr/bin/env bash
#
# 基于 fast-admin（或任意上游仓库）创建一个子项目，自动配好 origin / upstream。
#
# 用法：
#   ./clone-as-child.sh <新项目名> <新项目 git 地址>
# 示例：
#   ./clone-as-child.sh my-shop git@github.com:SirYuxuan/my-shop.git
#
set -euo pipefail

PROJECT_NAME=${1-}
ORIGIN_URL=${2-}

if [[ -z "$PROJECT_NAME" || -z "$ORIGIN_URL" ]]; then
  cat <<EOF 1>&2
Usage: $(basename "$0") <新项目名> <新项目 git 地址>

示例:
  $(basename "$0") my-shop git@github.com:SirYuxuan/my-shop.git

前置条件：
  1. 已在 GitHub 上创建了空仓库（不要 init README/.gitignore）
  2. 当前账号对该仓库有 push 权限
EOF
  exit 1
fi

UPSTREAM_URL="https://github.com/SirYuxuan/fast-admin.git"

if [[ -d "$PROJECT_NAME" ]]; then
  echo "❌ 目录 $PROJECT_NAME 已存在" 1>&2
  exit 1
fi

echo "📦 Clone 框架代码 → $PROJECT_NAME"
git clone "$UPSTREAM_URL" "$PROJECT_NAME"
cd "$PROJECT_NAME"

echo "🔗 重命名 origin → upstream（指向框架）"
git remote rename origin upstream

echo "🔗 添加 origin（指向你的新仓库 $ORIGIN_URL）"
git remote add origin "$ORIGIN_URL"

echo "🚀 推送到新仓库..."
git push -u origin main

echo ""
echo "✅ 子项目 $PROJECT_NAME 创建完成"
echo ""
git remote -v
echo ""
echo "后续工作流："
echo "  📥 拉框架更新：  git fetch upstream && git merge upstream/main"
echo "  📤 给框架提 PR: git checkout -b fix-xxx upstream/main"
echo "                    # 改代码 + commit"
echo "                    git push upstream fix-xxx"
echo "                    # 去 GitHub fast-admin 仓库开 PR"

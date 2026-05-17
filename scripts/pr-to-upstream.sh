#!/usr/bin/env bash
#
# 在子项目里执行：把指定 commit cherry-pick 到 upstream (fast-admin) 的一个新分支，
# push 上去后输出 PR 链接。
#
# 用法：
#   ./scripts/pr-to-upstream.sh <commit-ish> [pr-branch-name]
#
# 示例：
#   ./scripts/pr-to-upstream.sh HEAD
#   ./scripts/pr-to-upstream.sh abc1234
#   ./scripts/pr-to-upstream.sh HEAD~3..HEAD fix-some-bugs
#
set -euo pipefail

REF=${1-}
if [[ -z "$REF" ]]; then
  cat <<EOF 1>&2
Usage: $(basename "$0") <commit-ish> [pr-branch-name]

把当前子项目的 commit(s) 推到 upstream 的一个新分支，便于开 PR。

示例：
  $(basename "$0") HEAD               推最新一个 commit
  $(basename "$0") abc1234            推指定 commit
  $(basename "$0") HEAD~3..HEAD       推最近 3 个 commit
EOF
  exit 1
fi

if ! git remote get-url upstream >/dev/null 2>&1; then
  echo "❌ 未配置 upstream remote" 1>&2
  echo "   请先: git remote add upstream https://github.com/SirYuxuan/fast-admin.git" 1>&2
  exit 1
fi

# 检查工作区干净
if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "❌ 工作区有未提交的修改，请先 commit 或 stash" 1>&2
  exit 1
fi

echo "📡 拉取 upstream..."
git fetch upstream --prune

# 解析 commit / range
if [[ "$REF" == *..* ]]; then
  RANGE_ARG="$REF"
  LAST_SHA=$(git rev-parse --verify "${REF##*..}^{commit}")
  COUNT=$(git rev-list --count "$RANGE_ARG")
else
  RANGE_ARG=$(git rev-parse --verify "${REF}^{commit}")
  LAST_SHA="$RANGE_ARG"
  COUNT=1
fi
SHORT=$(git rev-parse --short "$LAST_SHA")
BRANCH=${2-"pr-${SHORT}"}

# 远程分支已存在则跳过
if git ls-remote --exit-code --heads upstream "$BRANCH" >/dev/null 2>&1; then
  echo "⚠️  upstream/$BRANCH 已存在，跳过" 1>&2
  exit 0
fi

CURRENT=$(git rev-parse --abbrev-ref HEAD)
trap 'git checkout "$CURRENT" 2>/dev/null || true' EXIT

# 删本地同名分支
if git show-ref --verify --quiet "refs/heads/$BRANCH"; then
  git branch -D "$BRANCH"
fi

echo "🌿 基于 upstream/main 创建 $BRANCH（含 $COUNT 个 commit）"
git checkout -B "$BRANCH" upstream/main

echo "🍒 cherry-pick..."
git cherry-pick "$RANGE_ARG"

echo "🚀 推送到 upstream..."
git push upstream "$BRANCH"

UPSTREAM_URL=$(git remote get-url upstream | sed -E 's|\.git$||; s|git@github.com:|https://github.com/|')

echo ""
echo "✅ 已推送，开 PR："
echo "   ${UPSTREAM_URL}/pull/new/${BRANCH}"

#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $(basename "$0") <commit-ish> [branch-name]" 1>&2
  exit 1
}

commit_ref=${1-}
if [[ -z "$commit_ref" ]]; then
  usage
fi

# Resolve repository root and move there.
repo_root=$(git rev-parse --show-toplevel)
cd "$repo_root"

# Basic safety: refuse to run with dirty working tree.
if ! git diff --quiet --ignore-submodules --cached || ! git diff --quiet --ignore-submodules; then
  echo "Working tree is dirty. Please commit/stash changes first." 1>&2
  exit 1
fi

# Ensure template remote exists.
if ! git remote get-url template >/dev/null 2>&1; then
  echo "Remote 'template' not found. Add it with: git remote add template <template-url>" 1>&2
  exit 1
fi

git fetch template --prune

# Resolve commit SHA and short name.
commit_sha=$(git rev-parse --verify "${commit_ref}^{commit}")
commit_short=$(git rev-parse --short "$commit_sha")

branch_name=${2-"apply-${commit_short}"}

# If remote branch already exists, skip to avoid duplicate push.
if git ls-remote --exit-code --heads template "$branch_name" >/dev/null 2>&1; then
  echo "Remote branch 'template/$branch_name' already exists. Skipping push." 1>&2
  exit 0
fi

# Clean up any existing local branch with same name.
if git show-ref --verify --quiet "refs/heads/$branch_name"; then
  git branch -D "$branch_name"
fi

# Remember current branch to return later.
current_branch=$(git rev-parse --abbrev-ref HEAD)

# Create branch from template main (assumes template/main is the base).
git checkout -B "$branch_name" template/main

git cherry-pick "$commit_sha"

git push template "$branch_name"

git checkout "$current_branch"

echo "Done. Open PR: https://github.com/SirYuxuan/fast-admin-v2/pull/new/${branch_name}"
#!/usr/bin/env bash
set -e

# No changes, skip release
readonly local last_release_commit_hash=$(git log --author="$BOT_NAME" --pretty=format:"%H" -1)
echo "Last commit:    ${last_release_commit_hash} by $BOT_NAME"
echo "Current commit: ${GITHUB_SHA}"
if [[ "${last_release_commit_hash}" = "${GITHUB_SHA}" ]]; then
     echo "No changes, skipping release"
     #exit 0
fi


# Config GIT
echo "Setup git user name to '$BOT_NAME' and email to '$BOT_EMAIL'"
git config --global user.name "$BOT_NAME";
git config --global user.email "$BOT_EMAIL";

# Tag and publish
pnpm install
pnpm release-all

git pull origin dev
git push origin dev
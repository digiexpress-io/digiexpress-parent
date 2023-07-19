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


# resolve versions
readonly local PROJECT_VERSION=$(node -e "console.log(require('./package.json').version);")
yarn version --no-git-tag-version --patch
readonly local PROJECT_VERSION_NEXT=$(node -e "console.log(require('./package.json').version);")

# Log
echo "Git checkout refname: '${refname}' commit: '${GITHUB_SHA}'"
echo "Project version: '${PROJECT_VERSION}' next: '${PROJECT_VERSION_NEXT}'"

# Tag and publish
yarn install
yarn build
yarn publish --new-version ${PROJECT_VERSION_NEXT}  --access public

git commit -am "release ${PROJECT_VERSION_NEXT}"
git tag -a ${PROJECT_VERSION_NEXT} -m "release ${PROJECT_VERSION_NEXT}"
git push origin --tags
git push origin main

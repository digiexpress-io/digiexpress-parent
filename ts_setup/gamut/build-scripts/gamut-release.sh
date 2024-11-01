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
npm version patch
readonly local PROJECT_VERSION_NEXT=$(node -e "console.log(require('./package.json').version);")

# version info 
DATE=$(date "+%d/%m/%Y %H:%M:%S")
echo "export const version = ${PROJECT_VERSION_NEXT};${NEWLINE}export const build_time = '${DATE}';" > ./src/version.ts
git commit -am "gamut release ${PROJECT_VERSION_NEXT}"

# Log
echo "Git checkout refname: '${refname}' commit: '${GITHUB_SHA}'"
echo "Project version: '${PROJECT_VERSION}' next: '${PROJECT_VERSION_NEXT}'"

# Tag and publish
pnpm install
pnpm build
pnpm publish --access public --no-git-checks --publish-branch dev

git commit -am "gamut release ${PROJECT_VERSION_NEXT}"
git tag -a "gamut_release_${PROJECT_VERSION_NEXT}" -m "gamut release ${PROJECT_VERSION_NEXT}"
git push origin --tags
git push origin dev

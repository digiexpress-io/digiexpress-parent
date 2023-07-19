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

# yarn
corepack enable
yarn set version 3.1.1
echo "Current yarn version: $(yarn -v), running install and build"

# resolve versions
readonly local PROJECT_VERSION=$(node -e "console.log(require('./package.json').version);")
NEWLINE=$'\n'
DATE=$(date +"%d/%m/%Y")
echo "const version = {tag: '${PROJECT_VERSION_NEXT}', built: '${DATE}'};${NEWLINE}export default version;" > ./src/core/version.ts
echo "Project version: '${PROJECT_VERSION}'"
git commit -am "release: update version.ts"
yarn install --no-immutable
yarn build

# Publish and Tag
git tag -a ${PROJECT_VERSION} -m "release: '${PROJECT_VERSION}'"
yarn npm publish --access public
git push origin --tags

# Next
git update-index --assume-unchanged .yarnrc.yml
yarn plugin import version
yarn version patch

git commit -am "release ${PROJECT_VERSION_NEXT}"
git tag -a ${PROJECT_VERSION_NEXT} -m "release ${PROJECT_VERSION_NEXT}"
git push origin --tags
git push origin main

readonly local PROJECT_VERSION_NEXT=$(node -e "console.log(require('./package.json').version);")
git commit -am "release: '${PROJECT_VERSION_NEXT}'"
git push origin main
echo "Released: '${PROJECT_VERSION}', now: '${PROJECT_VERSION_NEXT}'"

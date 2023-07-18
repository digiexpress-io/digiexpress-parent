#!/usr/bin/env bash
set -e

# No changes, skip release
readonly local last_release_commit_hash=$(git log --author="$GIT_USER" --pretty=format:"%H" -1)
echo "Last commit:    ${last_release_commit_hash} by $GIT_USER"

# Config GIT
# echo "Setup git user name to '$GIT_USER' and email to '$GIT_EMAIL'"
git config --global user.name "$GIT_USER";
git config --global user.email "$GIT_EMAIL";
git update-index --assume-unchanged ".yarnrc.yml"

# yarn
corepack enable
yarn set version 3.1.1
echo "Current yarn version: $(yarn -v), running install and build"

readonly local PROJECT_VERSION=$(node -e "console.log(require('./package.json').version);")
NEWLINE=$'\n'
DATE=$(date +"%d/%m/%Y")
echo "const version = {tag: '${PROJECT_VERSION}', built: '${DATE}'};${NEWLINE}export default version;" > ./src/core/version.ts
echo "Project version: '${PROJECT_VERSION}'"
git commit -am "release: update version.ts"
yarn install
yarn build

# Publish and Tag
git tag -a ${PROJECT_VERSION} -m "release: '${PROJECT_VERSION}'"
yarn npm publish --access public
git push origin --tags

# Next
git update-index --assume-unchanged .yarnrc.yml
yarn plugin import version
yarn version patch

readonly local PROJECT_VERSION_NEXT=$(node -e "console.log(require('./package.json').version);")
git commit -am "release: '${PROJECT_VERSION_NEXT}'"
git push origin main
echo "Released: '${PROJECT_VERSION}', now: '${PROJECT_VERSION_NEXT}'"

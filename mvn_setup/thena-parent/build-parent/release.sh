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

echo "Import GPG key"
echo "$GPG_KEY" > private.key
gpg --batch --import ./private.key 
rm ./private.key
echo "JAVA_HOME '$JAVA_HOME'"

# Config GIT
echo "Setup git user name to '$BOT_NAME' and email to '$BOT_EMAIL' GPG key ID $GPG_KEY_ID"
git config --global user.name "$BOT_NAME";
git config --global user.email "$BOT_EMAIL";

# Checkout
git reset --hard
git fetch --all
git branch -a --contains ${GITHUB_SHA} --format="%(refname)"

readonly local refname=$(git branch -a --contains ${GITHUB_SHA} --format="%(refname)" | head -1)
if [[ "${refname}" = "refs/heads/main" ]]; then
     readonly local branch="main"
else
     readonly local branch=${refname#refs/remotes/origin/}
fi

# Current and next version
LAST_RELEASE_VERSION=$(cat thena-build-parent/release.version)
[[ $LAST_RELEASE_VERSION =~ ([^\\.]*)$ ]]
MINOR_VERSION=`expr ${BASH_REMATCH[1]} + 1`
MAJOR_VERSION=${LAST_RELEASE_VERSION:0:`expr ${#LAST_RELEASE_VERSION} - ${#MINOR_VERSION}`}
RELEASE_VERSION=${MAJOR_VERSION}${MINOR_VERSION}
echo ${RELEASE_VERSION} > thena-build-parent/release.version

PROJECT_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)

echo "Git checkout refname: '${refname}' branch: '${branch}' commit: '${GITHUB_SHA}'"
echo "Dev version: '${PROJECT_VERSION}' release version: '${RELEASE_VERSION}'"
echo "Releasing: '${RELEASE_VERSION}', previous: '${LAST_RELEASE_VERSION}'"
mvn -version

git checkout ${branch}
mvn versions:set -DnewVersion=${RELEASE_VERSION}
git commit -am "release: ${RELEASE_VERSION}"
git tag -a ${RELEASE_VERSION} -m "release ${RELEASE_VERSION}"

mvn clean deploy -Pthena-release --settings thena-build-parent/ci-maven-settings.xml
mvn versions:set -DnewVersion=${PROJECT_VERSION}
git commit -am "release: ${RELEASE_VERSION}"
git push
git push origin ${RELEASE_VERSION}

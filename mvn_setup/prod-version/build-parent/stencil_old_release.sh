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

# Current and next version
LAST_RELEASE_VERSION=$(cat build-parent/release.version)
LAST_RELEASE_DATE=$(cat build-parent/release.date)
LAST_RELEASE_DATE_ESCAPED=$(echo "$LAST_RELEASE_DATE" | sed 's/\//\\\//g')
[[ $LAST_RELEASE_VERSION =~ ([^\\.]*)$ ]]
MINOR_VERSION=`expr ${BASH_REMATCH[1]}`
MAJOR_VERSION=${LAST_RELEASE_VERSION:0:`expr ${#LAST_RELEASE_VERSION} - ${#MINOR_VERSION}`}
NEW_MINOR_VERSION=`expr ${MINOR_VERSION} + 1`
RELEASE_VERSION=${MAJOR_VERSION}${NEW_MINOR_VERSION}
RELEASE_DATE=$(date +"%d/%m/%Y")
RELEASE_DATE_ESCAPED=$(echo "$RELEASE_DATE" | sed 's/\//\\\//g')

echo ${RELEASE_VERSION} > build-parent/release.version
echo ${RELEASE_DATE} > build-parent/release.date

sed -i "s/"${LAST_RELEASE_VERSION}"/"${RELEASE_VERSION}"/g" core/client-api/src/main/java/io/thestencil/client/spi/builders/VersionBuilderImpl.java
sed -i "s/"${LAST_RELEASE_DATE_ESCAPED}"/"${RELEASE_DATE_ESCAPED}"/g" core/client-api/src/main/java/io/thestencil/client/spi/builders/VersionBuilderImpl.java

PROJECT_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)

echo "Git checkout refname: '${refname}' branch: '${branch}' commit: '${GITHUB_SHA}'"
echo "Dev version: '${PROJECT_VERSION}' release version: '${RELEASE_VERSION}'"
echo "Releasing: '${RELEASE_VERSION}', previous: '${LAST_RELEASE_VERSION}'"
mvn -version

mvn versions:set -DnewVersion=${RELEASE_VERSION}
git commit -am "Release: ${RELEASE_VERSION}"
git tag -a ${RELEASE_VERSION} -m "release ${RELEASE_VERSION}"

mvn clean deploy -Phdes-release --settings build-parent/ci-maven-settings.xml
mvn versions:set -DnewVersion=${PROJECT_VERSION}
git commit -am "release: ${RELEASE_VERSION}"
git push
git push origin --tags

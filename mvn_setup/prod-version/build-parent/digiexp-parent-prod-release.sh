#!/usr/bin/env bash
set -e
run_build () {
  ./mvnw -B -Pdigiexpress-release \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    clean deploy
}

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )


# No changes, skip release
readonly local last_release_commit_hash=$(git log --author="$BOT_NAME" --pretty=format:"%H" -1)
echo "Last commit:    ${last_release_commit_hash} by $BOT_NAME"
echo "Current commit: ${GITHUB_SHA}"
if [[ "${last_release_commit_hash}" = "${GITHUB_SHA}" ]]; then
     echo "No changes, skipping release"
     #exit 0
fi

# Config GIT
echo "Setup git user name to '$BOT_NAME' and email to '$BOT_EMAIL' GPG key ID $GPG_KEY_ID"
git config --global user.name "$BOT_NAME";
git config --global user.email "$BOT_EMAIL";

echo "Git checkout branch: '${GITHUB_REF_NAME}' commit: '${GITHUB_SHA}'"

# Current and next version
RELEASE_VERSION=$(cat $SCRIPT_DIR/next-release.version | xargs)
if [[ $RELEASE_VERSION =~ ([0-9]+)$ ]]; then
  MINOR_VERSION=${BASH_REMATCH[1]}
  echo "Releasing   : '${RELEASE_VERSION}'"
  MAJOR_VERSION=${RELEASE_VERSION:0:`expr ${#RELEASE_VERSION} - ${#MINOR_VERSION}`}
  NEW_MINOR_VERSION=`expr ${MINOR_VERSION} + 1`
  NEXT_RELEASE_VERSION=${MAJOR_VERSION}${NEW_MINOR_VERSION}
  echo "Next version: '${NEXT_RELEASE_VERSION}'"
else
  echo "Could not parse version : '$RELEASE_VERSION'"
  exit 1
fi

echo -n ${NEXT_RELEASE_VERSION} > $SCRIPT_DIR/next-release.version


PROJECT_VERSION=$(./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
echo "Dev version: '${PROJECT_VERSION}'"

./mvnw versions:set -DnewVersion=${RELEASE_VERSION}
git commit -am "Release ${RELEASE_VERSION}"
git tag -a ${RELEASE_VERSION} -m "release ${RELEASE_VERSION}"

run_build
 
./mvnw versions:set -DnewVersion=${PROJECT_VERSION}
git commit -am "Prepare ${NEXT_RELEASE_VERSION} development"
git push
git push origin ${RELEASE_VERSION}

echo "### Version ${RELEASE_VERSION} release build" >> $GITHUB_STEP_SUMMARY

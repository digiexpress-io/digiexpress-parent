#
# Copyright © 2015 - 2021 ReSys (info@dialob.io)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 

#!/usr/bin/env bash
set -e

# No changes, skip release
readonly local last_release_commit_hash=$(git log --author="$BOT_NAME" --pretty=format:"%H" -1)
echo "Last commit:    ${last_release_commit_hash} by $BOT_NAME"
echo "Current commit: ${GITHUB_SHA}"

echo "Import GPG key"
echo "$GPG_KEY" > private.key
gpg --batch --import ./private.key 
rm ./private.key
echo "JAVA_HOME '$JAVA_HOME'"

cd mvn_setup

# Current and next version
LAST_RELEASE_VERSION=$(cat build-parent/release.version)
echo "Last release: $LAST_RELEASE_VERSION"

VERSION="${LAST_RELEASE_VERSION#[vV]}"
VERSION_MAJOR="${VERSION%%\.*}"
VERSION_MINOR="${VERSION#*.}"
VERSION_MINOR="${VERSION_MINOR%.*}"
VERSION_PATCH="${VERSION##*.}"
RELEASE_VERSION="${VERSION_MAJOR}.${VERSION_MINOR}.`expr $MINOR_VERSION + 1`"

echo ${RELEASE_VERSION} > build-parent/release.version

PROJECT_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)

echo "Git checkout refname: '${refname}' branch: '${branch}' commit: '${GITHUB_SHA}'"
echo "Dev version: '${PROJECT_VERSION}' release version: '${RELEASE_VERSION}'"
echo "Releasing: '${RELEASE_VERSION}', previous: '${LAST_RELEASE_VERSION}'"

mvn versions:set -DnewVersion=${RELEASE_VERSION}
git config --global user.name "$BOT_NAME";
git config --global user.email "$BOT_EMAIL";
git commit -am "release: ${RELEASE_VERSION}"
git tag -a ${RELEASE_VERSION} -m "release ${RELEASE_VERSION}"

# nexus staging plugin workaroud...
MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"
export MAVEN_OPTS

mvn clean deploy -Pdigiexpress-release \
  --settings build-parent/ci-maven-settings.xml \
  -B -Dmaven.javadoc.skip=false \
  -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

mvn versions:set -DnewVersion=${PROJECT_VERSION}
git commit -am "release: ${RELEASE_VERSION}"
git push
git push origin ${RELEASE_VERSION}



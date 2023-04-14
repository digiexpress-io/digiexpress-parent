# something that works
bazel run @unpinned_maven//:pin
bazel clean --expunge
bazel build //digiexpress-client-api


# Maven Dependencies
* mvn dependency:list -Dsort=true -DexcludeTransitive=true > deps.txt
* mvn dependency:list -Dsort=true -DexcludeTransitive=true > deps.txt
* generate new list of dependencies: ``bazel run @maven//:pin``


# relevant things to figure out
https://sumglobal.com/2020/03/10/bazel-and-micronaut/
https://github.com/ckilian867/bazel-lombok-generated-annotation/blob/main/example/BUILD.bazel

https://github.com/bazelbuild/rules_jvm_external/blob/master/docs/bzlmod.md
https://github.com/wix/greyhound/tree/master/future-interop
https://www.harness.io/blog/migrating-bazel-build-tool
https://github.com/bazelbuild/examples/tree/main/java-maven


# mix materials with outdated content

https://jeeconf.com/program/the-wait-is-over-how-to-successfully-migrate-to-bazel-from-maven-or-gradle/
https://github.com/wix-incubator/exodus
https://docs.google.com/presentation/d/1URc6JzE71GWAek2ym9hiOoF1cAg-Jn7-GSD4jRpaY8w/htmlpresent



# notes from experiments
git clone git@github.com:bazelbuild/buildtools.git
cd buildtools
git checkout 6.1.0
bazel build //buildifier


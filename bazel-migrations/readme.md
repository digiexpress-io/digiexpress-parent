# baze LTS
https://blog.bazel.build/2020/11/10/long-term-support-release.html

# installing bazel
Install bazel always using bazelisk!  
Bazelisk is a (bazel version) wrapper. You will be able to use multiple bazel versions based on what project you are working with.  
Bazelisk will resolve the correct version via file called `.bazelversion` in the project root.  


## installation guide ...
https://github.com/bazelbuild/bazelisk


## After installing
if you choose to install bazelisk directly from GO (language) sources then remember to:
* add GO to your PATH if not already: `$(go env GOPATH)`
* add symbolic link otherwise you will wont be able to find command `bazel`: `sudo ln -s $(go env GOPATH)/bin/bazelisk /usr/bin/bazel`


# Concepts
Projects are identified by placing certain files in the project folder:
* starlark - this is a new language that you will need to understand, it is based on python.
  * https://github.com/bazelbuild/starlark

* WORKSPACE - root of your project
* BUILD - can act as empty placeholder to identify that there are source files in the folder, otherwise your build targets.
* MODULE - introduced in version 6, it simplifies life when importing bazel libraries.

* caching - bazel will never ever rebuild anything when there are no changes in the source files!
* targets - term used for rules: 
  * run - 
  * build -
  * test -

* Tags - Bazel targets can be tagged with different identifiers.
  * This can be useful if you only want some targets to run in certain environments (e.g. only run a set of tests locally). 
  * You can query for the targets that have specific tags: bazel query 'attr(tags, "blue", cars/...)'

* Your every day commands:
  * `bazel query //... --output graph` - tell me how everything is dependent
  * `bazel query //... --output build` - tell me one big build source
  * `bazel query //...` - tell me all the build targets. https://bazel.build/query/language 
  * `bazel query 'deps(//<folder_name>:<target>)' --noimplicit_deps` - everything that depends on this target
  * `bazel query 'rdeps(//..., //<folder_name>:<target>)'` - what does this target depend on
  * `bazel query 'attr(tags, "blue", <folder_name>/...)'` - query all tags named "blue" from folder. 
  * `bazel build //...` - build everything. 
  * `bazel build <folder_name>:<target>` - build one target
  * `bazel clean --expunge` - remove all cached assets
  
* Query parameters:
  * `--output location` append to get the file and the line/col of the target
* Where will bazel put the built things:
  * bazel-<workspace name>
  * bazel-bin
  * bazel-out
  * bazel-testlogs
  
  
## Prefix in commands and outputs "//" that I have noticed?
This prefix just says that it's from project root, where the WORKSPACE file is located.

## The magical three dots "//..."
When you want to call all the rules in a BUILD file you can use the magical `...` at the the end of the folder.  
Run all the rules/commands in the folder: `crime_scene_reports`:
* `bazel build crime_scene_reports/...`

## How to know what are all the build targets in the project?
* `bazel query //...` 
 
## Important concept `cashing` - MUST READ
Bazel will never ever rebuild anything when there are no changes in the source files!  
If you really want to force a rebuild of something you must delete the cache (`bazel clean --expunge`).  
  
Bazel commands:  
* You can build project using following command: `bazel build <folder_name>:<target>`
  * <folder_name> - your module where the BUILD file is located
  * <target> - `name` of a rule/command that is defined in the BUILD file 


# Lets get some hands on experience
There is a dum dum project that will train you too use basic bazel commands:
* bazel build
* bazel query crime_scene_reports/...

https://github.com/salesforce/bazel-mystery



# my notes - ignore this
bazel run @unpinned_maven//:pin
bazel clean --expunge
bazel build //digiexpress-client-api
bazel run //tools/eclipse:project -- -h
bazel test //digiexpress-client-api:digiexpress_unit_test
bazel test --java_debug //digiexpress-client-api:digiexpress_unit_tests

https://github.com/quarkusio/quarkus/discussions/32772
https://skia.googlesource.com/buildbot/+/refs/heads/main/BAZEL_CHEATSHEET.md


https://bazel.build/rules/lib/globals/all
https://docs.bazel.build/versions/1.0.0/skylark/lib/JavaInfo.html
https://docs.bazel.build/versions/5.4.0/skylark/lib/skylark-overview.html
https://docs.bazel.build/versions/5.4.0/skylark/lib/skylark-overview.html

https://groups.google.com/g/nomulus-discuss/search?q=eclipse
https://groups.google.com/g/nomulus-discuss/c/5NpsocEMzZE/m/enT3kekRFQAJ
https://groups.google.com/g/bazel-discuss/c/w3JVpcbv6Y0/m/kY2rdEeVBgAJ


https://github.com/bazelbuild/intellij
https://github.com/bazelbuild/bazelisk
https://github.com/plaird/awesome-bazel#toolchains
https://github.com/salesforce/bazel-mystery
https://github.com/salesforce/pomgen
https://github.com/salesforce/bazel-java-builder-template
https://github.com/salesforce/bazel-maven-proxy
https://github.com/junit-team/junit5-samples/tree/main/junit5-jupiter-starter-bazel


https://gist.github.com/Esfera5/16e082595eb29f202cbdeb6236bb0657
https://gist.github.com/wolfgangmeyers/49d8edaa763f5fcdcdf7
https://gist.githubusercontent.com/wolfgangmeyers/49d8edaa763f5fcdcdf7/raw/4a2b17e9150c1ac25c5573f11bb3c6ffd2230a4e/setup_eclipse.py

https://stackoverflow.com/questions/54175366/bazel-run-passing-main-arguments
https://stackoverflow.com/questions/13324722/debugging-eclipse-plug-ins



## Maven Dependencies
* mvn dependency:list -Dsort=true -DexcludeTransitive=true > deps.txt
* mvn dependency:list -Dsort=true -DexcludeTransitive=true > deps.txt
* generate new list of dependencies: ``bazel run @maven//:pin``


## relevant things to figure out
https://sumglobal.com/2020/03/10/bazel-and-micronaut/
https://github.com/ckilian867/bazel-lombok-generated-annotation/blob/main/example/BUILD.bazel

https://github.com/bazelbuild/rules_jvm_external/blob/master/docs/bzlmod.md
https://github.com/wix/greyhound/tree/master/future-interop
https://www.harness.io/blog/migrating-bazel-build-tool
https://github.com/bazelbuild/examples/tree/main/java-maven


## mix materials with outdated content
https://github.com/GerritCodeReview/bazlets/blob/master/tools/eclipse/BUILD
https://groups.google.com/g/bazel-discuss/c/w3JVpcbv6Y0/m/kY2rdEeVBgAJ

https://jeeconf.com/program/the-wait-is-over-how-to-successfully-migrate-to-bazel-from-maven-or-gradle/
https://github.com/wix-incubator/exodus
https://docs.google.com/presentation/d/1URc6JzE71GWAek2ym9hiOoF1cAg-Jn7-GSD4jRpaY8w/htmlpresent



## notes from experiments
https://github.com/plaird/awesome-bazel
git clone git@github.com:bazelbuild/buildtools.git
cd buildtools
git checkout 6.1.0
bazel build //buildifier






[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

# The Stencil CMS 
     
# Local Development Environment

## Prerequisites Java 11+, Maven 3.6.2+
It's recommended to install them using [SDKMAN](https://sdkman.io/install)
```
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
sdk install java 11.0.7-zulu
sdk install maven
```

## Creating and Running Maven Project
```
mvn archetype:generate                                  \
  -DarchetypeGroupId=io.github.jocelynmutso             \
 // -DarchetypeArtifactId=zoe-maven-archetype           \
 // -DgroupId=io.placeholder.test                       \
  -DartifactId=test-project

cd test-project

mvn clean compile quarkus:dev

```

---

## Creating Reference Implementation

`core/dev-tools/demo-app`   
`core/dev-tools/demo-portal-ui`  

TODO

### STENCIL BACKEND: 

TODO

Copy `/the-stencil-parent/dev-tools/demo-app`

Create/configure local (postgre) database by following the steps in [README_PG.MD](README_PG.MD) 

Configure `/dev-tools/demo-app/src/resources/application.yaml`

---

### STENCIL PORTAL:

Copy the following from the-stencil-portal project:

* `src/core/app/primary`
* `src/core/app/secondary`
* `src/core/app/toolbar`

Add correct Portal import in files where needed:

`import Portal from '@the-stencil-io/portal';`

---

Copy PortalApp.tsx from the-stencil-portal  `src/core/app/PortalApp.tsx`  and rename PortalApp to desired name

```ts

const PortalApp: React.FC<{}> = (props) => {
...code...
}

```
---

Copy util.ts from `src/core/app/util.ts` 

---

Create `/public` and put logo there

---
Create custom theme folder + theme, then add it in `index.tsx`
```
import { myTheme } from './theme/myTheme';


    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={myTheme}>
        <RefApp />
      </ThemeProvider>
    </StyledEngineProvider>
```   
---

## Running Reference Implementation Locally

TODO

In Terminal:

 Navigate to `the-stencil-parent/`  

* Start database if it's not already running:
  * `docker-compose -f dev-tools/demo-app/src/main/resources/docker/stack-pg.yml up -d`
* run: `mvn clean install`

* Navigate to `the-stencil-parent/dev-tools/demo-app/`  
  * run: `mvn compile quarkus:dev`  
  
* Navigate to `the-stencil-parent/dev-tools/demo-portal-ui/`  
  * run: `yarn start`

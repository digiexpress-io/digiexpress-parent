# The Stencil Composer  v1.0

## Description

Multi-language content editor for client service portals connecting text-based content, links, workflows/integrations, and release management.

For end-user documentation, [see the wiki](https://github.com/the-stencil-io/the-stencil-composer/wiki)

the-stencil-composer contains built-in mock data and can be run independently of the backend

## Requirements

TODO 

## Local testing

To run the Stencil locally, with backend and local database, run these commands

/the-stencil-parent  
`mvn clean install`

/the-stencil-parent/dev-tools/demo-app  
`mvn compile quarkus:dev`

/the-stencil-composer   
`yarn start`

---

`yarn start` has two implementations:

**1. Run stencil-composer with mock data and no backend**  

In `src/index.tsx` 

```
   const service = StencilClient.mock();
// const service = StencilClient.service({ config: { url: "http://localhost:8080/q/ide-services" }});

```

**2. Run stencil-composer with backend** 

* /the-stencil-parent/dev-tools/demo-app  
`mvn compile quarkus:dev`

* /the-stencil-composer/src/index.tsx  
Comment out StencilClient.mock();  

```
// const service = StencilClient.mock();
   const service = StencilClient.service({ config: { url: "http://localhost:8080/q/ide-services" }});

```

* /the-stencil-composer    
 `yarn start`

# the-stencil-portal

The Stencil Portal is a layout component that comprises three core elements:  

* **Primary**: Section where the main content is rendered (CENTER).  
* **Secondary**: Section where the topic menu is rendered (LEFT SIDE).  
* **Toolbar:** Section that contains the components involved in rendering AppHeader (TOP).  

---

## Responsive design: breakpoints and media queries

The default configuration supports two breakpoints:

| breakpoint        | media query                                  |
|:----------------: |:------------------------------------------:  |
| sm, small: 600px  | `useMediaQuery(theme.breakpoints.down("sm")` |
| md, medium: 900px | `useMediaQuery(theme.breakpoints.down("md")` |


The 'lg' breakpoint will thus be any screen above 900px

Breakpoints are based on Material UI. [https://mui.com/customization/breakpoints/](https://mui.com/customization/breakpoints/)

---

## Local development

install dependencies

`yarn install`

run application

`yarn start`

publish to npm

`yarn publish`

OR 

pushing to branch main triggers a build/release via github workflows

---

### Prerequisites

`node v16.13.1`  
`yarn 1.22.17`  

### Where to start

#### /src/core/app

PortalApp.tsx

#### /src/core/context

AppAPI.ts
AppContext.tsx





/** 
Deps that are actually mapped via dependants...
import * from "@dxs-ts/gamut-api"
import * from "@dxs-ts/gamut-theme"
import * from "@dxs-ts/gamut-form"
import * from "@dxs-ts/gamut-md"
import * from "@dxs-ts/gamut-shell"
import * from "@dxs-ts/gamut-form-review"
import * from "@dxs-ts/gamut-intl"

import * from "@dxs-ts/envir-fetch"
import * from "@dxs-ts/envir-util"

import * from "@dxs-ts/wrench-api"
import * from "@dxs-ts/wrench-routes"
import * from "@dxs-ts/stencil-api"
import * from "@dxs-ts/stencil-routes"
*/

import React from 'react'
import ReactDOM from 'react-dom/client';
import { FrontdeskApp } from './frontdesk-app'

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);

root.render(<React.StrictMode><FrontdeskApp /></React.StrictMode>);
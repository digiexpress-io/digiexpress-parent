
import React from 'react'
import ReactDOM from "react-dom/client";
import { StencilApp } from './stencil-app'
import { WrenchApp } from './wrench-app'
import { FrontdeskApp } from './frontdesk-app'
import { UiDevApp } from './ui-dev-app';

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);

const CreateApp: React.FC = () => {

  if(process.env.REACT_APP_START_MODE === 'stencil') {
    return <StencilApp />
  } else if(process.env.REACT_APP_START_MODE === 'wrench') {
    return <WrenchApp />
  } else if (process.env.REACT_APP_START_MODE === 'frontdesk') {
    return <FrontdeskApp />
  } else if (process.env.REACT_APP_START_MODE === 'uiDevelopment') {
    return <UiDevApp />
  }

  return <>unknown app</>
}

root.render(
  <React.StrictMode>
    <CreateApp />
  </React.StrictMode>,
);

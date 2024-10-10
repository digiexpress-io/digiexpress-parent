
import React from 'react'
import ReactDOM from 'react-dom'
import { StencilApp } from './stencil-app'
import { WrenchApp } from './wrench-app'


const CreateApp: React.FC = () => {
  if(process.env.REACT_APP_START_MODE === 'stencil') {
    return <StencilApp />
  } else if(process.env.REACT_APP_START_MODE === 'wrench') {
    return <WrenchApp />
  }

  return <>unknown app</>
}

ReactDOM.render(
  <React.StrictMode>
    <CreateApp />
  </React.StrictMode>,
  document.getElementById('root')
);

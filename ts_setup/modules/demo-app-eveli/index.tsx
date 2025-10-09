import React from 'react'
import ReactDOM from 'react-dom/client';
import { FrontdeskApp } from './frontdesk-app'

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);

root.render(<React.StrictMode><FrontdeskApp /></React.StrictMode>);
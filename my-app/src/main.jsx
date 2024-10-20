import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import {MsalProvider} from "@azure/msal-react";
import {PublicClientApplication} from "@azure/msal-browser";
import {msalConfig} from "./authConfig.js";
const msalInstance = new PublicClientApplication(msalConfig);
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <MsalProvider instance={msalInstance}>
    <App />
    </MsalProvider>
  </StrictMode>,
)

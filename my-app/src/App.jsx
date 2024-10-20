import React from "react";
import {PublicClientApplication} from "@azure/msal-browser";
import {msalConfig} from "./authConfig.js";

import {PageLayout} from './components/PageLayout';

import {AuthenticatedTemplate, UnauthenticatedTemplate} from '@azure/msal-react';

import './App.css';
import ProfileContent from "./components/ProfileContent.jsx";


const MainContent = () => {
  return (
    <div className="App">
      <AuthenticatedTemplate>
        <ProfileContent />
      </AuthenticatedTemplate>

      <UnauthenticatedTemplate>
        <h5>
          <center>
            Please sign-in to see your profile information.
          </center>
        </h5>
      </UnauthenticatedTemplate>
    </div>
  );
};

export default function App() {
  return (
    <PageLayout>
      <center>
        <MainContent />
      </center>
    </PageLayout>
  );
}

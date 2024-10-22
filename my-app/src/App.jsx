import React, {useState} from "react";
import {PublicClientApplication} from "@azure/msal-browser";
import {loginRequest, msalConfig} from "./authConfig.js";

import {PageLayout} from './components/PageLayout';

import {AuthenticatedTemplate, UnauthenticatedTemplate, useMsal} from '@azure/msal-react';

import './App.css';
import ProfileContent from "./components/ProfileContent.jsx";
import {protectedData} from "./graph.js";


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
  const { instance } = useMsal();
  function generateRandomString(length) {
    let text = "";
    let possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (let i = 0; i < length; i++) {
      text += possible.charAt(Math.floor(Math.random() * possible.length));
    }

    return text;
  }

  function sha256(plain) {
    const encoder = new TextEncoder();
    const data = encoder.encode(plain);
    return crypto.subtle.digest('SHA-256', data);
  }

  function base64URLEncode(arrayBuffer) {
    return btoa(String.fromCharCode(...new Uint8Array(arrayBuffer)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
  }

  async function pkceChallengeFromVerifier(verifier) {
    const hashed = await sha256(verifier);
    return base64URLEncode(hashed);
  }
  const handleLogin = () => {
    instance.loginPopup(loginRequest)
      .then(response => {
        console.log(response);
        console.log('Access Token:', response.accessToken);
      })
      .catch(err => console.log(err));
  };
  return (
    <PageLayout>
      <button onClick={()=>protectedData("aa4cd622d128a30fa4f4c0259d503f82230e5200b9a8d1f8d357cb66ed037145")}></button>
      <center>
        <MainContent />
      </center>
    </PageLayout>
  );
}

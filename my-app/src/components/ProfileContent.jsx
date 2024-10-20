import {ProfileData} from "./ProfileData.jsx";
import Button from "react-bootstrap/Button";
import {useMsal} from "@azure/msal-react";
import {useState} from "react";
import {loginRequest} from "../authConfig.js";
import {callMsGraph, protectedData, userData} from "../graph.js";
/**
 * Renders information about the signed-in user or a button to retrieve data about the user
 */
const ProfileContent = () => {
  const { instance, accounts } = useMsal();
  const [graphData, setGraphData] = useState(null);

  function RequestProfileData() {
    // Silently acquires an access token which is then attached to a request for MS Graph data
    instance
      .acquireTokenSilent({
        ...loginRequest,
        account: accounts[0],
      })
      .then((response) => {
        console.log(response);
        callMsGraph(response.accessToken).then((response) => setGraphData(response));
      });
  }

  function RequestUserDataById() {
    // Silently acquires an access token which is then attached to a request for MS Graph data
    instance
      .acquireTokenSilent({
        ...loginRequest,
        account: accounts[0],
      })
      .then((response) => {
        console.log(response);
        userData(response.idToken).then((response) => console.log(response));
      });
  }

  function RequestUserDataByAccess() {
    // Silently acquires an access token which is then attached to a request for MS Graph data
    instance
      .acquireTokenSilent({
        ...loginRequest,
        account: accounts[0],
      })
      .then((response) => {
        console.log(response);
        protectedData(response.accessToken).then((response) => console.log(response));
      });
  }
  console.log(graphData);
  return (
    <>
      <h5 className="card-title">Welcome {accounts[0].name}</h5>
      <br/>
      {graphData ? (
        <ProfileData graphData={graphData} />
      ) : (
        <>
        <Button variant="secondary" onClick={RequestProfileData}>
          Request Profile Information
        </Button>

        <Button variant="secondary" onClick={RequestUserDataById}>
    Request Profile by IdToken
    </Button>

          <Button variant="secondary" onClick={RequestUserDataByAccess}>
            Request Profile by accessToken
          </Button>
        </>

      )}
    </>
  );
};

export default ProfileContent
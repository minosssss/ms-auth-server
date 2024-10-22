import {apiServer, graphConfig} from "./authConfig";

/**
 * Attaches a given access token to a MS Graph API call. Returns information about the user
 * @param accessToken
 */
export async function callMsGraph(accessToken) {
  const headers = new Headers();
  const bearer = `Bearer ${accessToken}`;

  headers.append("Authorization", bearer);

  const options = {
    method: "GET",
    headers: headers
  };

  return fetch(graphConfig.graphMeEndpoint, options)
    .then(response => response.json())
    .catch(error => console.log(error));
}

export async function userData(accessToken) {
  const headers = new Headers();
  const bearer = `Bearer ${accessToken}`;

  headers.append("Authorization", bearer);

  const options = {
    method: "GET",
    headers: headers
  };

  return fetch(apiServer.graphMeEndpoint, options)
    .then(response => response.json())
    .catch(error => console.log(error));
}

export async function protectedData(accessToken) {
  const headers = new Headers();
  // const bearer = `Bearer eyJraWQiOiJSRUZNOUl6M2dDSmE2M0V0amRJNXFDQmIwODQwdS1DbkdhTkpxWGFpOG5FIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwOi8vMTkyLjE2OC4xOS4yNTQ6MTAwMDAiLCJzdWIiOiIxIiwiYXVkIjoiOGNmNTgyYTBhMDNhZGZjNjNmMTdkMzMzYjZlMTE1Mzg0NTBmZWEwOGZhMmM1ZTEzYzg0MjYxNGU0NTkyOWU4ZiIsImV4cCI6MTcyOTQ5MzM3MSwiaWF0IjoxNzI5NDkzMjUxLCJhdXRoX3RpbWUiOjE3Mjk0ODc5ODQsInN1Yl9sZWdhY3kiOiJjYjRiY2JhMWYwMzMxZDY5OWM0MmExODQwMzViNTY2OTA1NjRjNWQzMzRmODY2MTc0NjY1MmM1ZWE3Y2VhMTM1IiwibmFtZSI6IkFkbWluaXN0cmF0b3IiLCJuaWNrbmFtZSI6InJvb3QiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJyb290IiwiZW1haWwiOiJyb290QGJyb2FkY25zLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJwcm9maWxlIjoiaHR0cDovLzE5Mi4xNjguMTkuMjU0OjEwMDAwL3Jvb3QiLCJwaWN0dXJlIjoiaHR0cHM6Ly93d3cuZ3JhdmF0YXIuY29tL2F2YXRhci9hNmE1YzE5Mzg1OWE2ODRhMTZhMDNhYTgwZGEwNjA3OT9zPTgwJmQ9aWRlbnRpY29uIiwiZ3JvdXBzX2RpcmVjdCI6WyJhaS1zZXJ2aWNlIiwiYWktc2VydmljZS9ib25hLXN0dCIsImFpLXNlcnZpY2UvcmVtb3RlLW1lZGljYWwiLCJhaS1zZXJ2aWNlL2NhbGxib3QiLCJ2b2ljZS1haSIsInJuZCIsInJuZC9zdHQiXX0.jFBKVtLtDyvrOf0EibtMseDJN7iRtoPDFCiA79U8NAN_PF8-hNmmeNDram3qLBr7FO1y8hJjs5CbPH1QJhKcXABi6zSZT3KoA8VOSduoxjhUFOGPau3CF0qgTyr5Vsyg5AIGwi403V3rJ66x2_mBmpfnE35nW73ivcCtNzWqzoyxbykzAGaaZ05FqrxKExq7aFm5shZd216zpAb8_JCS0nGcpU_AUCdO4GrUO__O_LzsQlBN8Zg0MGBRNIGTXEzO0w9CCD2YWXiUzyMqe9qqUIvGbGvQqqlwhqHsz-q8xCdW9IAEZQE1FRK9hcq-WBvUoPrG3AJpGMEEIylrPeYn_AZifkA4gaKWDKZcCk0EHimedDGjzO-fhOnxEaJ7H3lzh9n2A9LavoqjQ8pfC6WD7KLbLCYcHqcaR4I10t521OhE4Nc_zFLVWZ-7cxRV3CpkHrsEEtwalXRwpwkClu1BfZoAj2xGG2TBRhOIddTGhlvJENo_ZK3r5i2UjbLn5eC3K5AkPZ_tycXgV4CD2skrnJi-3QEJUcCubIzzQE3zOKZKzP5A14gpFCxK9pvn_PSxiB3rnf_PVvdWfVZHF6pe0zoHvXzHMBjANO01AN5Y3zyHfCNbIdOzkns2Zne2MDigCJmxzxcGA2ESGP6o1E6luI9A5eEMNef9T94hKdgSZm0`;
  const bearer = `Bearer 170b44b0fb2415a444f5b3b4b023167e4b4d73829d16851469fb42ab3c05eeb299`;

  headers.append("Authorization", bearer);

  const options = {
    method: "GET",
    headers: headers
  };

  return fetch(apiServer.graphMeEndpoint, options)
    .then(response => response.json())
    .catch(error => console.log(error));
}


// Encode client_id and client_secret for Basic Auth
const clientId = '8cf582a0a03adfc63f17d333b6e11538450fea08fa2c5e13c842614e45929e8f';
const clientSecret = 'gloas-d35800a58eba7ac2cd6908939cb6ccc7b88aa69b4111aa4fb415dbafb32294bd';
const authString = `${clientId}:${clientSecret}`;
const base64Auth = btoa(authString);

const tokenUrl = 'http://192.168.19.254:10000/oauth/token';

const body = new URLSearchParams({
  code: 'b25598f9c38624172c10fa4c68feca1006228d6d1b6e21b353a06c10ff947b08',           // Replace with the actual authorization code
  grant_type: 'authorization_code',     // OAuth grant type
  redirect_uri: 'http://localhost:5173', // Ensure this matches exactly with the one used during authorization
  // If you're using PKCE, include the code_verifier:
  // code_verifier: 'YOUR_CODE_VERIFIER'
});

// Make the fetch request
fetch(tokenUrl, {
  method: 'POST',
  headers: {
    'Authorization': `Basic ${base64Auth}`,  // Basic authentication header
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  body: body.toString(),  // Send the body as URL-encoded string
})
  .then(response => response.json())
  .then(data => {
    console.log('Token Response:', data);
  })
  .catch(error => {
    console.error('Error fetching token:', error);
  });

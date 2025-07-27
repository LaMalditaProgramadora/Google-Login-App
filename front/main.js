// Variables para construir la URL
const googleAuthBaseURL = "https://accounts.google.com/o/oauth2/v2/auth";
const redirectURI = encodeURIComponent("http://localhost:8080/grant-code");
const responseType = "code";
const clientID = "";
const scope = encodeURIComponent("https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile openid");
const accessType = "offline";

// Armar la URL de autenticación
const googleLoginURL = `${googleAuthBaseURL}?redirect_uri=${redirectURI}&response_type=${responseType}&client_id=${clientID}&scope=${scope}&access_type=${accessType}`;

// Asignar el enlace al botón
document.getElementById("googleLoginBtn").addEventListener("click", () => {
  window.location.href = googleLoginURL;
});
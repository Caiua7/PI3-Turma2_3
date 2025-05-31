let timerInterval;
let loginFinalizado = false; 

function callPerformAuth() {
  if (loginFinalizado) return; 

  fetch("http://localhost:3000/api/perform-auth", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ site: "www.cursini.com.br" })
  })
  .then(response => response.json())
  .then(data => {
    const base64 = data.qrcodeBase64;
    const loginToken = data.loginToken;

    document.getElementById("qrCodeImg").src = base64;

    const timerElement = document.getElementById('timer');
    let timeLeft = 60;
    timerElement.textContent = `Expira em: ${timeLeft} segundos`;

    clearInterval(timerInterval);
    timerInterval = setInterval(() => {
      if (loginFinalizado) {
        clearInterval(timerInterval);
        return;
      }

      timeLeft--;
      if (timeLeft <= 0) {
        clearInterval(timerInterval);
        document.getElementById("qrCodeImg").src = "";
        callPerformAuth(); 
      } else {
        timerElement.textContent = `Expira em: ${timeLeft} segundos`;
      }
    }, 1000);

    [15, 35, 60].forEach(delayInSeconds => {
      setTimeout(() => {
        if (loginFinalizado) return;

        fetch("http://localhost:3000/api/get-login-status", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "login-token": loginToken
          }
        })
        .then(res => res.json())
        .then(statusData => {
          console.log(` Status aos ${delayInSeconds}s:`, statusData);

          if (statusData.uid) {
            loginFinalizado = true; 
            clearInterval(timerInterval);
            window.location.href = "../home/HomePage.html";
          }
        })
        .catch(err => console.error("Erro ao verificar login status:", err));
      }, delayInSeconds * 1000);
    });

  })
  .catch(err => {
    console.error("Erro:", err.message);
  });
}

window.onload = callPerformAuth;

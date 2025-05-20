import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as qr from "qrcode";
import * as crypto from "crypto";

// inicializa o Firebase Admin
admin.initializeApp();

// função performAuth
export const performAuth = functions.https.
  onRequest({region: "southamerica-east1"}, async (request, response) => {
    try {
      const apiKey=request.headers["apikey"]as string;
      const url=request.headers["url"]as string;

      if (!apiKey || !url) {
        response.status(400).json({error: "Headers apiKey ou url ausentes"});
        return;
      }

      // verifica apikey na coleção partners
      const partnersSnapshot = await admin.firestore()
        .collection("partners")
        .where("apiKey", "==", apiKey)
        .get();

      if (partnersSnapshot.empty) {
        response.status(400).json({error: "API Key inválida"});
        return;
      }

      // geração loginToken
      const loginToken = crypto.randomBytes(128).toString("hex");

      // geração qrcode
      const qrCodeBase64 = await qr.toDataURL(loginToken);

      // criar documento de login
      await admin.firestore().collection("login").add({
        apiKey,
        dataHora: admin.firestore.FieldValue.serverTimestamp(),
        loginToken,
      });

      // retorna loginToken e qrcode
      response.status(200).json({
        qrcodeBase64: qrCodeBase64,
      });
    } catch (error) {
      response.status(500).json({error: "Erro interno do servidor"});
    }
  });


// contador de tentativas (máx. 3)
const loginAttempts = new Map<string, number>();
// função getLoginStatus
export const getLoginStatus = functions.https
  .onRequest({region: "southamerica-east1"}, async (request, response) => {
    try {
      const loginToken = request.headers["logintoken"] as string;

      if (!loginToken) {
        response.status(400).json({error: "Header loginToken ausente"});
        return;
      }

      // buscar documento pelo loginToken
      const loginDocSnapshot = await admin.firestore()
        .collection("login")
        .where("loginToken", "==", loginToken)
        .get();

      if (loginDocSnapshot.empty) {
        response.status(400).json({error: "Token inválido ou expirado"});
        return;
      }

      const loginDoc = loginDocSnapshot.docs[0];
      const loginData = loginDoc.data();

      // verifica se o token já está expirado (depois de 1 minuto)
      const tempo = admin.firestore.Timestamp.now();
      const tokenTimestamp = loginData.dataHora;
      const expirationTime = tokenTimestamp.toMillis() + 60000;

      if (tempo.toMillis() > expirationTime) {
        loginAttempts.delete(loginToken);
        await loginDoc.ref.delete();
        response.status(400).
          json({error: "Token expirado, gere um novo QR Code"});
        return;
      }

      // verifica o número de tentativas
      const attempts = (loginAttempts.get(loginToken) || 0) + 1;
      loginAttempts.set(loginToken, attempts);

      if (attempts > 3) {
        loginAttempts.delete(loginToken);
        await loginDoc.ref.delete();
        response.status(400).
          json({error: "Máximo de tentativas excedido, gere um novo QR Code"});
        return;
      }


      // retornar o usuário autenticado
      const user = loginData.user || "Usuário desconhecido";
      response.status(200).json({status: "Autenticado", user});
    } catch (error) {
      response.status(500).json({error: "Erro interno do servidor"});
    }
  });
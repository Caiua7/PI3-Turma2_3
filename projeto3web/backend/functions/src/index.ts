import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as qr from "qrcode";
import * as crypto from "crypto";
import cors from "cors";

// Inicializa o Firebase Admin
admin.initializeApp();

// Configura o CORS
const corsHandler = cors({origin: true});

// Função performAuth
export const performAuth = functions.https
  .onRequest({region: "southamerica-east1"}, (request, response) => {
    corsHandler(request, response, async () => {
      try {
        const apiKey = request.headers["apikey"] as string;
        const url = request.headers["url"] as string;

        if (!apiKey || !url) {
          response.status(400).json({error: "Headers apiKey ou url ausentes"});
          return;
        }

        const partnersSnapshot = await admin.firestore()
          .collection("partners")
          .where("apiKey", "==", apiKey)
          .get();

        if (partnersSnapshot.empty) {
          response.status(400).json({error: "API Key inválida"});
          return;
        }

        const loginToken = crypto.randomBytes(128).toString("hex");
        const qrCodeBase64 = await qr.toDataURL(loginToken);

        await admin.firestore().collection("login").add({
          apiKey,
          dataHora: admin.firestore.FieldValue.serverTimestamp(),
          loginToken,
        });

        response.status(200).json({qrcodeBase64: qrCodeBase64});
      } catch (error) {
        response.status(500).json({error: "Erro interno do servidor"});
      }
    });
  });

// Contador de tentativas (máx. 3)
const loginAttempts = new Map<string, number>();

// Função getLoginStatus
export const getLoginStatus = functions.https
  .onRequest({region: "southamerica-east1"}, (request, response) => {
    corsHandler(request, response, async () => {
      try {
        const loginToken = request.headers["logintoken"] as string;

        if (!loginToken) {
          response.status(400).json({error: "Header loginToken ausente"});
          return;
        }

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

        const attempts = (loginAttempts.get(loginToken) || 0) + 1;
        loginAttempts.set(loginToken, attempts);

        if (attempts > 3) {
          loginAttempts.delete(loginToken);
          await loginDoc.ref.delete();
          response.status(400).
            json({
              error: "Máximo de tentativas excedido, gere um novo QR Code"});
          return;
        }

        const user = loginData.user || "Usuário desconhecido";
        response.status(200).json({status: "Autenticado", user});
      } catch (error) {
        response.status(500).json({error: "Erro interno do servidor"});
      }
    });
  });

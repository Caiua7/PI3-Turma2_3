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
        tentativas: 0,
        url: "www.cursini.com.br",
        status: "aguardando autenticação",
      });

      // retorna loginToken e qrcode
      response.status(200).json({
        qrcodeBase64: qrCodeBase64,
        loginToken: loginToken,
      });
    } catch (error) {
      response.status(500).json({error: "Erro interno do servidor"});
    }
  });


export const getLoginStatus = functions.https.onRequest(
  {region: "southamerica-east1"},
  async (request, response) => {
    try {
      const loginToken = request.headers["login-token"] as string;

      if (!loginToken) {
        response.status(400).json({error: "Header loginToken ausente"});
        return;
      }

      // Busca o documento com esse loginToken
      const snapshot = await admin.firestore()
        .collection("login")
        .where("loginToken", "==", loginToken)
        .limit(1)
        .get();

      if (snapshot.empty) {
        response.status(400).json({error: "Token inválido ou expirado"});
        return;
      }

      const doc = snapshot.docs[0];
      const data = doc.data();

      // Verifica se passou mais de 1 minuto desde a criação
      const createdAt = data.dataHora?.toMillis?.();
      const now = Date.now();

      if (!createdAt || now > createdAt + 60000) {
        await doc.ref.delete();
        response.status(400).
          json({error: "Token expirado, gere um novo QR Code"});
        return;
      }

      // Verifica tentativas
      const tentativas = data.tentativas ?? 0;
      if (tentativas >= 3) {
        await doc.ref.delete();
        response.status(400).
          json({error: "Máximo de tentativas excedido, gere um novo QR Code"});
        return;
      }

      const updates: any = {tentativas: tentativas + 1};

      if (data.uid) {
        updates.status = "Autenticado";
      }

      await doc.ref.update(updates);

      response.status(200).json({
        status: data.uid ? "Autenticado" : "Aguardando autenticação",
        uid: data.uid ?? null,
      });
    } catch (error) {
      console.error("Erro em getLoginStatus:", error);
      response.status(500).json({error: "Erro interno do servidor"});
    }
  }
);

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
    // parâmetros no header
      const apiKey=request.headers["apikey"]as string;
      const url=request.headers["url"]as string;

      if (!apiKey || !url) {
        console.error("Parâmetros ausentes:", {apiKey, url});
        response.status(400).json({error: "Headers apiKey ou url ausentes"});
        return;
      }

      console.log("Recebido:", {apiKey, url});

      // verifica apikey na coleção partners
      const partnersSnapshot = await admin.firestore()
        .collection("partners")
        .where("apiKey", "==", apiKey)
        .get();

      if (partnersSnapshot.empty) {
        console.error("API Key inválida:", apiKey);
        response.status(400).json({error: "API Key inválida"});
        return;
      }

      const partnerDoc = partnersSnapshot.docs[0].data();
      console.log("Parceiro encontrado:", partnerDoc);


      // geração loginToken
      const loginToken = crypto.randomBytes(128).toString("hex");
      console.log("Token gerado:", loginToken);

      // geração qrcode
      const qrCodeBase64 = await qr.toDataURL(loginToken);
      console.log("QR Code gerado com sucesso");

      // criar documento de login
      await admin.firestore().collection("login").add({
        apiKey,
        dataHora: admin.firestore.FieldValue.serverTimestamp(),
        loginToken,
      });

      // retorta loginToken e qrcode
      response.status(200).json({
        qrcodeBase64: qrCodeBase64,
      });
    } catch (error) {
      console.error("Erro na função performAuth:", error);
      response.status(500).json({error: "Erro interno do servidor"});
    }
  });

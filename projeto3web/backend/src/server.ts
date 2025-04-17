import express, { Request, Response, Router } from 'express';
import cors from "cors";

const port = 3000; 
const routes = Router();
var app = express();
app.use(cors());

app.use(routes);
routes.get('/', (req: Request, res: Response)=>{
    res.statusCode = 403;
    res.send('Acesso não permitido. Rota default não definida.');
});

app.listen(port,()=>{
    console.log("Servidor rodando na porta 3000");
});
/*
    Todo backend precisa de um servidor para
    disponibilizar seus serviços. Todo backend é
    um conjunto de serviços funcionais que rodam em um
    servidor WEB. Portanto preciso inicializar um 
    servidor web (este é o primeiro passo).

    Protocolo é um conjunto de regras e ferramentas. Para
    a Internet funcionar existem 3 protocolos cruciais: TCP, IP e HTTP
    
    IP = Internet Protocol = Cada computador tem um endereço chamado
    numero IP em uma rede. 
    
    TCP = Transport Communication Protocol = É a "estrada" dos bytes
    entre um computador e outro. É o protocolo responsável por transportar
    pacotes entre de um IP a outro IP. 

    HTTP = É o protocolo que permite um "formato" de troca de dados entre
    os computadores que tem um IP na rede. Este formato significa:
    Que um computador que quer mandar um dado para outro, precisa obedecer
    algumas regras de formato, não só dos dados, mas sequência de operações.

    Cenário: 
    Imagine dois computadores ligados por um único cabo de rede azul.
    O computador A, tem o seguinte endereço IP: 192.168.0.1
    Já o computador B o IP seja 192.168.0.2 

    Imagine ainda, que você transforme a máquina A (poderia ser a B) em 
    um servidor http/web. O que isso significa? 
    Significa: 
    - Que a máquina A, vai propagar para a rede dela que está com uma
    PORTA de conexão TCP aberta. (geralmente backends em node, usam porta
    número 3000 - não é obrigatório).
    - Que as outras máquinas da rede, por exemplo no nosso caso, somente a
    B, pode REQUISITAR serviços - isso significa que a máquina B é uma
    CLIENTE dos serviços da máquina A, entendemos então que o papel da 
    máquina A é o de servir / servidor / prover serviços.
    - REQUISItAR que o servidor faça alguma coisa, é uma REQUISIÇÃO 
    (ou REQUEST) que sai de algum cliente e chega no servidor. 
    - Ao reqlizar a operação, o servidor devolve ou não uma RESPOSTA.

    Dizemos que o protocolo HTTP é baseado em 
    REQ (Requests) e RES (Responses)

    - Cada serviço funcional do backend tem um nome, portanto, 
    existe uma rota para chegar nele. Exemplo:

    http://192.168.0.1:3000/calcularRaizDeUmNumero

    O serviço chama calcularRaizDeUmNumero
    e rota completa é composta de:

    http://<nome ou ip do servidor>:<porta>/<serviço>

    Quando queremos testar um servidor que está na nossa própria máquina
    podemos digitar (WINDOWS)
    http://localhost:3000/calcularRaizDeUmNumero

*/

// importando o módulo http do node.
import http from "http"

// parece estranho, mas a constante server
// representa o servidor web de backend que foi criado.
const server = http.createServer((req,res)=>{
    res.writeHead(200, {"Content-Type":"text/plain"});
    res.end("Oi! Servidor do Mateus...");
});

// iniciando o servidor na porta 3000
server.listen(3000,()=>{
    console.log("Servidor rodando na porta 3000");
});
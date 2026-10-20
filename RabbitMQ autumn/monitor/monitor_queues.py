#!/usr/bin/env python3
# Solo stdlib, requiere Management API en localhost:15672.
import argparse, base64, json, os, time, urllib.request, urllib.error
parser=argparse.ArgumentParser(description="Monitor pedagógico de colas y DLQ de Pedidos360")
parser.add_argument('--intervalo',type=int,default=15,help='Segundos entre consultas')
parser.add_argument('--una-vez',action='store_true',help='Ejecutar una sola consulta')
parser.add_argument('--umbral-ready',type=int,default=10,help='Alerta si Ready alcanza este valor')
args=parser.parse_args()
url=os.getenv('RABBIT_API_URL','http://localhost:15672/api/queues/%2F')
user=os.getenv('RABBIT_API_USER','labuser')
password=os.getenv('RABBIT_API_PASSWORD','labpass')
token=base64.b64encode(f'{user}:{password}'.encode()).decode()
def mirar():
    request=urllib.request.Request(url,headers={'Authorization':f'Basic {token}'})
    try:
        with urllib.request.urlopen(request,timeout=8) as r: filas=json.load(r)
    except (urllib.error.URLError,ValueError) as exc:
        print('ERROR consultando API:',exc);return
    print('--- RabbitMQ Management API ---')
    for q in sorted(filas,key=lambda x:x.get('name','')):
        name=q.get('name','')
        if not name.startswith('pedidos360.'):continue
        ready=int(q.get('messages_ready',0) or 0)
        unacked=int(q.get('messages_unacknowledged',0) or 0)
        consumers=int(q.get('consumers',0) or 0)
        print(f'{name:43} Ready={ready:<4} Unacked={unacked:<4} Consumidores={consumers}')
        if name.endswith('.dlq') and ready>0:print('  ALERTA: DLQ contiene mensajes fallidos.')
        if ready>=args.umbral_ready:print('  ALERTA: cola con acumulación de Ready.')
        if consumers==0 and name.endswith('.queue') and ready>0:print('  ALERTA: cola sin consumidores.')
if args.una_vez: mirar()
else:
    try:
        while True:mirar();time.sleep(max(1,args.intervalo))
    except KeyboardInterrupt:print('Monitor detenido.')

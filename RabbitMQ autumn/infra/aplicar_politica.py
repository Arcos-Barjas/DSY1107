#!/usr/bin/env python3
# Método independiente del escape de comillas de PowerShell: Management HTTP API.
import os,json,base64,urllib.request
url=os.getenv('RABBIT_API_URL_POLICY','http://localhost:15672/api/policies/%2F/pedidos360-principales')
user=os.getenv('RABBIT_API_USER','labuser'); pw=os.getenv('RABBIT_API_PASSWORD','labpass')
data={"pattern":r"^pedidos360\.(notificaciones|cocina|documentos)\.queue$",
      "definition":{"message-ttl":120000,"max-length":200},
      "priority":0,"apply-to":"queues"}
credentials=base64.b64encode(f'{user}:{pw}'.encode()).decode()
req=urllib.request.Request(url,data=json.dumps(data).encode('utf-8'),method='PUT',
    headers={'Content-Type':'application/json','Authorization':f'Basic {credentials}'})
with urllib.request.urlopen(req,timeout=10) as r:
    print('Política aplicada mediante API. HTTP',r.status)
print('Se aplica SOLO a las colas principales; NO a .retry ni a .dlq.')

import json
import zeep
import sys

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    wsdl = 'https://appweb.dane.gov.co/sipsaWS/SrvSipsaUpraBeanService?WSDL'
    try:
        client = zeep.Client(wsdl=wsdl)
        # El metodo promediosSipsaParcial nos da los precios recientes (diarios)
        getService = client.service.promediosSipsaParcial()
        
        response_data = []
        
        if len(getService) > 0:
            for record in getService:
                nombre = str(record['artiNombre']).strip()
                if nombre.endswith('*'):
                    nombre = nombre[:-1].strip()
                
                data = {
                    'producto': nombre,
                    'departamento': str(record['deptNombre']).strip(),
                    'ciudad': str(record['muniNombre']).strip(),
                    'mercado': str(record['fuenNombre']).strip(),
                    'precio_min': float(record['minimoKg']) if record['minimoKg'] else 0.0,
                    'precio_max': float(record['maximoKg']) if record['maximoKg'] else 0.0,
                    'precio_promedio': float(record['promedioKg']) if record['promedioKg'] else 0.0,
                    'fecha': str(record['enmaFecha']).strip()
                }
                # Solo guardamos registros que tengan precio promedio válido
                if data['precio_promedio'] > 0:
                    response_data.append(data)
        
        # Agrupamos por producto para sacar un precio promedio nacional o el mas reciente
        # Esto nos permite simplificar los datos antes de enviarlos a Java
        catalogo_nacional = {}
        for item in response_data:
            prod_name = item['producto']
            precio = item['precio_promedio']
            if prod_name not in catalogo_nacional:
                catalogo_nacional[prod_name] = []
            catalogo_nacional[prod_name].append(precio)
            
        # Promedio nacional de cada producto
        catalogo_final = []
        for prod_name, precios in catalogo_nacional.items():
            promedio_nacional = sum(precios) / len(precios)
            catalogo_final.append({
                "nombre": prod_name,
                "precio": int(promedio_nacional)
            })
            
        # Imprimimos como JSON string para que Java lo capture en el stdout
        print(json.dumps({"status": "success", "data": catalogo_final}, ensure_ascii=False))
        
    except Exception as e:
        print(json.dumps({"status": "error", "message": str(e)}, ensure_ascii=False))
        sys.exit(1)

if __name__ == '__main__':
    main()

import os
import re

html_files = [
    "dashboard_admin.html",
    "admin_usuarios.html",
    "admin_pedidos.html",
    "admin_verificaciones.html",
    "admin_usuario_form.html"
]

base_dir = r"C:\Users\Brayan\Documents\Brayan JAVA\AccesoUsuarios\src\main\resources\templates"

for file in html_files:
    file_path = os.path.join(base_dir, file)
    if not os.path.exists(file_path):
        continue
        
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Remove the constraint that makes it look like it's taking only half the screen
    content = content.replace('<main class="p-6 md:p-10 w-full max-w-7xl mx-auto">', '<main class="p-6 md:p-10 w-full">')
    content = content.replace('<div class="px-10 pb-10 max-w-7xl mx-auto w-full">', '<div class="px-10 pb-10 w-full">')

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

print("Fluid width applied.")

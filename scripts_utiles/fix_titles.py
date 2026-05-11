import os
import re

html_files = {
    "dashboard_admin.html": "Panel de Control",
    "admin_usuarios.html": "Gestión de Usuarios",
    "admin_pedidos.html": "Gestión de Pedidos",
    "admin_verificaciones.html": "Verificaciones KYC",
    "admin_usuario_form.html": "Editar Usuario"
}

base_dir = r"C:\Users\Brayan\Documents\Brayan JAVA\AccesoUsuarios\src\main\resources\templates"

for file, title in html_files.items():
    file_path = os.path.join(base_dir, file)
    if not os.path.exists(file_path):
        continue
        
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Make the header nice with breadcrumbs and the title.
    # The header is currently: <header class="h-16 bg-[#FAFAFA] border-b border-gray-100 flex items-center justify-between px-8 sticky top-0 z-10"> ... </header>
    # Let's replace the header entirely with a much more "Linear/Vercel" style header that includes the Title and a Back button (except dashboard).

    back_btn_html = ""
    if file != "dashboard_admin.html":
        back_btn_html = """<button onclick="window.history.back()" class="flex items-center gap-2 text-[13px] font-medium text-gray-500 hover:text-gray-900 transition mr-4">
                    <i class="fas fa-arrow-left"></i> Volver
                </button>
                <div class="h-4 w-px bg-gray-200 mx-2"></div>"""

    new_header = f"""<header class="h-16 bg-[#FAFAFA]/80 backdrop-blur-md border-b border-gray-200/60 flex items-center justify-between px-8 sticky top-0 z-10">
                <div class="flex items-center">
                    {back_btn_html}
                    <div class="flex items-center gap-2 text-[13px]">
                        <span class="text-gray-400">Admin</span>
                        <span class="text-gray-300">/</span>
                        <span class="font-semibold text-gray-900">{title}</span>
                    </div>
                </div>
                <div class="flex items-center gap-4">
                    <!-- Right side actions if any -->
                </div>
            </header>"""

    # We replace the current header
    content = re.sub(r"<header class=\"h-16 bg-\[#FAFAFA\].*?</header>", new_header, content, flags=re.DOTALL)
    
    # In some files, we might still have the old mobile header or something else. We'll leave it if it's the mobile one.
    
    # Remove any old <h1> inside the page since the header now has the title nicely integrated like Vercel.
    # Actually, Vercel has the title inside the page too. Let's add a nice H1 inside the <div class="p-8"> if it's missing, or format the existing one.
    
    if '<h1 class="text-2xl font-bold text-gray-900">' in content:
        # replace with ultra modern H1
        content = content.replace('<h1 class="text-2xl font-bold text-gray-900">', '<h1 class="text-2xl font-semibold tracking-tight text-gray-900 mb-6">')
    else:
        # If the file doesn't have an h1 because I deleted it, let's inject it at the top of <div class="p-8">
        if '<div class="p-8">' in content and not '<h1 ' in content:
            content = content.replace('<div class="p-8">', f'<div class="p-8">\n                <h1 class="text-2xl font-semibold tracking-tight text-gray-900 mb-6">{title}</h1>')

    # Remove any stray "Volver Atrás" buttons
    content = re.sub(r'<button onclick="window\.history\.back\(\)".*?Volver Atrás\s*</button>', '', content, flags=re.DOTALL)
    
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

print("Titles and Headers fixed.")

import os
import re

html_files = [
    "dashboard_admin.html",
    "admin_usuarios.html",
    "admin_pedidos.html",
    "admin_verificaciones.html"
]

base_dir = r"C:\Users\Brayan\Documents\Brayan JAVA\AccesoUsuarios\src\main\resources\templates"

# The new 2026 SaaS Sidebar
sidebar_template = """<aside class="w-[260px] bg-white border-r border-gray-100 flex-shrink-0 hidden md:flex flex-col z-20">
        <!-- Logo -->
        <div class="h-20 flex items-center px-8">
            <div class="flex items-center gap-3">
                <div class="bg-black text-white p-2 rounded-lg shadow-sm">
                    <i class="fas fa-leaf text-sm"></i>
                </div>
                <span class="text-lg font-bold text-gray-900 tracking-tight">Agro<span class="text-green-600">Conecta</span></span>
            </div>
        </div>

        <!-- Menú -->
        <nav class="flex-1 py-4 space-y-1 px-4 overflow-y-auto">
            <p class="px-4 text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-4 mt-2">Plataforma</p>
            
            <a href="/admin/dashboard" class="nav-link {ACTIVE_DASHBOARD} flex items-center gap-3 px-4 py-2.5 text-[13px] font-medium rounded-lg transition-all duration-200 group">
                <i class="fas fa-chart-pie w-5 text-center {ICON_DASHBOARD} transition-colors"></i> 
                Dashboard
            </a>
            
            <a href="/admin/usuarios" class="nav-link {ACTIVE_USUARIOS} flex items-center gap-3 px-4 py-2.5 text-[13px] font-medium rounded-lg transition-all duration-200 group">
                <i class="fas fa-users w-5 text-center {ICON_USUARIOS} transition-colors"></i> 
                Usuarios
            </a>
            
            <a href="/admin/pedidos" class="nav-link {ACTIVE_PEDIDOS} flex items-center gap-3 px-4 py-2.5 text-[13px] font-medium rounded-lg transition-all duration-200 group">
                <i class="fas fa-box-open w-5 text-center {ICON_PEDIDOS} transition-colors"></i> 
                Pedidos
            </a>

            <a href="/admin/usuarios/verificaciones" class="nav-link {ACTIVE_KYC} flex items-center gap-3 px-4 py-2.5 text-[13px] font-medium rounded-lg transition-all duration-200 group">
                <i class="fas fa-id-card-clip w-5 text-center {ICON_KYC} transition-colors"></i> 
                Verificaciones KYC
            </a>
        </nav>

        <!-- Perfil Mini y Cerrar Sesión -->
        <div class="p-4 mb-4 mx-4 border border-gray-100 rounded-xl bg-gray-50/50">
            <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-gray-900 flex items-center justify-center text-white text-xs font-bold shadow-sm">
                    AD
                </div>
                <div class="flex-1 min-w-0">
                    <p class="text-[13px] font-bold text-gray-900 truncate">Admin</p>
                    <p class="text-[11px] text-gray-500 truncate">admin@agro.co</p>
                </div>
                <form action="/logout" method="post" class="m-0">
                    <button type="submit" title="Cerrar Sesión" class="text-gray-400 hover:text-gray-900 transition-colors p-2">
                        <i class="fas fa-sign-out-alt text-sm"></i>
                    </button>
                </form>
            </div>
        </div>
    </aside>"""

def get_active_classes(file_name):
    # default inactive
    d = {"ACTIVE_DASHBOARD": "text-gray-500 hover:bg-gray-100 hover:text-gray-900", "ICON_DASHBOARD": "text-gray-400 group-hover:text-gray-900",
         "ACTIVE_USUARIOS": "text-gray-500 hover:bg-gray-100 hover:text-gray-900", "ICON_USUARIOS": "text-gray-400 group-hover:text-gray-900",
         "ACTIVE_PEDIDOS": "text-gray-500 hover:bg-gray-100 hover:text-gray-900", "ICON_PEDIDOS": "text-gray-400 group-hover:text-gray-900",
         "ACTIVE_KYC": "text-gray-500 hover:bg-gray-100 hover:text-gray-900", "ICON_KYC": "text-gray-400 group-hover:text-gray-900"}
    
    active_bg = "bg-black text-white shadow-md shadow-black/10"
    active_icon = "text-white/80 group-hover:text-white"
    
    if file_name == "dashboard_admin.html":
        d["ACTIVE_DASHBOARD"] = active_bg
        d["ICON_DASHBOARD"] = active_icon
    elif file_name == "admin_usuarios.html":
        d["ACTIVE_USUARIOS"] = active_bg
        d["ICON_USUARIOS"] = active_icon
    elif file_name == "admin_pedidos.html":
        d["ACTIVE_PEDIDOS"] = active_bg
        d["ICON_PEDIDOS"] = active_icon
    elif file_name == "admin_verificaciones.html":
        d["ACTIVE_KYC"] = active_bg
        d["ICON_KYC"] = active_icon
        
    return d

def process_file(file):
    file_path = os.path.join(base_dir, file)
    if not os.path.exists(file_path):
        return
        
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # 1. Update font to Inter everywhere
    content = content.replace("Plus Jakarta Sans", "Inter")
    content = content.replace("Plus+Jakarta+Sans", "Inter")

    # 2. Update body background to ultra clean SaaS #FAFAFA
    content = re.sub(r'<body class="([^"]*)"', r'<body class="bg-[#FAFAFA] flex h-screen overflow-hidden text-[#111827] font-sans antialiased"', content)

    # 3. Replace the sidebar
    classes = get_active_classes(file)
    new_sidebar = sidebar_template.format(**classes)
    content = re.sub(r"<aside.*?</aside>", new_sidebar, content, flags=re.DOTALL)
    
    # 4. Remove the old header (which had logout and stuff) and make a clean header
    clean_header = """<header class="h-16 bg-[#FAFAFA] border-b border-gray-100 flex items-center justify-between px-8 sticky top-0 z-10">
                <div class="flex items-center gap-4">
                    {BACK_BUTTON}
                </div>
            </header>"""
    
    back_btn_html = """<button onclick="window.history.back()" class="flex items-center gap-2 text-[13px] font-medium text-gray-500 hover:text-gray-900 transition bg-white border border-gray-200 px-3 py-1.5 rounded-lg shadow-sm">
                        <i class="fas fa-arrow-left"></i> Volver
                    </button>"""
    
    if file == "dashboard_admin.html":
        header_rendered = clean_header.replace("{BACK_BUTTON}", "")
    else:
        header_rendered = clean_header.replace("{BACK_BUTTON}", back_btn_html)

    # remove old header
    content = re.sub(r"<header class=\"h-20 bg-white.*?</header>", header_rendered, content, flags=re.DOTALL)
    
    # Also remove any remaining old 'Volver Atrás' buttons in the content
    content = re.sub(r'<button onclick="window\.history\.back\(\)".*?</button>', '', content, flags=re.DOTALL)

    # 5. Redesign Cards
    # Replace old heavily rounded cards with sleek modern ones
    content = content.replace('rounded-2xl', 'rounded-xl')
    content = content.replace('bg-white p-6 rounded-xl shadow-sm border border-gray-100', 'bg-white p-5 rounded-xl shadow-[0_2px_10px_-3px_rgba(6,81,237,0.05)] border border-gray-200/60')
    content = content.replace('bg-white p-8 rounded-2xl shadow-sm border border-gray-100', 'bg-white p-8 rounded-xl shadow-[0_2px_10px_-3px_rgba(6,81,237,0.05)] border border-gray-200/60')
    
    # 6. Make fonts smaller and sleeker
    content = content.replace('text-2xl font-bold', 'text-xl font-semibold tracking-tight')
    content = content.replace('text-3xl font-extrabold', 'text-2xl font-semibold tracking-tight')
    content = content.replace('text-lg font-bold', 'text-base font-semibold tracking-tight')

    # 7. Redesign DataTables to look native
    content = content.replace('<table class="w-full text-left text-sm text-gray-500">', '<table class="w-full text-left text-[13px] text-gray-600">')
    content = content.replace('<thead class="text-xs text-gray-700 uppercase bg-gray-50 border-b border-gray-200">', '<thead class="text-[11px] text-gray-500 uppercase tracking-wider bg-[#FAFAFA] border-b border-gray-100">')
    
    # specific dashboard green gradient block redesign to black minimal
    content = content.replace('bg-gradient-to-r from-green-900 to-green-800', 'bg-black')
    content = content.replace('text-green-100', 'text-gray-400')
    content = content.replace('bg-green-600 border border-green-500 text-white', 'bg-white text-black')
    
    # remove duplicate logouts
    content = content.replace('<a href="/logout" class="text-sm font-medium text-gray-500 hover:text-gray-800 transition">Cerrar Sesión</a>', '')

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

for file in html_files:
    process_file(file)

print("Modernization applied to all admin templates.")

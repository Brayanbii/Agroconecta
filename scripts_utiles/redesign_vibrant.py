import os
import re

html_files = {
    "dashboard_admin.html": "Panel de Control",
    "admin_usuarios.html": "Gestión de Usuarios",
    "admin_pedidos.html": "Gestión de Pedidos",
    "admin_verificaciones.html": "Verificaciones KYC"
}

base_dir = r"C:\Users\Brayan\Documents\Brayan JAVA\AccesoUsuarios\src\main\resources\templates"

# Vibrant AgroConecta Sidebar (White & Green)
sidebar_template = """<aside class="w-[280px] bg-white border-r border-gray-100 flex-shrink-0 hidden md:flex flex-col z-20 shadow-xl shadow-gray-200/30">
        <!-- Logo -->
        <div class="h-24 flex items-center px-8">
            <div class="flex items-center gap-3">
                <div class="bg-gradient-to-br from-green-400 to-green-600 text-white w-10 h-10 rounded-xl shadow-lg shadow-green-500/40 flex items-center justify-center">
                    <i class="fas fa-leaf text-xl"></i>
                </div>
                <span class="text-2xl font-extrabold text-gray-800 tracking-tight">Agro<span class="text-green-600">Conecta</span></span>
            </div>
        </div>

        <!-- Menú -->
        <nav class="flex-1 py-6 space-y-2 px-6 overflow-y-auto">
            <p class="px-2 text-xs font-bold text-gray-400 uppercase tracking-widest mb-6 mt-2">Plataforma Administrativa</p>
            
            <a href="/admin/dashboard" class="nav-link {ACTIVE_DASHBOARD} flex items-center gap-4 px-4 py-3.5 text-sm font-bold rounded-xl transition-all duration-300 group">
                <div class="{ICON_BG_DASHBOARD} w-8 h-8 rounded-lg flex items-center justify-center transition-all duration-300">
                    <i class="fas fa-chart-pie {ICON_DASHBOARD}"></i> 
                </div>
                Dashboard
            </a>
            
            <a href="/admin/usuarios" class="nav-link {ACTIVE_USUARIOS} flex items-center gap-4 px-4 py-3.5 text-sm font-bold rounded-xl transition-all duration-300 group">
                <div class="{ICON_BG_USUARIOS} w-8 h-8 rounded-lg flex items-center justify-center transition-all duration-300">
                    <i class="fas fa-users {ICON_USUARIOS}"></i> 
                </div>
                Usuarios
            </a>
            
            <a href="/admin/pedidos" class="nav-link {ACTIVE_PEDIDOS} flex items-center gap-4 px-4 py-3.5 text-sm font-bold rounded-xl transition-all duration-300 group">
                <div class="{ICON_BG_PEDIDOS} w-8 h-8 rounded-lg flex items-center justify-center transition-all duration-300">
                    <i class="fas fa-box-open {ICON_PEDIDOS}"></i> 
                </div>
                Pedidos
            </a>

            <a href="/admin/usuarios/verificaciones" class="nav-link {ACTIVE_KYC} flex items-center gap-4 px-4 py-3.5 text-sm font-bold rounded-xl transition-all duration-300 group">
                <div class="{ICON_BG_KYC} w-8 h-8 rounded-lg flex items-center justify-center transition-all duration-300">
                    <i class="fas fa-id-card-clip {ICON_KYC}"></i> 
                </div>
                Verificaciones KYC
            </a>
        </nav>

        <!-- Perfil y Cerrar Sesión -->
        <div class="p-6">
            <div class="flex items-center justify-between p-4 rounded-2xl bg-gray-50 border border-gray-100 shadow-inner">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center text-green-600 text-sm font-extrabold shadow-sm">
                        AD
                    </div>
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-bold text-gray-800 truncate">Admin</p>
                        <p class="text-xs text-gray-500 font-medium truncate">admin@agro.co</p>
                    </div>
                </div>
                <form action="/logout" method="post" class="m-0">
                    <button type="submit" title="Cerrar Sesión" class="w-10 h-10 rounded-xl bg-white border border-gray-200 text-gray-400 hover:text-red-500 hover:border-red-200 hover:bg-red-50 transition-all duration-300 flex items-center justify-center shadow-sm">
                        <i class="fas fa-sign-out-alt"></i>
                    </button>
                </form>
            </div>
        </div>
    </aside>"""

def get_active_classes(file_name):
    # default inactive
    d = {
        "ACTIVE_DASHBOARD": "text-gray-500 hover:bg-green-50 hover:text-green-700", "ICON_DASHBOARD": "text-gray-400 group-hover:text-green-600", "ICON_BG_DASHBOARD": "bg-gray-100 group-hover:bg-green-100",
        "ACTIVE_USUARIOS": "text-gray-500 hover:bg-green-50 hover:text-green-700", "ICON_USUARIOS": "text-gray-400 group-hover:text-green-600", "ICON_BG_USUARIOS": "bg-gray-100 group-hover:bg-green-100",
        "ACTIVE_PEDIDOS": "text-gray-500 hover:bg-green-50 hover:text-green-700", "ICON_PEDIDOS": "text-gray-400 group-hover:text-green-600", "ICON_BG_PEDIDOS": "bg-gray-100 group-hover:bg-green-100",
        "ACTIVE_KYC": "text-gray-500 hover:bg-green-50 hover:text-green-700", "ICON_KYC": "text-gray-400 group-hover:text-green-600", "ICON_BG_KYC": "bg-gray-100 group-hover:bg-green-100",
    }
    
    active_bg = "bg-green-50 text-green-700 shadow-sm ring-1 ring-green-100"
    active_icon = "text-green-600"
    active_icon_bg = "bg-green-200/50"
    
    if file_name == "dashboard_admin.html":
        d["ACTIVE_DASHBOARD"] = active_bg
        d["ICON_DASHBOARD"] = active_icon
        d["ICON_BG_DASHBOARD"] = active_icon_bg
    elif file_name == "admin_usuarios.html":
        d["ACTIVE_USUARIOS"] = active_bg
        d["ICON_USUARIOS"] = active_icon
        d["ICON_BG_USUARIOS"] = active_icon_bg
    elif file_name == "admin_pedidos.html":
        d["ACTIVE_PEDIDOS"] = active_bg
        d["ICON_PEDIDOS"] = active_icon
        d["ICON_BG_PEDIDOS"] = active_icon_bg
    elif file_name == "admin_verificaciones.html":
        d["ACTIVE_KYC"] = active_bg
        d["ICON_KYC"] = active_icon
        d["ICON_BG_KYC"] = active_icon_bg
        
    return d

for file, title in html_files.items():
    file_path = os.path.join(base_dir, file)
    if not os.path.exists(file_path):
        continue
        
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Apply bright and spacious body background
    content = re.sub(r'<body class="([^"]*)"', r'<body class="bg-[#f8fafc] flex h-screen overflow-hidden text-gray-800 font-sans antialiased"', content)

    # Revert the "black/dark" linear style to vibrant green
    content = content.replace("bg-[#FAFAFA]/80 backdrop-blur-md border-b border-gray-200/60", "bg-transparent")
    content = content.replace("bg-[#FAFAFA] border-b border-gray-100", "bg-transparent")
    content = content.replace("bg-black text-white", "bg-green-600 text-white")
    content = content.replace("bg-[#111827]", "bg-gray-800")
    content = content.replace("text-[#111827]", "text-gray-800")

    # Replace Sidebar
    classes = get_active_classes(file)
    new_sidebar = sidebar_template.format(**classes)
    content = re.sub(r"<aside.*?</aside>", new_sidebar, content, flags=re.DOTALL)
    
    # Modern Spacious Header
    # Removing old linear header
    back_btn_html = ""
    if file != "dashboard_admin.html":
        back_btn_html = """<button onclick="window.history.back()" class="flex items-center gap-2 text-sm font-bold text-green-700 bg-white border border-green-200 hover:bg-green-50 hover:border-green-300 transition-all shadow-sm px-4 py-2 rounded-xl mr-6">
                    <i class="fas fa-arrow-left"></i> Volver
                </button>"""

    new_header = f"""<header class="h-24 flex items-center justify-between px-10">
                <div class="flex items-center">
                    {back_btn_html}
                    <div>
                        <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">AgroConecta Admin</p>
                        <h1 class="text-3xl font-extrabold text-gray-900 tracking-tight">{title}</h1>
                    </div>
                </div>
            </header>"""

    content = re.sub(r"<header.*?</header>", new_header, content, flags=re.DOTALL)
    
    # Remove inner redundant titles and buttons
    content = re.sub(r'<h1 class="text-2xl font-semibold tracking-tight text-gray-900 mb-6">.*?</h1>', '', content, flags=re.DOTALL)
    content = re.sub(r'<button onclick="window\.history\.back\(\)".*?</button>', '', content, flags=re.DOTALL)

    # Boost Card styling for vibrancy
    # "bg-white p-5 rounded-xl shadow-[0_2px_10px_-3px_rgba(6,81,237,0.05)] border border-gray-200/60"
    content = re.sub(r'bg-white p-5 rounded-xl shadow-\[.*?\] border border-gray-200/60', 'bg-white p-8 rounded-2xl shadow-xl shadow-gray-200/40 border-0', content)
    content = re.sub(r'bg-white p-8 rounded-xl shadow-\[.*?\] border border-gray-200/60', 'bg-white p-8 rounded-2xl shadow-xl shadow-gray-200/40 border-0', content)
    
    # Tables spacing and design
    content = content.replace('text-[13px] text-gray-600', 'text-sm text-gray-600')
    content = content.replace('text-[11px] text-gray-500 uppercase tracking-wider bg-[#FAFAFA] border-b border-gray-100', 'text-xs text-gray-400 uppercase tracking-wider font-bold bg-gray-50 border-b border-gray-100')
    
    # Fix Dashboard inner structure spacing
    content = content.replace('<main class="flex-1 flex flex-col h-screen overflow-y-auto">', '<main class="flex-1 flex flex-col h-screen overflow-y-auto">\n            <div class="px-10 pb-10 max-w-7xl mx-auto w-full">')
    
    # We must close that div at the end before </main>
    # Find </main> and replace it
    content = content.replace('</main>', '</div>\n        </main>')
    
    # The <div class="p-8"> was used heavily, replace it with nothing or just a wrapper since we added <div class="px-10 pb-10"> above
    content = content.replace('<div class="p-8">', '<div class="space-y-8 mt-4">')

    # Add back the nice green block for dashboard gestion rapida if it was removed or blackened
    content = content.replace('bg-black', 'bg-gradient-to-br from-green-800 to-green-900 shadow-2xl shadow-green-900/20')
    
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

print("Vibrant redesign applied.")

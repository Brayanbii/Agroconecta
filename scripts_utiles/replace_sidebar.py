import os
import re

html_files = [
    "dashboard_admin.html",
    "admin_usuarios.html",
    "admin_pedidos.html",
    "admin_usuario_form.html",
    "admin_verificaciones.html"
]

base_dir = r"C:\Users\Brayan\Documents\Brayan JAVA\AccesoUsuarios\src\main\resources\templates"

sidebar_template = """<aside class="w-64 bg-slate-900 flex-shrink-0 hidden md:flex flex-col shadow-2xl z-20">
        <!-- Logo -->
        <div class="h-20 flex items-center px-8 border-b border-slate-800">
            <div class="flex items-center gap-3">
                <div class="bg-emerald-500 text-white p-2 rounded-xl shadow-lg shadow-emerald-500/30">
                    <i class="fas fa-leaf text-lg"></i>
                </div>
                <span class="text-xl font-bold text-white tracking-tight">Agro<span class="text-emerald-400">Conecta</span></span>
            </div>
        </div>

        <!-- Menú -->
        <nav class="flex-1 py-6 space-y-2 px-4 overflow-y-auto">
            <p class="px-4 text-xs font-bold text-slate-500 uppercase tracking-wider mb-4 mt-2">Principal</p>
            
            <a href="/admin/dashboard" class="nav-link {ACTIVE_DASHBOARD} flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 group hover:bg-slate-800 hover:text-white">
                <i class="fas fa-chart-pie w-5 text-center {ICON_DASHBOARD} group-hover:text-emerald-400"></i> 
                Dashboard
            </a>
            
            <a href="/admin/usuarios" class="nav-link {ACTIVE_USUARIOS} flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 group hover:bg-slate-800 hover:text-white">
                <i class="fas fa-users w-5 text-center {ICON_USUARIOS} group-hover:text-emerald-400"></i> 
                Usuarios
            </a>
            
            <a href="/admin/pedidos" class="nav-link {ACTIVE_PEDIDOS} flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 group hover:bg-slate-800 hover:text-white">
                <i class="fas fa-box-open w-5 text-center {ICON_PEDIDOS} group-hover:text-emerald-400"></i> 
                Pedidos
            </a>

            <a href="/admin/usuarios/verificaciones" class="nav-link {ACTIVE_KYC} flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 group hover:bg-slate-800 hover:text-white">
                <i class="fas fa-id-card-clip w-5 text-center {ICON_KYC} group-hover:text-emerald-400"></i> 
                Validaciones KYC
            </a>
        </nav>

        <!-- Perfil Mini y Cerrar Sesión -->
        <div class="p-4 border-t border-slate-800 bg-slate-900/50">
            <div class="flex items-center gap-3 p-3 rounded-xl bg-slate-800/50 border border-slate-700/50">
                <div class="w-10 h-10 rounded-full bg-emerald-500/20 flex items-center justify-center text-emerald-400 font-bold border border-emerald-500/30">
                    AD
                </div>
                <div class="flex-1 min-w-0">
                    <p class="text-sm font-bold text-white truncate">Admin</p>
                    <p class="text-xs text-slate-400 truncate">admin@agro.co</p>
                </div>
                <form action="/logout" method="post" class="m-0">
                    <button type="submit" title="Cerrar Sesión" class="w-10 h-10 flex items-center justify-center rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500 hover:text-white transition-colors">
                        <i class="fas fa-power-off text-sm"></i>
                    </button>
                </form>
            </div>
        </div>
    </aside>"""

back_btn = """<button onclick="window.history.back()" class="flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-emerald-600 transition bg-white border border-slate-200 hover:border-emerald-200 px-4 py-2 rounded-xl shadow-sm">
                    <i class="fas fa-arrow-left"></i> Volver Atrás
                </button>"""

def get_active_classes(file_name):
    # default inactive
    d = {"ACTIVE_DASHBOARD": "text-slate-400", "ICON_DASHBOARD": "text-slate-500",
         "ACTIVE_USUARIOS": "text-slate-400", "ICON_USUARIOS": "text-slate-500",
         "ACTIVE_PEDIDOS": "text-slate-400", "ICON_PEDIDOS": "text-slate-500",
         "ACTIVE_KYC": "text-slate-400", "ICON_KYC": "text-slate-500"}
    
    active_bg = "bg-slate-800 text-white border-l-4 border-emerald-500"
    active_icon = "text-emerald-400"
    
    if file_name == "dashboard_admin.html":
        d["ACTIVE_DASHBOARD"] = active_bg
        d["ICON_DASHBOARD"] = active_icon
    elif file_name == "admin_usuarios.html" or file_name == "admin_usuario_form.html":
        d["ACTIVE_USUARIOS"] = active_bg
        d["ICON_USUARIOS"] = active_icon
    elif file_name == "admin_pedidos.html":
        d["ACTIVE_PEDIDOS"] = active_bg
        d["ICON_PEDIDOS"] = active_icon
    elif file_name == "admin_verificaciones.html":
        d["ACTIVE_KYC"] = active_bg
        d["ICON_KYC"] = active_icon
        
    return d

for file in html_files:
    file_path = os.path.join(base_dir, file)
    if not os.path.exists(file_path):
        continue
        
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
        
    # Replace sidebar
    classes = get_active_classes(file)
    new_sidebar = sidebar_template.format(**classes)
    
    # regex to find the aside element. It might have nested tags.
    # We find <aside ...> ... </aside> using re.DOTALL
    new_content = re.sub(r"<aside.*?</aside>", new_sidebar, content, flags=re.DOTALL)
    
    # Also inject the Back button into the header.
    # Look for the main header
    if '<header class="' in new_content:
        # It has a header. Let's see if we can put the back button in it or just below it.
        pass
        
    # The user wanted a global "Volver atras" button.
    # Let's insert it right after <main class="..."> or <div class="p-8">
    # Actually, a good place is near the title (<h1>...</h1>)
    if 'Volver Atrás' not in new_content:
        new_content = re.sub(r"(<h1[^>]*>.*?</h1>)", r"\1\n                " + back_btn, new_content, count=1)
    
    # Change body background to be more modern slate-50
    new_content = re.sub(r'<body class="([^"]*)"', lambda m: '<body class="' + m.group(1).replace('bg-gray-50', 'bg-slate-50').replace('text-gray-800', 'text-slate-800').replace('bg-gray-100', 'bg-slate-50') + '"', new_content)
    
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(new_content)

print("Done replacing sidebars and modernizing.")

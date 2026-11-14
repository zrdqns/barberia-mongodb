import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import '@fontsource-variable/besley/wght.css'
import '@fontsource-variable/besley/wght-italic.css'
import '@fontsource-variable/hanken-grotesk/wght.css'
import '@barberia/compartido/estilos/base.css'
import '@barberia/compartido/estilos/estructura.css'
import '@barberia/compartido/estilos/componentes.css'
import '@barberia/compartido/estilos/citas.css'

import { ProveedorAvisos } from '@barberia/compartido/componentes/Avisos.jsx'
import { ProveedorConexion } from '@barberia/compartido/conexion.jsx'
import { ProveedorSesion } from '@barberia/compartido/sesion.jsx'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ProveedorSesion>
      <ProveedorConexion>
        <ProveedorAvisos>
          <App />
        </ProveedorAvisos>
      </ProveedorConexion>
    </ProveedorSesion>
  </StrictMode>,
)

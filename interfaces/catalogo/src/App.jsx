import { Armchair, Scissors } from 'lucide-react'
import { API_CATALOGO, INTERFAZ_AGENDA } from '@barberia/compartido/api.js'
import Marco from '@barberia/compartido/Marco.jsx'
import PantallaIngreso from '@barberia/compartido/componentes/PantallaIngreso.jsx'
import { useSesion } from '@barberia/compartido/sesion.jsx'
import Barberos from './paginas/Barberos.jsx'
import Servicios from './paginas/Servicios.jsx'

// Segunda interfaz: el catálogo que administra el dueño. Todo viene de api-catalogo.
const SECCIONES = [
  { clave: 'servicios', texto: 'Servicios', Icono: Scissors, Pagina: Servicios },
  { clave: 'barberos', texto: 'Barberos', Icono: Armchair, Pagina: Barberos },
]
const OTRA = { texto: 'la agenda', url: INTERFAZ_AGENDA }

export default function App() {
  const { sesion } = useSesion()

  if (!sesion) {
    return (
      <PantallaIngreso
        interfaz="Catálogo"
        descripcion="Servicios con su precio y duración, y el equipo de barberos."
        servidor={API_CATALOGO}
      />
    )
  }
  return <Marco interfaz="Catálogo" secciones={SECCIONES} otra={OTRA} />
}

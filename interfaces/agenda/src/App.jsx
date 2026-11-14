import { CalendarDays, Ticket, Users } from 'lucide-react'
import { API_CITAS, INTERFAZ_CATALOGO } from '@barberia/compartido/api.js'
import Marco from '@barberia/compartido/Marco.jsx'
import PantallaIngreso from '@barberia/compartido/componentes/PantallaIngreso.jsx'
import { useSesion } from '@barberia/compartido/sesion.jsx'
import Citas from './paginas/Citas.jsx'
import Clientes from './paginas/Clientes.jsx'
import Inicio from './paginas/Inicio.jsx'

// Primera interfaz: la agenda de la recepción. Clientes, citas y horario vienen de api-citas;
// los servicios y barberos para agendar, de api-catalogo.
const SECCIONES = [
  { clave: 'hoy', texto: 'Hoy', Icono: CalendarDays, Pagina: Inicio },
  { clave: 'citas', texto: 'Citas', Icono: Ticket, Pagina: Citas },
  { clave: 'clientes', texto: 'Clientes', Icono: Users, Pagina: Clientes },
]
const OTRA = { texto: 'el catálogo', url: INTERFAZ_CATALOGO }

export default function App() {
  const { sesion } = useSesion()

  if (!sesion) {
    return (
      <PantallaIngreso
        interfaz="Agenda"
        descripcion="Citas del día, agenda por barbero y clientes de la barbería."
        servidor={API_CITAS}
      />
    )
  }
  return <Marco interfaz="Agenda" secciones={SECCIONES} otra={OTRA} />
}

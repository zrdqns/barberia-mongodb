import { clientes } from '@barberia/compartido/api.js'
import { camposCliente } from '../camposCliente.js'
import PaginaCrud from '@barberia/compartido/componentes/PaginaCrud.jsx'
import { esAdmin, useSesion } from '@barberia/compartido/sesion.jsx'

const textos = {
  titulo: 'Clientes',
  descripcion: 'Personas que agendan citas en la barbería.',
  nuevo: 'Nuevo cliente',
  editar: 'Editar cliente',
  eliminar: 'Eliminar cliente',
  guardarNuevo: 'Crear cliente',
  creado: 'Cliente creado',
  actualizado: 'Cliente actualizado',
  eliminado: 'Cliente eliminado',
  buscar: 'Buscar por nombre, documento o teléfono',
  vacioTitulo: 'Aún no hay clientes',
  vacioTexto: 'Sin clientes no se pueden agendar citas.',
  ninguno: 'Ningún cliente',
  confirmar: (cliente) =>
    `Se eliminará a ${cliente.nombre} (documento ${cliente.documento}). Sus citas anteriores se conservan con su nombre. Esta acción no se puede deshacer.`,
}

const columnas = [
  { titulo: 'Nombre', clase: 'principal', celda: (cliente) => cliente.nombre },
  { titulo: 'Documento', clase: 'numero-id', celda: (cliente) => cliente.documento },
  { titulo: 'Teléfono', clase: 'numero-id', celda: (cliente) => cliente.telefono },
  { titulo: 'Correo', celda: (cliente) => cliente.correo || <span className="sin-dato">Sin correo</span> },
]

const textoBusqueda = (cliente) => `${cliente.nombre} ${cliente.documento} ${cliente.telefono}`

// La recepción crea y edita clientes; eliminarlos es solo del administrador (api-citas responde 403)
export default function Clientes() {
  const { sesion } = useSesion()
  return (
    <PaginaCrud
      api={clientes}
      textos={textos}
      columnas={columnas}
      campos={camposCliente}
      textoBusqueda={textoBusqueda}
      puedeEliminar={esAdmin(sesion)}
    />
  )
}

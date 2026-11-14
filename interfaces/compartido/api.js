import { SESION_VENCIDA, tokenActual } from './sesion.jsx'

// Direcciones de los dos microservicios y de las dos interfaces.
// Las interfaces hablan con ambos; api-citas además consume api-catalogo por su cuenta.
export const API_CITAS = import.meta.env.VITE_API_CITAS ?? 'http://localhost:8082'
export const API_CATALOGO = import.meta.env.VITE_API_CATALOGO ?? 'http://localhost:8083'
export const INTERFAZ_AGENDA = import.meta.env.VITE_INTERFAZ_AGENDA ?? 'http://localhost:5174'
export const INTERFAZ_CATALOGO = import.meta.env.VITE_INTERFAZ_CATALOGO ?? 'http://localhost:5175'

// Dónde está cada microservicio: "Puerto 8082"
export const lugar = (base) => `Puerto ${new URL(base).port}`

const NOMBRES = {
  [API_CITAS]: 'api-citas',
  [API_CATALOGO]: 'api-catalogo',
}

// Hace la petición y devuelve el cuerpo ya convertido.
// Si hay sesión, envía el token JWT en el header Authorization.
// Si algo falla lanza un Error con un mensaje que se puede mostrar al usuario.
async function pedir(base, ruta, metodo = 'GET', cuerpo) {
  const token = tokenActual()
  const headers = {}
  if (cuerpo) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let respuesta
  try {
    respuesta = await fetch(base + ruta, {
      method: metodo,
      headers,
      body: cuerpo ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw new Error(`No hay conexión con ${NOMBRES[base]}. Verifique que el microservicio esté encendido.`)
  }

  const texto = await respuesta.text()
  let datos = texto
  try {
    datos = JSON.parse(texto)
  } catch {
    // La respuesta era texto plano (por ejemplo /api/status)
  }

  // El token venció o ya no es válido: se pide iniciar sesión otra vez
  if (respuesta.status === 401 && token) window.dispatchEvent(new Event(SESION_VENCIDA))

  if (!respuesta.ok) {
    throw new Error(datos?.mensaje ?? `La operación falló (HTTP ${respuesta.status}).`)
  }
  return datos
}

// POST /api/auth/login: devuelve { token, tipo, usuario, rol, expira }
export const iniciarSesion = (base, username, password) =>
  pedir(base, '/api/auth/login', 'POST', { username, password })

// Las cinco operaciones que comparte cada recurso REST
function recurso(base, ruta) {
  return {
    listar: () => pedir(base, ruta),
    crear: (datos) => pedir(base, ruta, 'POST', datos),
    actualizar: (id, datos) => pedir(base, `${ruta}/${id}`, 'PUT', datos),
    eliminar: (id) => pedir(base, `${ruta}/${id}`, 'DELETE'),
  }
}

export const clientes = recurso(API_CITAS, '/api/clientes')
export const servicios = recurso(API_CATALOGO, '/api/servicios')
export const barberos = recurso(API_CATALOGO, '/api/barberos')

export const citas = {
  ...recurso(API_CITAS, '/api/citas'),
  cambiarEstado: (id, estado) => pedir(API_CITAS, `/api/citas/${id}/estado`, 'PATCH', { estado }),
}

// Horario de atención por día de la semana. Lo guarda api-citas, que no deja agendar fuera de él.
export const horario = {
  listar: () => pedir(API_CITAS, '/api/horario'),
}

export const estado = {
  citas: () => pedir(API_CITAS, '/api/status'),
  catalogo: () => pedir(API_CATALOGO, '/api/status'),
  // api-citas le pregunta a api-catalogo: comunicación de backend a backend
  conexion: () => pedir(API_CITAS, '/api/conexion/catalogo'),
}

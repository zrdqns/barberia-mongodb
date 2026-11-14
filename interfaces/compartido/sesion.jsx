import { createContext, use, useCallback, useEffect, useState } from 'react'

// La sesión es lo que devuelve POST /api/auth/login: { token, tipo, usuario, rol, expira }.
// Se guarda en el navegador para no pedir el usuario en cada recarga, hasta que el token vence.
const CLAVE = 'sesion'
export const SESION_VENCIDA = 'sesion-vencida'

function leerGuardada() {
  try {
    const sesion = JSON.parse(localStorage.getItem(CLAVE))
    if (sesion?.token && new Date(sesion.expira) > new Date()) return sesion
  } catch {
    // Sin almacenamiento o con un valor dañado: se empieza sin sesión
  }
  return null
}

// api.js lee el token de aquí en cada petición
let actual = leerGuardada()
export const tokenActual = () => actual?.token

function guardar(sesion) {
  actual = sesion
  try {
    if (sesion) localStorage.setItem(CLAVE, JSON.stringify(sesion))
    else localStorage.removeItem(CLAVE)
  } catch {
    // Sin almacenamiento la sesión dura lo que dure la pestaña
  }
}

const ContextoSesion = createContext(null)

// sesion: null mientras no se ha iniciado. aviso: por qué se cerró, si no fue a pedido.
export function ProveedorSesion({ children }) {
  const [sesion, setSesion] = useState(actual)
  const [aviso, setAviso] = useState('')

  const entrar = useCallback((nueva) => {
    guardar(nueva)
    setAviso('')
    setSesion(nueva)
  }, [])

  const salir = useCallback((motivo = '') => {
    guardar(null)
    setAviso(motivo)
    setSesion(null)
  }, [])

  useEffect(() => {
    if (!sesion) return
    // La API rechazó el token, o vence con la página abierta: se pide iniciar sesión de nuevo
    const alVencer = () => salir('Su sesión venció. Inicie sesión de nuevo para seguir.')
    const reloj = setTimeout(alVencer, new Date(sesion.expira) - Date.now())
    window.addEventListener(SESION_VENCIDA, alVencer)
    return () => {
      clearTimeout(reloj)
      window.removeEventListener(SESION_VENCIDA, alVencer)
    }
  }, [sesion, salir])

  return <ContextoSesion value={{ sesion, aviso, entrar, salir }}>{children}</ContextoSesion>
}

export const useSesion = () => use(ContextoSesion)

export const esAdmin = (sesion) => sesion?.rol === 'ADMIN'
export const nombreRol = (rol) => (rol === 'ADMIN' ? 'Administrador' : 'Recepción')

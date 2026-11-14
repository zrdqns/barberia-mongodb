import { LogIn } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { iniciarSesion } from '../api.js'
import { useSesion } from '../sesion.jsx'
import Alerta from './Alerta.jsx'
import Campo, { describe } from './Campo.jsx'

// Lo primero que se ve en cada interfaz: usuario y contraseña. La API responde con un
// token JWT que desde ahí viaja en cada petición (ver api.js).
// servidor: el microservicio que emite el token. Los dos usan la misma clave, así que
// el token de uno también lo acepta el otro.
export default function PantallaIngreso({ interfaz, descripcion, servidor }) {
  const { aviso, entrar } = useSesion()
  const [usuario, setUsuario] = useState('')
  const [clave, setClave] = useState('')
  const [errores, setErrores] = useState({})
  const [rechazo, setRechazo] = useState('')
  const [enviando, setEnviando] = useState(false)
  const primerCampo = useRef(null)

  useEffect(() => {
    document.title = `Iniciar sesión | ${interfaz} | Barbería El Remolino`
    primerCampo.current?.focus()
  }, [interfaz])

  async function enviar(evento) {
    evento.preventDefault()

    const encontrados = {}
    if (!usuario.trim()) encontrados.usuario = 'Escriba el usuario.'
    if (!clave) encontrados.clave = 'Escriba la contraseña.'
    setErrores(encontrados)
    const primero = Object.keys(encontrados)[0]
    if (primero) {
      document.getElementById(`ingreso-${primero}`)?.focus()
      return
    }

    setEnviando(true)
    setRechazo('')
    try {
      entrar(await iniciarSesion(servidor, usuario.trim(), clave))
    } catch (e) {
      // 401: usuario o contraseña incorrectos, o el microservicio no responde
      setRechazo(e.message)
      setClave('')
      setEnviando(false)
    }
  }

  return (
    <main className="ingreso">
      <div className="ingreso-tarjeta">
        <div className="ingreso-rotulo">
          <small>Barbería</small>
          <b>El Remolino</b>
          <span className="letrero-interfaz">{interfaz}</span>
        </div>

        <form className="ingreso-formulario" onSubmit={enviar} noValidate>
          <div>
            <h1>Iniciar sesión</h1>
            <p className="ingreso-texto">{descripcion}</p>
          </div>

          {(rechazo || aviso) && <Alerta titulo={rechazo ? 'No se pudo entrar' : 'Sesión cerrada'}>{rechazo || aviso}</Alerta>}

          <Campo id="ingreso-usuario" etiqueta="Usuario" error={errores.usuario}>
            <input
              ref={primerCampo}
              id="ingreso-usuario"
              autoComplete="username"
              autoCapitalize="none"
              spellCheck={false}
              value={usuario}
              aria-invalid={Boolean(errores.usuario)}
              aria-describedby={describe('ingreso-usuario', errores.usuario)}
              onChange={(evento) => {
                setUsuario(evento.target.value)
                setErrores((actuales) => ({ ...actuales, usuario: '' }))
              }}
            />
          </Campo>

          <Campo id="ingreso-clave" etiqueta="Contraseña" error={errores.clave}>
            <input
              id="ingreso-clave"
              type="password"
              autoComplete="current-password"
              value={clave}
              aria-invalid={Boolean(errores.clave)}
              aria-describedby={describe('ingreso-clave', errores.clave)}
              onChange={(evento) => {
                setClave(evento.target.value)
                setErrores((actuales) => ({ ...actuales, clave: '' }))
              }}
            />
          </Campo>

          <button type="submit" className="boton boton-primario ingreso-boton" disabled={enviando} aria-busy={enviando}>
            <LogIn aria-hidden="true" />
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>

          <p className="ingreso-pista">
            Cuentas de prueba, contraseña <code>123456</code>: <code>admin</code> (administrador) y{' '}
            <code>recepcion</code> (recepción).
          </p>
        </form>
      </div>
    </main>
  )
}

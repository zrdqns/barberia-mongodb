import { ArrowUpRight, LogOut, Moon, RefreshCw, Sun } from 'lucide-react'
import { useEffect, useMemo } from 'react'
import { API_CATALOGO, API_CITAS, lugar } from './api.js'
import Alerta from './componentes/Alerta.jsx'
import { textoSonda, useConexion } from './conexion.jsx'
import { rutaDe, useSeccion } from './navegacion.js'
import { nombreRol, useSesion } from './sesion.jsx'
import { useTema } from './tema.js'

// El armazón de cada interfaz: rótulo lateral con su menú, el estado de los microservicios,
// la sesión y el enlace a la otra interfaz. Cada interfaz pasa sus propias secciones.
// secciones: [{ clave, texto, Icono, Pagina }] · otra: { texto, url }
export default function Marco({ interfaz, secciones, otra }) {
  const claves = useMemo(() => secciones.map((seccion) => seccion.clave), [secciones])
  const seccion = useSeccion(claves)
  const [tema, alternarTema] = useTema()
  const { sesion, salir } = useSesion()
  const { texto, Pagina } = secciones.find((opcion) => opcion.clave === seccion)

  // La pestaña del navegador dice en qué interfaz y sección se está
  useEffect(() => {
    document.title = `${texto} | ${interfaz} | Barbería El Remolino`
  }, [texto, interfaz])

  return (
    <div className="app">
      <button type="button" className="saltar" onClick={() => document.getElementById('contenido').focus()}>
        Saltar al contenido
      </button>

      <aside className="lateral">
        <div className="lateral-fijo">
          <a className="letrero" href={rutaDe(claves[0])} aria-label={`Barbería El Remolino, ${interfaz}`}>
            <small>Barbería</small>
            <b>El Remolino</b>
            <span className="letrero-interfaz">{interfaz}</span>
          </a>

          <nav className="menu" aria-label="Secciones">
            {secciones.map(({ clave, texto: nombre, Icono }) => (
              <a key={clave} href={rutaDe(clave)} aria-current={seccion === clave ? 'page' : undefined}>
                <Icono aria-hidden="true" />
                {nombre}
              </a>
            ))}
          </nav>

          <div className="lateral-pie">
            <LucesConexion />
            <a className="boton-lateral" href={otra.url}>
              <ArrowUpRight aria-hidden="true" />
              <span>Abrir {otra.texto}</span>
            </a>
            <button type="button" className="boton-lateral" onClick={alternarTema}>
              {tema === 'oscuro' ? <Sun aria-hidden="true" /> : <Moon aria-hidden="true" />}
              <span>{tema === 'oscuro' ? 'Tema claro' : 'Tema oscuro'}</span>
            </button>
            <button type="button" className="boton-lateral sesion-lateral" title="Cerrar sesión" onClick={() => salir()}>
              <LogOut aria-hidden="true" />
              <span>
                <b>
                  {sesion.usuario} · {nombreRol(sesion.rol)}
                </b>
                <span className="oculto">. </span>
                Cerrar sesión
              </span>
            </button>
            <p className="lateral-nota">Diseño de Soluciones, prototipo final</p>
          </div>
        </div>
      </aside>

      <main id="contenido" className="contenido" tabIndex={-1}>
        <AvisoConexion />
        {/* La clave monta cada sección de nuevo: ninguna hereda el estado de otra */}
        <div className="pagina" key={seccion}>
          <Pagina />
        </div>
      </main>
    </div>
  )
}

const estadoDe = (valor) => (valor === undefined ? 'espera' : valor ? 'bien' : 'caido')

// Siempre a la vista, en todas las secciones: los dos microservicios y la llamada
// que api-citas le hace a api-catalogo. Al pulsarlo se comprueban de nuevo.
function LucesConexion() {
  const { apiCitas, apiCatalogo, entreApis, sonda, comprobando, comprobar } = useConexion()

  const filas = [
    { nombre: 'api-citas', valor: apiCitas, bien: 'en línea', mal: 'caído' },
    { nombre: 'api-catalogo', valor: apiCatalogo, bien: 'en línea', mal: 'caído' },
    {
      // La llamada de api-citas a api-catalogo, con el código que contestó
      nombre: 'RestClient',
      valor: entreApis,
      bien: 'HTTP 200',
      mal: sonda?.codigo ? `HTTP ${sonda.codigo}` : 'sin respuesta',
    },
  ]
  const caidas = filas.filter((fila) => fila.valor === false)
  const resumen =
    apiCitas === undefined
      ? 'Comprobando microservicios'
      : caidas.length === 0
        ? 'Los dos microservicios en línea'
        : `${caidas[0].nombre} ${caidas[0].mal}`

  return (
    <button
      type="button"
      className="boton-lateral luces"
      title="Comprobar ahora"
      aria-busy={comprobando}
      onClick={comprobar}
    >
      {/* En el teléfono solo cabe una luz con el resumen */}
      <span className="luces-resumen" data-estado={apiCitas === undefined ? 'espera' : caidas.length ? 'caido' : 'bien'}>
        <span className="luz" aria-hidden="true" />
        <span>{resumen}</span>
      </span>
      <span className="luces-lista">
        {/* Los signos ocultos separan las frases para quien usa lector de pantalla */}
        <span className="luces-titulo">
          Microservicios<span className="oculto">: </span>
        </span>
        {filas.map((fila) => (
          <span key={fila.nombre} className="luces-fila" data-estado={estadoDe(fila.valor)}>
            <span className="luz" aria-hidden="true" />
            <code>{fila.nombre}</code>
            <span>
              <span className="oculto"> </span>
              {fila.valor === undefined ? 'comprobando' : fila.valor ? fila.bien : fila.mal}
              <span className="oculto">. </span>
            </span>
          </span>
        ))}
      </span>
    </button>
  )
}

// Cuando algo no responde, cada sección lo dice arriba: qué pasa, qué se puede
// hacer y qué no, y lo que api-citas contestó de verdad en la última comprobación.
function AvisoConexion() {
  const { apiCitas, apiCatalogo, entreApis, sonda, comprobando, comprobar } = useConexion()
  if (apiCitas === undefined || (apiCitas && apiCatalogo && entreApis)) return null

  let titulo
  let texto
  if (!apiCitas && !apiCatalogo) {
    titulo = 'Ningún microservicio responde'
    texto = `Encienda api-catalogo (${lugar(API_CATALOGO).toLowerCase()}) y api-citas (${lugar(API_CITAS).toLowerCase()}), y verifique que MongoDB esté corriendo. Mientras tanto no hay datos que mostrar.`
  } else if (!apiCitas) {
    titulo = 'api-citas no responde'
    texto =
      'No se pueden consultar ni guardar clientes ni citas. Servicios y barberos se pueden consultar y editar, pero no eliminar: antes de eliminar, api-catalogo le pregunta a api-citas si tienen citas programadas.'
  } else if (!apiCatalogo) {
    titulo = 'api-catalogo no responde'
    texto =
      'Los clientes y las citas ya agendadas se pueden consultar. Agendar o editar una cita no: api-citas necesita consultar el catálogo.'
  } else {
    titulo = 'api-citas no alcanza a api-catalogo'
    texto = 'Los dos están encendidos, pero la llamada entre ellos falla. No se pueden agendar ni editar citas.'
  }

  return (
    <Alerta
      titulo={titulo}
      acciones={
        <button type="button" className="boton" disabled={comprobando} aria-busy={comprobando} onClick={comprobar}>
          <RefreshCw aria-hidden="true" />
          Comprobar de nuevo
        </button>
      }
    >
      {texto}
      {apiCitas && sonda && (
        <span className="respuesta-real">
          Respuesta de api-citas al consultarlo: <code>{textoSonda(sonda)}</code>
        </span>
      )}
    </Alerta>
  )
}

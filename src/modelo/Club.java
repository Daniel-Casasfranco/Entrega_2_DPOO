package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import persistencia.Persistencia;

public class Club implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Usuario> usuarios;
    private List<Equipo> equipos;
    private List<Competencia> competencias;
    private List<Instalacion> instalaciones;
    private List<Reserva> reservas;
    private List<SesionEntrenamiento> sesiones;
    private List<Partido> partidos;
    private List<Venta> ventas;
    private List<Producto> productos;
    private List<UbicacionInventario> ubicaciones;
    private List<ReglaDeporte> reglas;
    private List<SolicitudCambioTurno> solicitudes;

    private transient Persistencia persistencia;

    public Club() {
        usuarios = new ArrayList<>();
        equipos = new ArrayList<>();
        competencias = new ArrayList<>();
        instalaciones = new ArrayList<>();
        reservas = new ArrayList<>();
        sesiones = new ArrayList<>();
        partidos = new ArrayList<>();
        ventas = new ArrayList<>();
        productos = new ArrayList<>();
        ubicaciones = new ArrayList<>();
        reglas = new ArrayList<>();
        solicitudes = new ArrayList<>();
    }

    public void setPersistencia(Persistencia persistencia) {
        this.persistencia = persistencia;
    }

    public void guardar() {
        if (persistencia != null) {
            persistencia.guardar(this);
        }
    }

    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Equipo> getEquipos() { return equipos; }
    public List<Competencia> getCompetencias() { return competencias; }
    public List<Instalacion> getInstalaciones() { return instalaciones; }
    public List<Reserva> getReservas() { return reservas; }
    public List<SesionEntrenamiento> getSesiones() { return sesiones; }
    public List<Partido> getPartidos() { return partidos; }
    public List<Venta> getVentas() { return ventas; }
    public List<Producto> getProductos() { return productos; }
    public List<UbicacionInventario> getUbicaciones() { return ubicaciones; }
    public List<ReglaDeporte> getReglas() { return reglas; }
    public List<SolicitudCambioTurno> getSolicitudes() { return solicitudes; }

    public ReglaDeporte buscarRegla(Deporte deporte) {
        for (ReglaDeporte r : reglas) {
            if (r.getDeporte() == deporte) {
                return r;
            }
        }
        throw new ReglaNegocioException("No hay reglas definidas para " + deporte);
    }

    private Usuario buscarPorCodigo(int codigo) {
        for (Usuario u : usuarios) {
            if (u.getCodigo() == codigo) {
                return u;
            }
        }
        return null;
    }

    private Entrenador buscarEntrenador(int codigo) {
        Usuario u = buscarPorCodigo(codigo);
        if (u instanceof Entrenador) {
            return (Entrenador) u;
        }
        return null;
    }

    private Empleado buscarEmpleado(int codigo) {
        Usuario u = buscarPorCodigo(codigo);
        if (u instanceof Empleado) {
            return (Empleado) u;
        }
        return null;
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final es anterior a la inicial");
        }
    }

    private boolean enPeriodo(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private List<String> sinVacio(List<String> lineas) {
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException("No hay datos en el periodo seleccionado");
        }
        return lineas;
    }

    //  PERSONA 1: usuarios, deportes de conjunto y competencias

    public Usuario iniciarSesion(String login, String password) {
        if (login == null || password == null) {
            return null;
        }

        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equals(login) && usuario.validarPassword(password)) {
                return usuario;
            }
        }
        return null;
    }

    public Jugador inscribirJugador(int codigoSocio, String nombreEquipo, String posicion, int numeroCamiseta) {
        Usuario usuario = buscarPorCodigo(codigoSocio);
        if (!(usuario instanceof Socio)) {
            throw new ReglaNegocioException("El codigo no pertenece a un socio");
        }

        Equipo equipo = buscarEquipoPorNombre(nombreEquipo);
        if (equipo == null) {
            throw new ReglaNegocioException("El equipo no existe");
        }

        Socio socio = (Socio) usuario;
        Jugador jugador = socio.getJugadorDeportivo();

        if (usuario instanceof Jugador) {
            jugador = (Jugador) usuario;
        }

        if (jugador == null) {
            jugador = new Jugador(socio.getCodigo(), socio.getNombre(), socio.getApellido(),
                    socio.getLogin(), socio.getPasswordParaRegistro(), socio.getFechaNacimiento(),
                    posicion, numeroCamiseta);
            try {
                equipo.inscribirJugador(jugador);
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException(e.getMessage());
            }
            socio.setJugadorDeportivo(jugador);
        } else {
            if (!jugador.getPosicion().equals(posicion) || jugador.getNumeroCamiseta() != numeroCamiseta) {
                throw new ReglaNegocioException("La posicion o camiseta no coincide con el registro deportivo");
            }
            try {
                equipo.inscribirJugador(jugador);
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException(e.getMessage());
            }
        }

        guardar();
        return jugador;
    }

    public PartidoInterno registrarPartidoInterno(int codigoEntrenador, Equipo local, Equipo visitante,
            LocalDate fecha, int marcadorLocal, int marcadorVisitante,
            Map<Jugador, Map<String, Integer>> estadisticas) {
        Entrenador entrenador = buscarEntrenador(codigoEntrenador);
        if (entrenador == null) {
            throw new ReglaNegocioException("Se requiere un entrenador registrado");
        }
        if (local == null || visitante == null || !equipos.contains(local) ||
                !equipos.contains(visitante) || local == visitante ||
                local.getDeporte() != visitante.getDeporte() ||
                local.getCategoria() != visitante.getCategoria()) {
            throw new ReglaNegocioException("Equipos incompatibles");
        }
        int minimo = minimoJugadores(local.getDeporte());
        if (contarHabilitados(local) < minimo || contarHabilitados(visitante) < minimo) {
            throw new ReglaNegocioException("No hay suficientes jugadores habilitados");
        }

        validarMarcador(local.getDeporte(), marcadorLocal, marcadorVisitante);
        validarEstadisticas(estadisticas, local, visitante, null);

        PartidoInterno partido = new PartidoInterno(fecha, local.getDeporte(), local, visitante);
        partido.registrarResultado(marcadorLocal, marcadorVisitante);
        registrarEstadisticas(partido, estadisticas, null);
        partidos.add(partido);
        guardar();
        return partido;
    }

    public InscripcionEquipo inscribirEquipo(int codigoEntrenador, Equipo equipo,
            Competencia competencia, List<Jugador> jugadores) {
        if (buscarEntrenador(codigoEntrenador) == null) {
            throw new ReglaNegocioException("Se requiere un entrenador registrado");
        }
        if (equipo == null || competencia == null || !equipos.contains(equipo) ||
                !competencias.contains(competencia)) {
            throw new ReglaNegocioException("Equipo o competencia no registrado");
        }
        if (jugadores == null || jugadores.size() < competencia.getNominaMinima()) {
            throw new ReglaNegocioException("La nomina no cumple el minimo requerido");
        }
        if (contarHabilitados(equipo) < competencia.getNominaMinima()) {
            throw new ReglaNegocioException("No hay suficientes jugadores habilitados");
        }
        for (Jugador jugador : jugadores) {
            if (jugador == null || !equipo.getJugadores().contains(jugador) ||
                    jugador.estaSuspendido()) {
                throw new ReglaNegocioException("La nomina contiene un jugador no habilitado");
            }
        }
        for (int i = 0; i < jugadores.size(); i++) {
            for (int j = i + 1; j < jugadores.size(); j++) {
                if (jugadores.get(i) == jugadores.get(j)) {
                    throw new ReglaNegocioException("Hay jugadores repetidos en la nomina");
                }
            }
        }

        InscripcionEquipo inscripcion = competencia.inscribirEquipo(equipo);
        for (Jugador jugador : jugadores) {
            inscripcion.inscribirJugadorNomina(jugador);
        }
        guardar();
        return inscripcion;
    }

    public PartidoCompeticion registrarPartidoCompeticion(int codigoEntrenador, InscripcionEquipo inscripcion,
            String institucionRival, String escenario, LocalDate fecha, int marcadorPropio, int marcadorRival,
            Map<Jugador, Map<String, Integer>> estadisticas) {
        if (buscarEntrenador(codigoEntrenador) == null) {
            throw new ReglaNegocioException("Se requiere un entrenador registrado");
        }
        if (inscripcion == null || !competencias.contains(inscripcion.getCompetencia()) ||
                !equipos.contains(inscripcion.getEquipo()) ||
                inscripcion.getCompetencia().buscarInscripcion(inscripcion.getEquipo()) != inscripcion) {
            throw new ReglaNegocioException("La inscripcion no esta registrada");
        }
        Equipo equipo = inscripcion.getEquipo();
        if (!inscripcion.cumpleNominaMinima()) {
            throw new ReglaNegocioException("Falta la nomina oficial");
        }
        if (contarHabilitados(equipo) < minimoJugadores(equipo.getDeporte())) {
            throw new ReglaNegocioException("No hay jugadores habilitados suficientes");
        }

        validarMarcador(equipo.getDeporte(), marcadorPropio, marcadorRival);
        validarEstadisticas(estadisticas, equipo, null, inscripcion);

        PartidoCompeticion partido = new PartidoCompeticion(fecha, equipo, institucionRival, escenario);
        partido.registrarResultado(marcadorPropio, marcadorRival);
        registrarEstadisticas(partido, estadisticas, inscripcion);

        ReglaDeporte regla = buscarRegla(equipo.getDeporte());
        int puntosVictoria = regla.getPuntosVictoria();
        int puntosEmpate = regla.getPuntosEmpate();
        int puntosDerrota = regla.getPuntosDerrota();

        inscripcion.getCompetencia().agregarPartido(partido);
        inscripcion.registrarResultado(marcadorPropio, marcadorRival,
                puntosVictoria, puntosEmpate, puntosDerrota);
        partidos.add(partido);
        guardar();
        return partido;
    }

    
    private Equipo buscarEquipoPorNombre(String nombre) {
        for (Equipo equipo : equipos) {
            if (equipo.getNombre().equals(nombre)) {
                return equipo;
            }
        }
    
        return null;
    }
    
    private int minimoJugadores(Deporte deporte) {
        for (ReglaDeporte regla : reglas) {
            if (regla.getDeporte() == deporte) {
                return regla.getJugadoresMinimos();
            }
        }
    
        if (deporte == Deporte.FUTBOL) {
            return 7;
        }
    
        if (deporte == Deporte.BALONCESTO) {
            return 5;
        }
    
        if (deporte == Deporte.VOLEIBOL) {
            return 6;
        }
    
        throw new ReglaNegocioException("El deporte no es de conjunto");
    }
    
    private int contarHabilitados(Equipo equipo) {
        int total = 0;
    
        for (Jugador jugador : equipo.getJugadores()) {
            if (!jugador.estaSuspendido()) {
                total++;
            }
        }
    
        return total;
    }
    
    private int siguienteSancion = 1;
    
    public Sancion registrarSancion(Jugador jugador, TipoSancion tipo, LocalDate fecha) {
        if (jugador == null || (buscarPorCodigo(jugador.getCodigo()) != jugador
                && (!(buscarPorCodigo(jugador.getCodigo()) instanceof Socio)
                    || ((Socio) buscarPorCodigo(jugador.getCodigo())).getJugadorDeportivo() != jugador))) {
    
            throw new ReglaNegocioException("Jugador no registrado");
        }
    
        Sancion sancion = new Sancion(siguienteSancion, tipo, fecha, jugador, 1);
    
        siguienteSancion++;
        jugador.agregarSancion(sancion);
        guardar();
    
        return sancion;
    }
    
    public void revisarTarjetas(EstadisticaFutbol estadistica) {
        if (estadistica == null) {
            throw new ReglaNegocioException("No hay estadistica");
        }
    
        if (estadistica.getTarjetasRojas() > 0) {
            registrarSancion(estadistica.getJugador(), TipoSancion.ROJA_DIRECTA,
                    estadistica.getPartido().getFecha());
        } else if (estadistica.getTarjetasAmarillas() >= 2) {
            registrarSancion(estadistica.getJugador(), TipoSancion.DOBLE_AMARILLA,
                    estadistica.getPartido().getFecha());
        }
    }
    
    private void validarMarcador(Deporte deporte, int marcador1, int marcador2) {
        if (marcador1 < 0 || marcador2 < 0) {
            throw new ReglaNegocioException("El marcador no puede ser negativo");
        }
    
        ReglaDeporte regla = buscarRegla(deporte);
    
        if (!regla.isPermiteEmpate() && marcador1 == marcador2) {
            throw new ReglaNegocioException("Este deporte no permite empates");
        }
    }
    
    private void validarEstadisticas(Map<Jugador, Map<String, Integer>> estadisticas,
            Equipo local, Equipo visitante, InscripcionEquipo inscripcion) {
    
        if (estadisticas == null) {
            return;
        }
    
        for (Jugador jugador : estadisticas.keySet()) {
            if (jugador == null || jugador.estaSuspendido()) {
                throw new ReglaNegocioException("Jugador no habilitado en estadisticas");
            }
    
            if (inscripcion != null) {
                if (!inscripcion.contieneJugador(jugador)) {
                    throw new ReglaNegocioException("Jugador fuera de la nomina");
                }
            } else {
                if (!local.getJugadores().contains(jugador) &&
                        (visitante == null || !visitante.getJugadores().contains(jugador))) {
    
                    throw new ReglaNegocioException("Jugador fuera de los equipos participantes");
                }
            }
    
            Map<String, Integer> valores = estadisticas.get(jugador);
    
            if (valores == null) {
                throw new ReglaNegocioException("Faltan los valores de estadistica");
            }
    
            for (Integer valor : valores.values()) {
                if (valor == null || valor < 0) {
                    throw new ReglaNegocioException("Estadistica negativa o sin valor");
                }
            }
        }
    }
    
    private void registrarEstadisticas(Partido partido,
            Map<Jugador, Map<String, Integer>> estadisticas, InscripcionEquipo inscripcion) {
    
        if (estadisticas == null) {
            return;
        }
    
        for (Jugador jugador : estadisticas.keySet()) {
            if (jugador == null || (inscripcion != null && !inscripcion.contieneJugador(jugador))) {
                throw new ReglaNegocioException("Jugador fuera de la nomina");
            }
    
            Map<String, Integer> valores = estadisticas.get(jugador);
    
            if (valores == null) {
                throw new ReglaNegocioException("Faltan los valores de estadistica");
            }
    
            if (jugador.estaSuspendido()) {
                throw new ReglaNegocioException("Jugador suspendido");
            }
    
            if (partido.getDeporte() == Deporte.FUTBOL) {
                EstadisticaFutbol dato = new EstadisticaFutbol(jugador, partido);
    
                dato.setGoles(valorEstadistica(valores, "goles"));
                dato.setAsistencias(valorEstadistica(valores, "asistencias"));
                dato.setTarjetasAmarillas(valorEstadistica(valores, "tarjetasAmarillas"));
                dato.setTarjetasRojas(valorEstadistica(valores, "tarjetasRojas"));
    
                partido.agregarEstadistica(dato);
                revisarTarjetas(dato);
    
            } else if (partido.getDeporte() == Deporte.BALONCESTO) {
                EstadisticaBaloncesto dato = new EstadisticaBaloncesto(jugador, partido);
    
                dato.setPuntos(valorEstadistica(valores, "puntos"));
                dato.setRebotes(valorEstadistica(valores, "rebotes"));
    
                partido.agregarEstadistica(dato);
    
            } else if (partido.getDeporte() == Deporte.VOLEIBOL) {
                EstadisticaVoleibol dato = new EstadisticaVoleibol(jugador, partido);
    
                dato.setAces(valorEstadistica(valores, "aces"));
                dato.setBloqueos(valorEstadistica(valores, "bloqueos"));
    
                partido.agregarEstadistica(dato);
            }
        }
    }
    
    private int valorEstadistica(Map<String, Integer> valores, String nombre) {
        Integer valor = valores.get(nombre);
    
        if (valor == null) {
            return 0;
        }
    
        return valor;
    }
    
    public void crearEquipo(Administrador administrador, Equipo equipo) {
        validarAdministradorDeportivo(administrador);
    
        if (equipo == null || buscarEquipoPorNombre(equipo.getNombre()) != null) {
            throw new ReglaNegocioException("Equipo invalido o repetido");
        }
    
        equipos.add(equipo);
        guardar();
    }
    
    public void darDeBajaEquipo(Administrador administrador, Equipo equipo) {
        validarAdministradorDeportivo(administrador);
    
        if (equipo == null || !equipos.contains(equipo)) {
            throw new ReglaNegocioException("Equipo no registrado");
        }
    
        for (Jugador jugador : equipo.getJugadores()) {
            equipo.retirarJugador(jugador);
        }
    
        equipos.remove(equipo);
        guardar();
    }
    
    public void crearJugador(Administrador administrador, Jugador jugador) {
        validarAdministradorDeportivo(administrador);
    
        if (jugador == null || buscarPorCodigo(jugador.getCodigo()) != null) {
            throw new ReglaNegocioException("Jugador invalido o codigo repetido");
        }
    
        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equals(jugador.getLogin())) {
                throw new ReglaNegocioException("Login repetido");
            }
        }
    
        usuarios.add(jugador);
        guardar();
    }
    
    
    public void darDeBajaJugador(Administrador administrador, Jugador jugador) {
        validarAdministradorDeportivo(administrador);
    
        if (jugador == null || (buscarPorCodigo(jugador.getCodigo()) != jugador
                && (!(buscarPorCodigo(jugador.getCodigo()) instanceof Socio)
                    || ((Socio) buscarPorCodigo(jugador.getCodigo())).getJugadorDeportivo() != jugador))) {
    
            throw new ReglaNegocioException("Jugador no registrado");
        }
    
        for (Equipo equipo : equipos) {
            equipo.retirarJugador(jugador);
        }
    
        Usuario usuario = buscarPorCodigo(jugador.getCodigo());
    
        if (usuario instanceof Socio) {
            Socio socio = (Socio) usuario;
    
            if (socio.getJugadorDeportivo() == jugador) {
                socio.setJugadorDeportivo(null);
            }
        }
    
        usuarios.remove(jugador);
        guardar();
    }

    
    public void agregarCompetencia(Competencia competencia) {
        if (competencia == null || competencias.contains(competencia)) {
            throw new ReglaNegocioException("Competencia invalida");
        }
    
        competencias.add(competencia);
        guardar();
    }
    
    private void validarAdministradorDeportivo(Administrador administrador) {
        if (administrador == null || !usuarios.contains(administrador)) {
            throw new ReglaNegocioException("Se requiere un administrador registrado");
        }
    }
    
    public List<EstadisticaPartido> consultarEstadisticas(Jugador jugador) {
        List<EstadisticaPartido> resultado = new ArrayList<>();
    
        for (Partido partido : partidos) {
            for (EstadisticaPartido dato : partido.getEstadisticas()) {
                if (dato.getJugador() == jugador) {
                    resultado.add(dato);
                }
            }
        }
    
        return resultado;
    }
    
    public List<Sancion> consultarSancion(Jugador jugador) {
        return jugador.getSanciones();
    }
    
    public List<JugadorNomina> consultarNomina(InscripcionEquipo inscripcion) {
        return inscripcion.getNomina();
    }
    
    public List<InscripcionEquipo> consultarTablaPosiciones(Competencia competencia) {
        return competencia.tablaPosiciones();
    }
    
    public List<PartidoCompeticion> consultarResultados(Competencia competencia) {
        return competencia.getPartidos();
    }
    
    public List<EstadisticaPartido> consultarClasificaciones(Competencia competencia) {
        List<EstadisticaPartido> resultado = new ArrayList<>();
    
        for (PartidoCompeticion partido : competencia.getPartidos()) {
            resultado.addAll(partido.getEstadisticas());
        }
    
        return resultado;
    }
    
    public String consultarPerfil(Usuario usuario) {
        if (usuario == null || !usuarios.contains(usuario)) {
            throw new ReglaNegocioException("Usuario no registrado");
        }
    
        return usuario.consultarPerfil();
    }


    // =====================================================================
    //  PERSONA 3: individuales, tienda, cafeteria, inventario y ventas
    // =====================================================================

    
    private void validarCupoInstalaciones(Instalacion instalacion,
            LocalDate fecha, LocalTime inicio, LocalTime fin) {

        Deporte deporte = instalacion.getDisciplina();
        int maximo = buscarRegla(deporte).maximo();

        if (maximo <= 0) {
            throw new ReglaNegocioException(
                    "No hay instalaciones habilitadas para este deporte");
        }

        List<LocalTime> momentos = new ArrayList<>();
        momentos.add(inicio);

        for (Instalacion otra : instalaciones) {

            if (otra == instalacion || otra.getDisciplina() != deporte) {
                continue;
            }

            for (Reserva reserva : otra.getReservas()) {

                LocalTime comienzo = reserva.getHoraInicio();

                if (fecha.equals(reserva.getFecha()) &&
                        comienzo.isAfter(inicio) && comienzo.isBefore(fin)) {

                    momentos.add(comienzo);
                }
            }

            for (SesionEntrenamiento sesion : otra.getSesiones()) {

                LocalTime comienzo = sesion.getHora();

                if (fecha.equals(sesion.getFecha()) &&
                        comienzo.isAfter(inicio) && comienzo.isBefore(fin)) {

                    momentos.add(comienzo);
                }
            }
        }

        for (LocalTime momento : momentos) {

            int ocupadas = 0;

            for (Instalacion otra : instalaciones) {

                if (otra == instalacion || otra.getDisciplina() != deporte) {
                    continue;
                }

                boolean ocupada = false;

                for (Reserva reserva : otra.getReservas()) {

                    if (fecha.equals(reserva.getFecha()) &&
                            !momento.isBefore(reserva.getHoraInicio()) &&
                            momento.isBefore(reserva.getHoraFin())) {

                        ocupada = true;
                        break;
                    }
                }

                if (!ocupada) {

                    for (SesionEntrenamiento sesion : otra.getSesiones()) {

                        if (fecha.equals(sesion.getFecha()) &&
                                !momento.isBefore(sesion.getHora()) &&
                                momento.isBefore(sesion.getHoraFin())) {

                            ocupada = true;
                            break;
                        }
                    }
                }

                if (ocupada) {
                    ocupadas++;
                }
            }

            if (ocupadas >= maximo) {
                throw new ReglaNegocioException(
                        "Se alcanzo el maximo de instalaciones simultaneas de "
                        + deporte);
            }
        }
    }

    
    /** Indica si el socio compro una bebida caliente en la cafeteria durante la ultima hora. */
    private boolean tieneBebidaCaliente(Socio socio) {
        java.time.LocalDateTime limite = java.time.LocalDateTime.now().minusHours(1);
        for (Venta v : ventas) {
            if (v instanceof VentaCafeteria && v.getComprador() == socio && v.getFecha().isAfter(limite)) {
                for (LineaVenta linea : v.getLineas()) {
                    if (linea.getProducto() instanceof Bebida && ((Bebida) linea.getProducto()).isCaliente()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Reserva reservar(int codigoSocio, Instalacion instalacion, LocalDate fecha,
            LocalTime horaInicio, LocalTime horaFin, int numeroJugadores, Modalidad modalidad) {

        Usuario usuario = buscarPorCodigo(codigoSocio);

        if (!(usuario instanceof Socio)) {
            throw new ReglaNegocioException("El codigo no corresponde a un socio");
        }

        if (instalacion == null || !instalaciones.contains(instalacion) ||
                fecha == null || horaInicio == null || horaFin == null) {
            throw new ReglaNegocioException("Datos de reserva invalidos");
        }

        if (!horaInicio.isBefore(horaFin)) {
            throw new ReglaNegocioException("El horario es invalido");
        }

        if (!instalacion.admite(numeroJugadores, modalidad)) {
            throw new ReglaNegocioException(
                    "Cantidad de jugadores o modalidad invalida");
        }

        long segundos = java.time.Duration.between(
                horaInicio, horaFin).getSeconds();

        int duracionMinutos = (int) ((segundos + 59) / 60);

        if (!instalacion.disponible(fecha, horaInicio, duracionMinutos)) {
            throw new ReglaNegocioException("La instalacion esta ocupada");
        }

        validarCupoInstalaciones(instalacion, fecha, horaInicio, horaFin);

        boolean esTechadaDeRiesgo = instalacion.isTechada()
                && (instalacion.getDisciplina() == Deporte.PADEL
                || instalacion.getDisciplina() == Deporte.TENIS_DE_MESA);
        if (esTechadaDeRiesgo && tieneBebidaCaliente((Socio) usuario)) {
            throw new ReglaNegocioException("No se puede usar esta instalacion techada con una bebida caliente");
        }

        Reserva reserva = new Reserva((Socio) usuario, instalacion, fecha,
                horaInicio, horaFin, numeroJugadores, modalidad);

        instalacion.agregarReserva(reserva);
        reservas.add(reserva);

        guardar();

        return reserva;
    }

  
    
    public PartidoPractica programarPractica(List<InscripcionDisciplina> inscripciones,
            Modalidad modalidad, LocalDate fecha) {

        if (inscripciones == null || inscripciones.isEmpty()) {
            throw new ReglaNegocioException("No hay participantes");
        }

        if (fecha == null || modalidad == null) {
            throw new ReglaNegocioException("Faltan datos del partido");
        }

        int nivelMinimo = Nivel.AVANZADO.ordinal();
        int nivelMaximo = Nivel.PRINCIPIANTE.ordinal();

        boolean participaEntrenador = false;

        for (InscripcionDisciplina inscripcion : inscripciones) {

            if (inscripcion == null) {
                throw new ReglaNegocioException("Inscripcion inexistente");
            }

            Socio socio = inscripcion.getSocio();

            if (socio == null || !usuarios.contains(socio)) {
                throw new ReglaNegocioException("El socio no esta registrado");
            }

            if (!socio.getInscripciones().contains(inscripcion)) {
                throw new ReglaNegocioException(
                        "La inscripcion no pertenece al socio");
            }

            Nivel nivel = inscripcion.getNivel();

            if (nivel == null) {
                throw new ReglaNegocioException(
                        "El participante no tiene nivel asignado");
            }

            int numeroNivel = nivel.ordinal();

            if (numeroNivel < nivelMinimo) {
                nivelMinimo = numeroNivel;
            }

            if (numeroNivel > nivelMaximo) {
                nivelMaximo = numeroNivel;
            }

            Entrenador entrenador = socio.getEntrenadorAsociado();

            if (entrenador != null && usuarios.contains(entrenador)) {
                participaEntrenador = true;
            }
        }

        if (nivelMaximo - nivelMinimo > 1 && !participaEntrenador) {
            throw new ReglaNegocioException(
                    "Los participantes tienen niveles incompatibles");
        }

        PartidoPractica partido;

        try {
            partido = new PartidoPractica(inscripciones, modalidad, fecha);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(e.getMessage());
        }

        partidos.add(partido);

        guardar();

        return partido;
    }


    
    public void vincularSocioEntrenador(int codigoSocio, int codigoEntrenador) {

        Usuario usuario = buscarPorCodigo(codigoSocio);
        Entrenador entrenador = buscarEntrenador(codigoEntrenador);

        if (!(usuario instanceof Socio) || entrenador == null) {
            throw new ReglaNegocioException(
                    "Socio o entrenador no registrado");
        }

        Socio socio = (Socio) usuario;

        for (Usuario otro : usuarios) {

            if (otro instanceof Socio) {

                Socio otroSocio = (Socio) otro;

                if (otroSocio != socio &&
                        otroSocio.getEntrenadorAsociado() == entrenador) {

                    throw new ReglaNegocioException(
                            "El entrenador ya esta asociado a otro socio");
                }
            }
        }

        if (socio.getEntrenadorAsociado() != null &&
                socio.getEntrenadorAsociado() != entrenador) {

            throw new ReglaNegocioException(
                    "El socio ya tiene otro entrenador asociado");
        }

        socio.asociarEntrenador(entrenador);

        guardar();
    }

    
    private int valorPractica(Map<String, Integer> valores,
            String nombre, boolean obligatorio) {

        Integer dato = valores.get(nombre);

        if (dato == null) {

            if (obligatorio) {
                throw new ReglaNegocioException(
                        "Falta la estadistica: " + nombre);
            }

            return 0;
        }

        if (dato < 0) {
            throw new ReglaNegocioException(
                    "La estadistica no puede ser negativa: " + nombre);
        }

        return dato;
    }


    public void registrarPractica(PartidoPractica partido,
            Map<InscripcionDisciplina, Map<String, Integer>> estadisticas) {

        if (partido == null || !partidos.contains(partido)) {
            throw new ReglaNegocioException("El partido no esta registrado");
        }

        if (partido.resuelto() || !partido.getEstadisticasPractica().isEmpty()) {
            throw new ReglaNegocioException(
                    "El partido ya tiene estadisticas o resultado");
        }

        List<InscripcionDisciplina> participantes = partido.getParticipantes();

        if (estadisticas == null || estadisticas.size() != participantes.size()) {
            throw new ReglaNegocioException(
                    "Se necesitan las estadisticas de todos los participantes");
        }

        boolean esMesa = partido.getDeporte() == Deporte.TENIS_DE_MESA;

        List<EstadisticaPractica> nuevas = new ArrayList<>();

        int mitad = participantes.size() / 2;
        int setsPrimeraPareja = -1;
        int setsSegundaPareja = -1;

        for (int i = 0; i < participantes.size(); i++) {

            InscripcionDisciplina inscripcion = participantes.get(i);
            Map<String, Integer> valores = estadisticas.get(inscripcion);

            if (valores == null) {
                throw new ReglaNegocioException(
                        "Faltan estadisticas de un participante");
            }

            int sets = valorPractica(valores, "sets", true);
            int games = valorPractica(valores, "games", !esMesa);
            int aces = valorPractica(valores, "aces", !esMesa);
            int errores = valorPractica(valores, "erroresNoForzados", !esMesa);
            int puntos = valorPractica(valores, "puntosTotales", esMesa);

            if (i < mitad) {

                if (setsPrimeraPareja == -1) {
                    setsPrimeraPareja = sets;
                } else if (setsPrimeraPareja != sets) {
                    throw new ReglaNegocioException(
                            "Los integrantes de una pareja deben tener los mismos sets ganados");
                }

            } else {

                if (setsSegundaPareja == -1) {
                    setsSegundaPareja = sets;
                } else if (setsSegundaPareja != sets) {
                    throw new ReglaNegocioException(
                            "Los integrantes de una pareja deben tener los mismos sets ganados");
                }
            }

            EstadisticaPractica nueva = new EstadisticaPractica(
                    inscripcion, sets, games, aces, errores, puntos);

            nuevas.add(nueva);
        }

        if (setsPrimeraPareja == setsSegundaPareja) {
            throw new ReglaNegocioException(
                    "Debe existir un ganador por sets");
        }

        boolean ganaPrimeraPareja = setsPrimeraPareja > setsSegundaPareja;

        for (EstadisticaPractica nueva : nuevas) {
            partido.agregarEstadisticaPractica(nueva);
        }

        partido.registrarResultado();

        for (int i = 0; i < participantes.size(); i++) {

            InscripcionDisciplina inscripcion = participantes.get(i);

            boolean gana = (i < mitad && ganaPrimeraPareja)
                    || (i >= mitad && !ganaPrimeraPareja);

            int nuevoPuntaje = inscripcion.getPuntajeRanking();

            if (gana) {
                nuevoPuntaje += 10;
            } else {
                nuevoPuntaje -= 10;

                if (nuevoPuntaje < 0) {
                    nuevoPuntaje = 0;
                }
            }

            inscripcion.actualizarRanking(
                    nuevoPuntaje, partido.getFecha());
        }

        guardar();
    }


    //  PERSONA 2. B:tienda, cafeteria, inventario y ventas
    
    private static final String UBICACION_TIENDA = "Tienda";
    private UbicacionInventario buscarUbicacion(String nombre) {
        for (UbicacionInventario u : ubicaciones) {
            if (u.getNombre().equalsIgnoreCase(nombre)) {
                return u;
            }
        }
        return null;
    }
    
    private void exigirAdministrador(int codigo, String accion) {
        if (!(buscarPorCodigo(codigo) instanceof Administrador)) {
            throw new ReglaNegocioException("Solo el administrador puede " + accion);
        }
    }
    
    
    private boolean enTechada(Socio socio) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        for (Instalacion i : instalaciones) {
            boolean esDeRiesgo = i.isTechada()
                    && (i.getDisciplina() == Deporte.PADEL || i.getDisciplina() == Deporte.TENIS_DE_MESA);
            if (esDeRiesgo) {
                for (Reserva r : i.getReservas()) {
                    if (r.getSocio() == socio && r.getFecha().equals(hoy)
                            && !ahora.isBefore(r.getHoraInicio()) && ahora.isBefore(r.getHoraFin())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private double descuento(Usuario comprador, String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return 0;
        }
        for (Usuario u : usuarios) {
            if (u instanceof Empleado && ((Empleado) u).getCodigoDescuento().equals(codigo)) {
                return (u == comprador) ? 0.15 : 0.08;
            }
        }
        throw new ReglaNegocioException("El codigo de descuento no existe");
    }
    
    public VentaTienda comprarTienda(int codigoComprador, Map<ArticuloTienda, Integer> articulos,
            String codigoDescuento) {
        Usuario comprador = buscarPorCodigo(codigoComprador);
        if (!(comprador instanceof Socio) && !(comprador instanceof Empleado)) {
            throw new ReglaNegocioException("Solo un socio o un empleado puede comprar en la tienda");
        }
        if (articulos == null || articulos.isEmpty()) {
            throw new ReglaNegocioException("La compra no tiene articulos");
        }
        UbicacionInventario tienda = buscarUbicacion(UBICACION_TIENDA);
        if (tienda == null) {
            throw new ReglaNegocioException("No existe la ubicacion de inventario de la tienda");
        }

        for (Map.Entry<ArticuloTienda, Integer> e : articulos.entrySet()) {
            if (e.getValue() <= 0) {
                throw new ReglaNegocioException("La cantidad de " + e.getKey().getNombre() + " debe ser mayor que cero");
            }
            ExistenciaInventario existencia = tienda.buscarExistencia(e.getKey());
            if (existencia == null || !existencia.hayStock(e.getValue())) {
                throw new ReglaNegocioException("No hay stock suficiente de " + e.getKey().getNombre());
            }
        }

        double porcentaje = descuento(comprador, codigoDescuento);
        VentaTienda venta = new VentaTienda(comprador, articulos, porcentaje);
        if (porcentaje > 0) {
            venta.setCodigoDescuentoUsado(codigoDescuento);
        }

        for (Map.Entry<ArticuloTienda, Integer> e : articulos.entrySet()) {
            tienda.buscarExistencia(e.getKey()).descontar(e.getValue());
        }
        if (comprador instanceof Socio) {
            ((Socio) comprador).actualizarPuntos(venta.getPuntosGanados(), 0);
        }
        ventas.add(venta);
        guardar();
        return venta;
    }
    
    
    public VentaCafeteria comprarCafeteria(int codigoSocio, Map<Producto, Integer> productosComprados,
            double propina, int puntosAUsar) {
        Usuario usuario = buscarPorCodigo(codigoSocio);
        if (!(usuario instanceof Socio)) {
            throw new ReglaNegocioException("Solo un socio puede comprar en la cafeteria");
        }
        Socio socio = (Socio) usuario;
        if (productosComprados == null || productosComprados.isEmpty()) {
            throw new ReglaNegocioException("La compra no tiene productos");
        }
        if (propina < 0 || propina > 1) {
            throw new ReglaNegocioException("La propina debe estar entre 0% y 100%");
        }
        if (puntosAUsar < 0) {
            throw new ReglaNegocioException("Los puntos a usar no pueden ser negativos");
        }

        boolean estaEnTechada = enTechada(socio);
        double subtotal = 0;
        for (Map.Entry<Producto, Integer> e : productosComprados.entrySet()) {
            Producto producto = e.getKey();
            if (e.getValue() <= 0) {
                throw new ReglaNegocioException("La cantidad de " + producto.getNombre() + " debe ser mayor que cero");
            }
            if (!productos.contains(producto)) {
                throw new ReglaNegocioException(producto.getNombre() + " no esta registrado en el club");
            }
            if (producto instanceof ArticuloTienda) {
                throw new ReglaNegocioException(producto.getNombre() + " es de la tienda, no de la cafeteria");
            }
            if (!producto.permitido(estaEnTechada)) {
                throw new ReglaNegocioException("No se sirven bebidas calientes a un socio en una cancha techada de padel o tenis de mesa");
            }
            subtotal += producto.getPrecio() * e.getValue();
        }
        
        if (!socio.tienePuntos(puntosAUsar)) {
            throw new ReglaNegocioException("El socio no tiene suficientes puntos de fidelidad");
        }
        if (puntosAUsar > subtotal) {
            throw new ReglaNegocioException("No se pueden usar mas puntos que el valor de la compra");
        }

        VentaCafeteria venta = new VentaCafeteria(socio, productosComprados, propina, puntosAUsar);
        socio.actualizarPuntos(venta.getPuntosGanados(), puntosAUsar);
        ventas.add(venta);
        guardar();
        return venta;
    }
    
    
    public List<Venta> consultarHistorialCompras(int codigoSocio) {
        Usuario usuario = buscarPorCodigo(codigoSocio);
        if (!(usuario instanceof Socio)) {
            throw new ReglaNegocioException("El usuario no es un socio");
        }
        List<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.getComprador() == usuario) {
                resultado.add(v);
            }
        }
        if (resultado.isEmpty()) {
            throw new ReglaNegocioException("El socio no tiene compras registradas");
        }
        return resultado;
    }
    
    public List<String> consultarInventario(int codigoAdministrador, String nombreUbicacion) {
        exigirAdministrador(codigoAdministrador, "consultar el inventario");
        List<String> lineas = new ArrayList<>();
        for (UbicacionInventario u : ubicaciones) {
            if (nombreUbicacion == null || u.getNombre().equalsIgnoreCase(nombreUbicacion)) {
                for (ExistenciaInventario e : u.getExistencias()) {
                    lineas.add(u.getNombre() + " | " + e.getProducto().getNombre() + " | " + e.getCantidad());
                }
            }
        }
        if (nombreUbicacion != null && buscarUbicacion(nombreUbicacion) == null) {
            throw new ReglaNegocioException("No se encontro la ubicacion " + nombreUbicacion);
        }
        return sinVacio(lineas);
    }
    
    public void reabastecer(int codigoAdministrador, ArticuloTienda articulo, int cantidad) {
        exigirAdministrador(codigoAdministrador, "reabastecer el inventario");
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad a reabastecer debe ser mayor que cero");
        }
        if (!productos.contains(articulo)) {
            throw new ReglaNegocioException(articulo.getNombre() + " no esta registrado en el club");
        }
        UbicacionInventario tienda = buscarUbicacion(UBICACION_TIENDA);
        if (tienda == null) {
            throw new ReglaNegocioException("No existe la ubicacion de inventario de la tienda");
        }
        tienda.agregar(articulo, cantidad);
        guardar();
    }
    
    public void moverArticulo(int codigoAdministrador, Producto producto, String origen, String destino,
            int cantidad) {
        exigirAdministrador(codigoAdministrador, "mover articulos entre ubicaciones");
        UbicacionInventario desde = buscarUbicacion(origen);
        UbicacionInventario hacia = buscarUbicacion(destino);
        if (desde == null || hacia == null) {
            throw new ReglaNegocioException("La ubicacion de origen o de destino no existe");
        }
        if (desde == hacia) {
            throw new ReglaNegocioException("El origen y el destino son la misma ubicacion");
        }
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad a mover debe ser mayor que cero");
        }
        ExistenciaInventario existencia = desde.buscarExistencia(producto);
        if (existencia == null || !existencia.hayStock(cantidad)) {
            throw new ReglaNegocioException("La ubicacion " + origen + " no tiene suficientes unidades de "
                    + producto.getNombre());
        }
        existencia.descontar(cantidad);
        hacia.agregar(producto, cantidad);
        guardar();
    }
    
    
    public SugerenciaProducto sugerirProducto(int codigoEmpleado, String nombre, String descripcion) {
        Empleado empleado = buscarEmpleado(codigoEmpleado);
        if (empleado == null) {
            throw new ReglaNegocioException("Solo un empleado puede sugerir productos");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("La sugerencia debe tener un nombre");
        }
        SugerenciaProducto sugerencia = new SugerenciaProducto(empleado, nombre, descripcion);
        guardar();
        return sugerencia;
    }
    
    
    public void aprobarSugerencia(int codigoAdministrador, SugerenciaProducto sugerencia) {
        evaluarSugerencia(codigoAdministrador, sugerencia, true);
    }
    
    public void rechazarSugerencia(int codigoAdministrador, SugerenciaProducto sugerencia) {
        evaluarSugerencia(codigoAdministrador, sugerencia, false);
    }

    private void evaluarSugerencia(int codigoAdministrador, SugerenciaProducto sugerencia, boolean aprobar) {
        exigirAdministrador(codigoAdministrador, "evaluar sugerencias de productos");
        if (sugerencia.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("La sugerencia ya fue evaluada");
        }
        if (aprobar) {
            sugerencia.aprobar();
        } else {
            sugerencia.rechazar();
        }
        guardar();
    }
    
    public List<SugerenciaProducto> consultarSugerencias(int codigoAdministrador) {
        exigirAdministrador(codigoAdministrador, "consultar las sugerencias de productos");
        List<SugerenciaProducto> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Empleado) {
                resultado.addAll(((Empleado) u).getSugerencias());
            }
        }
        return resultado;
    }
    
    
    public List<String> reporteVentas(int codigoAdministrador, LocalDate desde, LocalDate hasta) {
        exigirAdministrador(codigoAdministrador, "generar reportes de ventas");
        validarPeriodo(desde, hasta);

        int cantidadTienda = 0;
        double totalTienda = 0;
        int cantidadCafeteria = 0;
        double totalCafeteria = 0;
        for (Venta v : ventas) {
            if (enPeriodo(v.getFecha().toLocalDate(), desde, hasta)) {
                if (v instanceof VentaTienda) {
                    cantidadTienda++;
                    totalTienda += v.getTotal();
                } else {
                    cantidadCafeteria++;
                    totalCafeteria += v.getTotal();
                }
            }
        }
        int cantidadMensualidades = 0;
        double totalMensualidades = 0;
        for (Usuario u : usuarios) {
            if (u instanceof Socio) {
                for (Mensualidad m : ((Socio) u).getMensualidades()) {
                    if (enPeriodo(m.getFechaPago(), desde, hasta)) {
                        cantidadMensualidades++;
                        totalMensualidades += m.getValor();
                    }
                }
            }
        }

        List<String> lineas = new ArrayList<>();
        if (cantidadTienda + cantidadCafeteria + cantidadMensualidades > 0) {
            lineas.add("Tienda | ventas: " + cantidadTienda + " | total: " + totalTienda);
            lineas.add("Cafeteria | ventas: " + cantidadCafeteria + " | total: " + totalCafeteria);
            lineas.add("Mensualidades | pagos: " + cantidadMensualidades + " | total: " + totalMensualidades);
            lineas.add("TOTAL | " + (totalTienda + totalCafeteria + totalMensualidades));
        }
        return sinVacio(lineas);
    }

    //  PERSONA 2: entrenamientos

    public SesionEntrenamiento organizarSesion(int codigoEntrenador, Instalacion instalacion,
            LocalDate fecha, LocalTime hora, int duracionMinutos, List<Socio> convocados) {
        Entrenador entrenador = buscarEntrenador(codigoEntrenador);
        if (entrenador == null) {
            throw new ReglaNegocioException("Solo un entrenador puede organizar sesiones");
        }
        if (duracionMinutos <= 0 || hora.plusMinutes(duracionMinutos).isBefore(hora)) {
            throw new ReglaNegocioException("Duracion invalida: la sesion debe terminar el mismo dia");
        }
        if (convocados == null || convocados.isEmpty()) {
            throw new ReglaNegocioException("Debe convocar al menos un socio");
        }
        if (!instalacion.disponible(fecha, hora, duracionMinutos)) {
            throw new ReglaNegocioException("La instalacion esta ocupada en ese horario");
        }
        

        Deporte disciplina = instalacion.getDisciplina();

        if (disciplina == Deporte.TENIS ||
                disciplina == Deporte.PADEL ||
                disciplina == Deporte.TENIS_DE_MESA) {

            validarCupoInstalaciones(instalacion, fecha, hora,
                    hora.plusMinutes(duracionMinutos));
        }

        SesionEntrenamiento sesion = new SesionEntrenamiento(entrenador, instalacion, fecha, hora, duracionMinutos);
        for (Socio socio : convocados) {
            new Convocatoria(sesion, socio);
        }
        sesiones.add(sesion);
        guardar();
        return sesion;
    }

    public void registrarAsistencia(int codigoEntrenador, SesionEntrenamiento sesion, Socio socio,
            Asistencia asistencia, int nota, String comentario) {
        Entrenador entrenador = buscarEntrenador(codigoEntrenador);
        if (entrenador == null) {
            throw new ReglaNegocioException("Solo un entrenador puede registrar asistencia");
        }
        if (sesion.getEntrenador() != entrenador) {
            throw new ReglaNegocioException("Solo el entrenador de la sesion puede registrar la asistencia");
        }
        if (!sesion.haTerminado()) {
            throw new ReglaNegocioException("La sesion aun no ha terminado");
        }
        Convocatoria convocatoria = sesion.buscarConvocatoria(socio);
        if (convocatoria == null) {
            throw new ReglaNegocioException("El socio no fue convocado a esta sesion");
        }
        convocatoria.registrar(asistencia, nota, comentario);
        guardar();
    }

    public List<Convocatoria> consultarConvocatorias(Socio socio) {
        List<Convocatoria> resultado = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            Convocatoria c = s.buscarConvocatoria(socio);
            if (c != null) {
                resultado.add(c);
            }
        }
        if (resultado.isEmpty()) {
            throw new ReglaNegocioException("No existe ninguna convocatoria para este socio");
        }
        return resultado;
    }
 

    public Convocatoria consultarAsistencia(Socio socio, SesionEntrenamiento sesion) {
        Convocatoria convocatoria = sesion.buscarConvocatoria(socio);
        if (convocatoria == null) {
            throw new ReglaNegocioException("El socio no fue convocado a esta sesion");
        }
        if (!sesion.haTerminado()) {
            throw new ReglaNegocioException("La sesion esta en proceso: aun no hay asistencia");
        }
        if (convocatoria.getAsistencia() == null) {
            throw new ReglaNegocioException("La asistencia de esta sesion aun no se ha registrado");
        }
        return convocatoria;
    }

    public List<Convocatoria> consultarDesempenoJugador(int codigoEmpleado, Socio socio) {
        Empleado empleado = buscarEmpleado(codigoEmpleado);
        if (empleado == null) {
            throw new ReglaNegocioException("Solo un entrenador o fisioterapeuta puede consultar el desempeno");
        }
        List<Convocatoria> resultado = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            Convocatoria c = s.buscarConvocatoria(socio);
            if (c != null && c.getAsistencia() != null) {
                resultado.add(c);
            }
        }
        if (resultado.isEmpty()) {
            throw new ReglaNegocioException("El jugador no tiene asistencias registradas");
        }
        return resultado;
    }

    //  PERSONA 2: turnos

    public SolicitudCambioTurno solicitarCambioTurno(int codigoEmpleado, Turno turno, String motivo,
            String nuevoDiaSemana, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
        Empleado empleado = buscarEmpleado(codigoEmpleado);
        if (empleado == null) {
            throw new ReglaNegocioException("Solo un entrenador o fisioterapeuta puede pedir cambio de turno");
        }
        if (turno.getEmpleado() != empleado) {
            throw new ReglaNegocioException("El turno no pertenece a este empleado");
        }
        if (!nuevaHoraInicio.isBefore(nuevaHoraFin)) {
            throw new ReglaNegocioException("El horario nuevo es invalido");
        }
        SolicitudCambioTurno solicitud = new SolicitudCambioTurno(empleado, turno, motivo,
                nuevoDiaSemana, nuevaHoraInicio, nuevaHoraFin);
        solicitudes.add(solicitud);
        guardar();
        return solicitud;
    }

    public void aprobarCambio(SolicitudCambioTurno solicitud) {
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("La solicitud ya fue evaluada");
        }
        if (cumpleMinimos(solicitud)) {
            solicitud.aprobar();
            solicitud.getTurno().cambiar(solicitud);
            guardar();
        } else {
            solicitud.rechazar();
            guardar();
            throw new ReglaNegocioException(
                    "El cambio dejaria el horario por debajo del minimo (2 entrenadores y 1 fisioterapeuta)");
        }
    }

  
    public void rechazarCambio(SolicitudCambioTurno solicitud, String observacion) {
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("La solicitud ya fue evaluada");
        }
        solicitud.setObservacionEvaluacion(observacion);
        solicitud.rechazar();
        guardar();
    }

    public List<Turno> consultarTurnos(int codigoAdministrador) {
        if (!(buscarPorCodigo(codigoAdministrador) instanceof Administrador)) {
            throw new ReglaNegocioException("Solo el administrador puede consultar los turnos");
        }
        List<Turno> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Empleado) {
                resultado.addAll(((Empleado) u).getTurnos());
            }
        }
        return resultado;
    }
    
    private boolean cumpleMinimos(SolicitudCambioTurno solicitud) {
        Turno turno = solicitud.getTurno();
        boolean esEntrenador = turno.getEmpleado() instanceof Entrenador;
        int minimo = esEntrenador ? 2 : 1;

        LocalTime hora = turno.getHoraInicio();
        while (hora.isBefore(turno.getHoraFin())) {
            int cubiertos = 0;
            for (Usuario u : usuarios) {
                if (u instanceof Empleado && (u instanceof Entrenador) == esEntrenador) {
                    for (Turno t : ((Empleado) u).getTurnos()) {
                        if (t == turno) {
                            if (solicitud.cubreNuevo(turno.getDiaSemana(), hora)) {
                                cubiertos++;
                            }
                        } else if (t.cubre(turno.getDiaSemana(), hora)) {
                            cubiertos++;
                        }
                    }
                }
            }
            if (cubiertos < minimo) {
                return false;
            }
            LocalTime siguiente = hora.plusMinutes(30);
            if (siguiente.isBefore(hora)) {
                break; 
            }
            hora = siguiente;
        }
        return true;
    }


    //  PERSONA 2: reportes deportivos e historiales

    public List<String> reporteAsistencia(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            if (enPeriodo(s.getFecha(), desde, hasta) && s.haTerminado()) {
                int asistio = 0;
                int falto = 0;
                int tarde = 0;
                int sinRegistro = 0;
                for (Convocatoria c : s.getConvocatorias()) {
                    if (c.getAsistencia() == null) {
                        sinRegistro++;
                    } else if (c.getAsistencia() == Asistencia.ASISTIO) {
                        asistio++;
                    } else if (c.getAsistencia() == Asistencia.FALTO) {
                        falto++;
                    } else {
                        tarde++;
                    }
                }
                lineas.add(s.getFecha() + " " + s.getHora() + " | " + s.getInstalacion().getNombre()
                        + " | asistio: " + asistio + " | falto: " + falto + " | llego tarde: " + tarde
                        + " | sin registrar: " + sinRegistro);
            }
        }
        return sinVacio(lineas);
    }

    public List<String> reporteResultados(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Partido p : partidos) {
            if (enPeriodo(p.getFecha(), desde, hasta)) {
                String linea = p.getFecha() + " | " + p.getDeporte() + " | " + p.getClass().getSimpleName();
                if (p instanceof PartidoEquipo) {
                    PartidoEquipo pe = (PartidoEquipo) p;
                    linea += " | marcador: " + pe.getMarcadorEquipo1() + " - " + pe.getMarcadorEquipo2();
                } else if (p instanceof PartidoPractica) {
                    linea += " | resultado registrado: " + (((PartidoPractica) p).resuelto() ? "si" : "no");
                }
                lineas.add(linea);
            }
        }
        return sinVacio(lineas);
    }

    public List<String> reporteEvolucionRanking(Deporte deporte, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Socio) {
                Socio socio = (Socio) u;
                for (InscripcionDisciplina inscripcion : socio.getInscripciones()) {
                    if (inscripcion.getDisciplina() == deporte) {
                        for (RegistroRanking r : inscripcion.getHistorial()) {
                            if (enPeriodo(r.getFecha(), desde, hasta)) {
                                lineas.add(r.getFecha() + " | " + socio.getNombre() + " | "
                                        + r.getPuntajeAnterior() + " -> " + r.getPuntajeNuevo());
                            }
                        }
                    }
                }
            }
        }
        return sinVacio(lineas);
    }


    public List<String> historialReservas(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Reserva r : reservas) {
            if (enPeriodo(r.getFecha(), desde, hasta)) {
                lineas.add(r.getFecha() + " " + r.getHoraInicio() + "-" + r.getHoraFin() + " | "
                        + r.getInstalacion().getNombre() + " | " + r.getSocio().getNombre());
            }
        }
        return sinVacio(lineas);
    }

    public List<String> historialPartidos(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Partido p : partidos) {
            if (enPeriodo(p.getFecha(), desde, hasta)) {
                lineas.add(p.getFecha() + " | " + p.getDeporte() + " | " + p.getClass().getSimpleName());
            }
        }
        return sinVacio(lineas);
    }

    public List<String> historialSesiones(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            if (enPeriodo(s.getFecha(), desde, hasta)) {
                lineas.add(s.getFecha() + " " + s.getHora() + " | " + s.getInstalacion().getNombre()
                        + " | " + s.getEntrenador().getNombre()
                        + " | convocados: " + s.getConvocatorias().size());
            }
        }
        return sinVacio(lineas);
    }
}

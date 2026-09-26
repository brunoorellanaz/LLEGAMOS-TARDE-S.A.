import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/** Controlador principal del dominio del sistema de buses. */
public class gestionbuses {
    private ArrayList<Buses> listaBuses;
    private ArrayList<Pasajeros> listaPasajeros;
    private ArrayList<Viajes> listaViajes;
    private int contadorViajes = 1;
    
    private String archivo;
    
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public gestionbuses() {
        listaBuses = new ArrayList<>();
        listaPasajeros = new ArrayList<>();
        listaViajes = new ArrayList<>();
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        Buses bus1 = new Buses(1, 40);
        Buses bus2 = new Buses(2, 40);
        Buses bus3 = new Buses(3, 50);
        listaBuses.add(bus1);
        listaBuses.add(bus2);
        listaBuses.add(bus3);

        Viajes viajeEjemplo = new Viajes(
                contadorViajes++,
                "Valparaiso",
                "Santiago",
                3000,
                5000,
                LocalDateTime.now().plusDays(1));
        
        Buses busEj = new Buses(bus1.getIdBus(), bus1.getCapacity());
        viajeEjemplo.agregarBus(busEj);
        
        try {
            Pasajeros pasajeroEj = busEj.agregarPasajero(1, 30, "Pasajero Ejemplo");
            listaPasajeros.add(pasajeroEj);
        } catch (CapacidadExcedidaException error){
            System.out.println("No se pudo cargar el pasajero de ejemplo: " + error.getMessage());
        }
        
        listaViajes.add(viajeEjemplo);
    }

    public void setContadorViajes(int contadorViajes) { this.contadorViajes = contadorViajes; }
    public int getContadorViajes() { return contadorViajes; }
    public ArrayList<Buses> getListaBuses() { return listaBuses; }
    public ArrayList<Pasajeros> getListaPasajeros() { return listaPasajeros; }

    // ==================== PERSISTENCIA ====================
    // Este módulo se encarga de cargar y guardar los datos del sistema.
    public void cargarDesdeArchivo(String ruta) {
        try {
            CargarDatos.cargar(this, ruta);
            System.out.println("Datos cargados correctamente desde " + ruta);
        } catch (Exception e) {
            System.out.println("No se pudo cargar el archivo. Se usarán los datos iniciales: " + e.getMessage());
            limpiarDatos();
            cargarDatosIniciales();
        }
    }

    public void guardarEnArchivo(String ruta) {
        try {
            GuardarDatos.guardar(this, ruta);
            System.out.println("Datos guardados correctamente en " + ruta);
        } catch (Exception e) {
            System.out.println("No se pudieron guardar los datos: " + e.getMessage());
        }
    }
    
    // Archivo utilizado para guardar automáticamente los cambios realizados.
    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }
    private void guardarCambios() {
        if (archivo != null && !archivo.trim().isEmpty()) {
            guardarEnArchivo(archivo);
        }
    }
    public void limpiarDatos() {
        listaBuses.clear();
        listaPasajeros.clear();
        listaViajes.clear();
        contadorViajes = 1;
    }
    
    
    

    // Métodos auxiliares usados exclusivamente por la carga de datos; no realizan guardado automático.
    public void agregarBusInicial(int id, int capacidad) {
        listaBuses.add(new Buses(id, capacidad));
    }
    public void agregarViajeInicial(Viajes viaje) {
        listaViajes.add(viaje);
        if (viaje.getIdViaje() >= contadorViajes) contadorViajes = viaje.getIdViaje() + 1;
    }
    public void agregarPasajeroInicial(Pasajeros pasajero) { listaPasajeros.add(pasajero); }

    // ==================== BÚSQUEDAS ====================
    // Módulo encargado de localizar buses, pasajeros y viajes por sus datos identificadores.
    // SIA-5: sobrecarga en una segunda clase distinta de Buses.
    public Pasajeros buscarPasajero(int id) throws ElementoNoEncontradoException {
        for (Pasajeros p : listaPasajeros) {
            if (p.getIdPasajero() == id) return p;
        }
        throw new ElementoNoEncontradoException("No existe el pasajero con ID " + id + ".");
    }

    public Pasajeros buscarPasajero(String nombre) throws ElementoNoEncontradoException {
        for (Pasajeros p : listaPasajeros) {
            if (p.getNombre().equalsIgnoreCase(nombre)) return p;
        }
        throw new ElementoNoEncontradoException("No existe el pasajero con nombre " + nombre + ".");
    }

    public Buses buscarBus(int id) throws ElementoNoEncontradoException {
        for (Buses bus : listaBuses) {
            if (bus.getIdBus() == id) return bus;
        }
        throw new ElementoNoEncontradoException("No existe el bus con ID " + id + ".");
    }

    public Viajes buscarViaje(int id) throws ElementoNoEncontradoException {
        for (Viajes viaje : listaViajes) {
            if (viaje.getIdViaje() == id) return viaje;
        }
        throw new ElementoNoEncontradoException("No existe el viaje con ID " + id + ".");
    }

    // ==================== CRUD BUSES ====================
    // Módulo encargado de agregar, modificar, eliminar y listar buses.
    public void agregarBus(int id, int capacidad) throws IllegalArgumentException {
        if (id <= 0 || capacidad <= 0) throw new IllegalArgumentException("ID y capacidad deben ser mayores que cero.");
        try { buscarBus(id); throw new IllegalArgumentException("Ya existe un bus con ese ID."); }
        catch (ElementoNoEncontradoException e) { 
            listaBuses.add(new Buses(id, capacidad));
            guardarCambios();
        }  
    }
    

    public boolean modificarBus(int id, int nuevaCapacidad) throws ElementoNoEncontradoException {
        Buses bus = buscarBus(id);
        if (nuevaCapacidad <= 0 || nuevaCapacidad < bus.getCantidadPasajeros()) {
            throw new IllegalArgumentException("La capacidad debe ser positiva y no menor que los pasajeros actuales.");
        }
        bus.setCapacity(nuevaCapacidad);
        guardarCambios();
        return true;
    }

    public boolean eliminarBus(int id) throws ElementoNoEncontradoException {
        Buses bus = buscarBus(id);
        for (Viajes viaje : listaViajes) {
            if (viaje.tieneBus(id) && viaje.estaDisponible()) {
                throw new IllegalArgumentException("El bus está asignado a un viaje futuro y no puede eliminarse.");
            }
        }
        boolean eliminado = listaBuses.remove(bus);
        if (eliminado) {
            guardarCambios();
        }
        return eliminado;
    }

    public String listarBusesTexto() {
        StringBuilder sb = new StringBuilder("=== BUSES ===\n");
        for (Buses bus : listaBuses) sb.append(bus).append("\n");
        return sb.toString();
    }
    
    public int getCantidadBuses() {
        return listaBuses.size();
    }

    public Buses obtenerBus(int posicion) {
        return listaBuses.get(posicion);
    }

    // ==================== CRUD VIAJES ====================
    // Módulo encargado de agregar, modificar, eliminar y listar viajes.
    public Viajes agregarViaje(String origen, String destino, double costoViaje,
                               double costoPasaje, LocalDateTime fechaHora, int cantidadBuses)
            throws IllegalArgumentException {
        if (origen == null || origen.trim().isEmpty() || destino == null || destino.trim().isEmpty())
            throw new IllegalArgumentException("Origen y destino son obligatorios.");
        if (costoViaje < 0 || costoPasaje < 0 || fechaHora == null || cantidadBuses <= 0)
            throw new IllegalArgumentException("Datos del viaje inválidos.");
        Viajes viaje = new Viajes(contadorViajes++, origen.trim(), destino.trim(), costoViaje, costoPasaje, fechaHora);
        int agregados = agregarBusesDisponibles(viaje, cantidadBuses);
        if (agregados == 0) {
            contadorViajes--;
            throw new IllegalArgumentException("No hay buses disponibles para el horario indicado.");
        }
        listaViajes.add(viaje);
        guardarCambios();
        return viaje;
    }

    private int agregarBusesDisponibles(Viajes viaje, int cantidad) {
        int agregados = 0;
        for (Buses base : listaBuses) {
            if (agregados >= cantidad) break;
            if (busDisponibleEnHorario(base.getIdBus(), viaje.getFechaHoraInicio(), viaje.getFechaHoraFin(), null)) {
                viaje.agregarBus(base.getIdBus(), base.getCapacity());
                agregados++;
            }
        }
        return agregados;
    }

    private boolean busDisponibleEnHorario(int idBus, LocalDateTime inicio, LocalDateTime fin, Integer excluirViaje) {
        for (Viajes viaje : listaViajes) {
            if (excluirViaje != null && viaje.getIdViaje() == excluirViaje) continue;
            if (!viaje.tieneBus(idBus)) continue;
            boolean seCruzan = inicio.isBefore(viaje.getFechaHoraFin()) && fin.isAfter(viaje.getFechaHoraInicio());
            if (seCruzan) return false;
        }
        return true;
    }

    public boolean modificarViaje(int id, String origen, String destino, double costoViaje,
                                  double costoPasaje, LocalDateTime nuevaFechaHora)
            throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(id);
        if (origen == null || origen.trim().isEmpty() || destino == null || destino.trim().isEmpty())
            throw new IllegalArgumentException("Origen y destino son obligatorios.");
        if (costoViaje < 0 || costoPasaje < 0 || nuevaFechaHora == null)
            throw new IllegalArgumentException("Datos del viaje inválidos.");
        viaje.setOrigen(origen.trim());
        viaje.setDestino(destino.trim());
        viaje.setCostoViaje(costoViaje);
        viaje.setCostoPasaje(costoPasaje);
        viaje.setFechaHoraInicio(nuevaFechaHora);
        guardarCambios();
        return true;
    }

    public boolean eliminarViaje(int id) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(id);
        if (!viaje.estaDisponible()) throw new IllegalArgumentException("No se puede eliminar un viaje que ya comenzó.");
        for(int i = 0; i < viaje.getCantidadBuses(); i++){
            Buses bus = viaje.obtenerBusPorPosicion(i);
            while(bus.getCantidadPasajeros() > 0){
                Pasajeros pasajero = bus.obtenerPasajero(0);
                bus.eliminarPasajero(pasajero);
                
            }
        }


        boolean eliminado = listaViajes.remove(viaje);
        if (eliminado) {
            guardarCambios();
        }
        return eliminado;
    }

    public String listarViajesTexto() {
        StringBuilder sb = new StringBuilder("=== VIAJES ===\n");
        for (Viajes viaje : listaViajes) {
            sb.append(viaje).append(" | Inicio: ").append(viaje.getFechaHoraInicio().format(FORMATO))
              .append(" | Buses: ").append(viaje.getCantidadBuses()).append("\n");
        }
        if (listaViajes.isEmpty()) sb.append("No existen viajes registrados.\n");
        return sb.toString();
    }
    
    public int getCantidadViajes() {
        return listaViajes.size();
    }

    public Viajes obtenerViaje(int posicion) {
        return listaViajes.get(posicion);
    }

    // ==================== RESERVAS ====================
    // Módulo encargado de crear, cancelar y reagendar reservas de pasajeros.
    public boolean reservarViaje(int idPasajero, String nombre, int edad,
                              String origen, String destino, String fechaHora) {
        try {
            if (idPasajero <= 0 || edad <= 0 || nombre == null || nombre.trim().isEmpty())
                throw new IllegalArgumentException("Datos del pasajero inválidos.");
            try { buscarPasajero(idPasajero); throw new IllegalArgumentException("Ya existe una reserva con ese ID de pasajero."); }
            catch (ElementoNoEncontradoException e) { /* ID disponible */ }

            LocalDateTime fecha = parseFechaHora(fechaHora);
            Viajes viajeEncontrado = null;
            for (Viajes viaje : listaViajes) {
                if (viaje.getOrigen().equalsIgnoreCase(origen) && viaje.getDestino().equalsIgnoreCase(destino)
                        && viaje.getFechaHoraInicio().equals(fecha)) {
                    viajeEncontrado = viaje;
                    break;
                }
            }

            if (viajeEncontrado == null) {
                viajeEncontrado = agregarViaje(origen, destino, 100000, 5000, fecha, listaBuses.size());
            }
            if (!viajeEncontrado.estaDisponible()) {
                System.out.println("El viaje ya comenzó o no está disponible.");
                return false;
            }
            Buses bus = viajeEncontrado.buscarBusDisponible();
            if (bus == null) {
                System.out.println("No hay buses disponibles para este viaje.");
                return false;
            }
            Pasajeros pasajero = bus.agregarPasajero(idPasajero, edad, nombre.trim());
            listaPasajeros.add(pasajero);
            guardarCambios();
            System.out.println("\nViaje reservado con éxito.");
            System.out.println("Pasajero: " + nombre + " | Bus: " + bus.getIdBus() + " | Viaje ID: " + viajeEncontrado.getIdViaje());
            return true;
        } catch (CapacidadExcedidaException e) {
            System.out.println("No se pudo reservar: " + e.getMessage());
            return false;
        } catch (DateTimeParseException e) {
            System.out.println("Fecha/hora inválida. Use dd/MM/yyyy HH:mm.");
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo reservar: " + e.getMessage());
            return false;
        }
    }

    public void cancelarViaje(int idReserva) {
        try {
            Pasajeros pasajero = buscarPasajero(idReserva);
            Buses bus = pasajero.getBus();
            if (bus != null) bus.eliminarPasajero(pasajero);
            boolean cancelada = listaPasajeros.remove(pasajero);
            if (cancelada) {
                guardarCambios();
                System.out.println("Reserva " + idReserva + " cancelada correctamente.");
            }
        } catch (ElementoNoEncontradoException e) {
            System.out.println(e.getMessage());
        }
    }

    public void reagendarViaje(int idReserva, String nuevaFechaHora) {
        try {
            Pasajeros pasajero = buscarPasajero(idReserva);
            LocalDateTime nuevaFecha = parseFechaHora(nuevaFechaHora);
            Viajes nuevoViaje = null;
            for (Viajes viaje : listaViajes) {
                if (viaje.getFechaHoraInicio().equals(nuevaFecha) && viaje.estaDisponible()) {
                    nuevoViaje = viaje;
                    break;
                }
            }
            if (nuevoViaje == null) {
                System.out.println("No existe un viaje disponible para esa fecha y hora.");
                return;
            }
            Buses nuevoBus = nuevoViaje.buscarBusDisponible();
            if (nuevoBus == null) {
                System.out.println("No hay espacio disponible en el nuevo viaje.");
                return;
            }
            Buses anterior = pasajero.getBus();
            nuevoBus.agregarPasajero(pasajero);
            if (anterior != null) anterior.eliminarPasajero(pasajero);
            guardarCambios();
            System.out.println("Reserva reagendada correctamente al viaje " + nuevoViaje.getIdViaje() + ".");
        } catch (ElementoNoEncontradoException e) {
            System.out.println(e.getMessage());
        } catch (CapacidadExcedidaException e) {
            System.out.println("No se pudo reagendar: " + e.getMessage());
        } catch (DateTimeParseException e) {
            System.out.println("Fecha/hora inválida. Use dd/MM/yyyy HH:mm.");
        }
    }

    private LocalDateTime parseFechaHora(String texto) {
        return LocalDateTime.parse(texto.trim(), FORMATO);
    }
    
    
    public int getCantidadPasajeros() {
    return listaPasajeros.size();
    }

    public Pasajeros obtenerPasajero(int posicion) {
        return listaPasajeros.get(posicion);
        
    }
    
    public boolean eliminarPasajero(int id) throws ElementoNoEncontradoException {
        Pasajeros pasajero = buscarPasajero(id);
        Buses bus = pasajero.getBus();

        if (bus != null) {
            bus.eliminarPasajero(pasajero);
        }

        boolean eliminado = listaPasajeros.remove(pasajero);
        if (eliminado) {
            guardarCambios();
        }
        return eliminado;
    }
        
    // ==================== SIA-9: UTILIDAD DE NEGOCIO ====================
    // Módulo de utilidad de negocio: filtra los viajes según su rentabilidad.
    public ArrayList<Viajes> obtenerViajesRentables() {
        ArrayList<Viajes> resultado = new ArrayList<>();
        for (Viajes viaje : listaViajes) {
            for (int i = 0; i < viaje.getCantidadBuses(); i++) {
                Buses bus = viaje.obtenerBusPorPosicion(i);
                if (viaje.esRentable(bus)) {
                    resultado.add(viaje);
                    break;
                }
            }
        }
        return resultado;
    }

    public void mostrarViajesRentables() {
        ArrayList<Viajes> resultado = obtenerViajesRentables();
        System.out.println("=== VIAJES RENTABLES ===");
        if (resultado.isEmpty()) {
            System.out.println("No hay viajes que cumplan el criterio de rentabilidad.");
            return;
        }
        for (Viajes viaje : resultado) System.out.println(viaje + " | Ganancia estimada por bus con pasajeros: criterio cumplido");
    }

    public String buscarBusTexto(int id) {
        try { return buscarBus(id).toString(); }
        catch (ElementoNoEncontradoException e) { return e.getMessage(); }
    }

    public String buscarViajeTexto(int id) {
        try { return buscarViaje(id).toString(); }
        catch (ElementoNoEncontradoException e) { return e.getMessage(); }
    }
}
    

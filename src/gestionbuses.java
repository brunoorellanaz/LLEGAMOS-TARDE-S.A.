import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class gestionbuses {
    private ArrayList<Viajes> listaViajes;
    private int contadorViajes;
    private String archivo;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public gestionbuses() {
        listaViajes = new ArrayList<>();
        contadorViajes = 1;
        cargarDatosIniciales();
    }
    public void cargarDesdeArchivo(String ruta) throws IOException {
        CargarDatos.cargar(this, ruta);
        archivo = ruta;
    }

    public ArrayList<Viajes> getListaViajes() {
        return listaViajes;
    }

    public int getCantidadViajes() {
        return listaViajes.size();
    }

    public Viajes obtenerViaje(int posicion) {
        if (posicion < 0 || posicion >= listaViajes.size()) {
            return null;
        }

        return listaViajes.get(posicion);
    }

    public Viajes buscarViaje(int id) throws ElementoNoEncontradoException {
        for (Viajes viaje : listaViajes) {
            if (viaje.getIdViaje() == id) {
                return viaje;
            }
        }

        throw new ElementoNoEncontradoException("No existe el viaje con ID " + id + ".");
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public String getArchivo() {
        return archivo;
    }

    public void agregarViaje(Viajes viaje) {
        if (viaje == null) {
            return;
        }

        listaViajes.add(viaje);

        if (viaje.getIdViaje() >= contadorViajes) {
            contadorViajes = viaje.getIdViaje() + 1;
        }
    }

    public Viajes agregarViaje(String origen, String destino, double costoViaje, double costoPasaje, LocalDateTime fechaHoraInicio, int duracionMinutos) {
        Viajes viaje = new Viajes(contadorViajes, origen, destino, costoViaje, costoPasaje, fechaHoraInicio);
        viaje.setDuracionMinutos(duracionMinutos);
        agregarViaje(viaje);
        return viaje;
    }

    public void agregarViajeInicial(Viajes viaje) {
        agregarViaje(viaje);
    }

    public void eliminarViaje(int id) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(id);
        listaViajes.remove(viaje);
    }

    public void modificarViaje(int id, String origen, String destino, double costoViaje, double costoPasaje, LocalDateTime fechaHoraInicio) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(id);

        if (origen == null || origen.trim().isEmpty()) {
            throw new IllegalArgumentException("El origen no puede estar vacío.");
        }

        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("El destino no puede estar vacío.");
        }

        if (costoViaje < 0) {
            throw new IllegalArgumentException("El costo del viaje no puede ser negativo.");
        }

        if (costoPasaje < 0) {
            throw new IllegalArgumentException("El costo del pasaje no puede ser negativo.");
        }

        if (fechaHoraInicio == null) {
            throw new IllegalArgumentException("La fecha del viaje no puede ser nula.");
        }

        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setCostoViaje(costoViaje);
        viaje.setCostoPasaje(costoPasaje);
        viaje.setFechaHoraInicio(fechaHoraInicio);
    }

    public void reagendarViaje(int id, LocalDateTime nuevaFecha) throws ElementoNoEncontradoException {
        if (nuevaFecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula.");
        }

        Viajes viaje = buscarViaje(id);
        viaje.setFechaHoraInicio(nuevaFecha);
    }

    // ============================================================
    // GESTION DE BUSES
    // ============================================================

    public void agregarBus(int id, int capacidad) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del bus debe ser positivo.");
        }

        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }

        if (buscarBusSinExcepcion(id) != null) {
            throw new IllegalArgumentException("Ya existe un bus con el ID " + id + ".");
        }

        if (listaViajes.isEmpty()) {
            LocalDateTime fecha = LocalDateTime.now().plusDays(1);
            Viajes viaje = new Viajes(contadorViajes, "Sin origen", "Sin destino", 100000, 5000, fecha);
            viaje.agregarBus(id, capacidad);
            agregarViaje(viaje);
            return;
        }

        for (Viajes viaje : listaViajes) {
            if (!viaje.tieneBus(id)) {
                viaje.agregarBus(id, capacidad);
            }
        }
    }

    public void eliminarBus(int id) throws ElementoNoEncontradoException {
        Buses bus = buscarBusSinExcepcion(id);

        if (bus == null) {
            throw new ElementoNoEncontradoException("No existe un bus con el ID " + id + ".");
        }

        if (bus.getCantidadPasajeros() > 0) {
            throw new IllegalArgumentException("No se puede eliminar el bus porque tiene pasajeros.");
        }

        boolean eliminado = false;

        for (Viajes viaje : listaViajes) {
            if (viaje.tieneBus(id)) {
                viaje.eliminarBus(id);
                eliminado = true;
            }
        }

        if (!eliminado) {
            throw new ElementoNoEncontradoException("No se pudo eliminar el bus.");
        }
    }

    public void modificarBus(int id, int nuevaCapacidad) throws ElementoNoEncontradoException {
        if (nuevaCapacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }

        Buses bus = buscarBusSinExcepcion(id);

        if (bus == null) {
            throw new ElementoNoEncontradoException("No existe un bus con el ID " + id + ".");
        }

        for (Viajes viaje : listaViajes) {
            Buses busViaje = viaje.obtenerBus(id);

            if (busViaje != null) {
                if (nuevaCapacidad < busViaje.getCantidadPasajeros()) {
                    throw new IllegalArgumentException("La nueva capacidad no puede ser menor que la cantidad de pasajeros.");
                }

                busViaje.setCapacity(nuevaCapacidad);
            }
        }
    }

    public int getCantidadBuses() {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Viajes viaje : listaViajes) {
            for (int i = 0; i < viaje.getCantidadBuses(); i++) {
                Buses bus = viaje.obtenerBusPorPosicion(i);

                if (bus != null && !ids.contains(bus.getIdBus())) {
                    ids.add(bus.getIdBus());
                }
            }
        }
        return ids.size();
    }

    public Buses obtenerBus(int posicion) {
        ArrayList<Buses> buses = obtenerBusesUnicos();

        if (posicion < 0 || posicion >= buses.size()) {
            return null;
        }

        return buses.get(posicion);
    }

    public Buses buscarBus(int id) throws ElementoNoEncontradoException{
        return buscarBusSinExcepcion(id);
    }

    private Buses buscarBusSinExcepcion(int id) {
        for (Viajes viaje : listaViajes) {
            Buses bus = viaje.obtenerBus(id);

            if (bus != null) {
                return bus;
            }
        }

        return null;
    }

    private ArrayList<Buses> obtenerBusesUnicos() {
        ArrayList<Buses> buses = new ArrayList<>();

        for (Viajes viaje : listaViajes) {
            for (int i = 0; i < viaje.getCantidadBuses(); i++) {
                Buses bus = viaje.obtenerBusPorPosicion(i);

                if (bus != null) {
                    boolean existe = false;

                    for (Buses busExistente : buses) {
                        if (busExistente.getIdBus() == bus.getIdBus()) {
                            existe = true;
                            break;
                        }
                    }

                    if (!existe) {
                        buses.add(bus);
                    }
                }
            }
        }

        return buses;
    }

    // ============================================================
    // GESTION DE PASAJEROS
    // ============================================================

    public int getCantidadPasajeros() {
        return obtenerPasajerosUnicos().size();
    }

    public Pasajeros obtenerPasajero(int posicion) {
        ArrayList<Pasajeros> pasajeros = obtenerPasajerosUnicos();

        if (posicion < 0 || posicion >= pasajeros.size()) {
            return null;
        }

        return pasajeros.get(posicion);
    }

    public Pasajeros buscarPasajero(int id) throws ElementoNoEncontradoException {
        for (Viajes viaje : listaViajes) {
            try {
                return viaje.buscarPasajero(id);
            } catch (ElementoNoEncontradoException e) {
            }
        }

        throw new ElementoNoEncontradoException("No existe un pasajero con ID " + id + ".");
    }

    public boolean eliminarPasajero(int id) throws ElementoNoEncontradoException{
        for (Viajes viaje : listaViajes) {
            try {
                Pasajeros pasajero = viaje.buscarPasajero(id);
                Buses bus = pasajero.getBus();

                if (bus != null) {
                    return bus.eliminarPasajero(pasajero);
                }
            } catch (ElementoNoEncontradoException e) {
            }
        }

        return false;
    }

    private ArrayList<Pasajeros> obtenerPasajerosUnicos() {
        ArrayList<Pasajeros> pasajeros = new ArrayList<>();

        for (Viajes viaje : listaViajes) {
            for (int i = 0; i < viaje.getCantidadBuses(); i++) {
                Buses bus = viaje.obtenerBusPorPosicion(i);

                if (bus == null) {
                    continue;
                }

                for (int j = 0; j < bus.getCantidadPasajeros(); j++) {
                    Pasajeros pasajero = bus.obtenerPasajero(j);

                    if (pasajero != null) {
                        boolean existe = false;

                        for (Pasajeros pasajeroExistente : pasajeros) {
                            if (pasajeroExistente.getIdPasajero() == pasajero.getIdPasajero()) {
                                existe = true;
                                break;
                            }
                        }

                        if (!existe) {
                            pasajeros.add(pasajero);
                        }
                    }
                }
            }
        }

        return pasajeros;
    }

    // ============================================================
    // RESERVAS
    // ============================================================

    public boolean reservarViaje(int idPasajero, String nombre, int edad, String origen, String destino, String fechaHora) {
        if (idPasajero <= 0) {
            throw new IllegalArgumentException("El ID del pasajero debe ser positivo.");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }

        if (edad <= 0) {
            throw new IllegalArgumentException("La edad debe ser mayor que cero.");
        }

        if (origen == null || origen.trim().isEmpty()) {
            throw new IllegalArgumentException("El origen no puede estar vacío.");
        }

        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("El destino no puede estar vacío.");
        }

        if (fechaHora == null || fechaHora.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha y hora no pueden estar vacías.");
        }

        try {
            buscarPasajero(idPasajero);
            throw new IllegalArgumentException("Ya existe un pasajero con el ID " + idPasajero + ".");
        } catch (ElementoNoEncontradoException e) {
        }

        LocalDateTime fecha;

        try {
            fecha = LocalDateTime.parse(fechaHora, FORMATO);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener el formato dd/MM/yyyy HH:mm.");
        }

        Viajes viajeEncontrado = null;

        for (Viajes viaje : listaViajes) {
            if (viaje.getOrigen().equalsIgnoreCase(origen) && viaje.getDestino().equalsIgnoreCase(destino) && viaje.getFechaHoraInicio().equals(fecha)) {
                viajeEncontrado = viaje;
                break;
            }
        }

        if (viajeEncontrado == null) {
            viajeEncontrado = agregarViaje(origen, destino, 100000, 5000, fecha, 120);

            viajeEncontrado.agregarBus(1, 40);
            viajeEncontrado.agregarBus(2, 40);
            viajeEncontrado.agregarBus(3, 50);
        }

        if (!viajeEncontrado.estaDisponible()) {
            throw new IllegalArgumentException("El viaje ya comenzó o no está disponible.");
        }

        Buses bus = viajeEncontrado.buscarBusDisponible();

        if (bus == null) {
            throw new IllegalArgumentException("No hay asientos disponibles para este viaje.");
        }

        try {
            Pasajeros pasajero = new Pasajeros(idPasajero, edad, nombre);
            bus.agregarPasajero(pasajero);
            return true;
        } catch (CapacidadExcedidaException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // ============================================================
    // VIAJES RENTABLES
    // ============================================================

    public ArrayList<Viajes> obtenerViajesRentables() {
        ArrayList<Viajes> rentables = new ArrayList<>();

        for (Viajes viaje : listaViajes) {
            boolean rentable = false;

            for (int i = 0; i < viaje.getCantidadBuses(); i++) {
                Buses bus = viaje.obtenerBusPorPosicion(i);

                if (viaje.esRentable(bus)) {
                    rentable = true;
                    break;
                }
            }

            if (rentable) {
                rentables.add(viaje);
            }
        }

        return rentables;
    }

    // ============================================================
    // OPERACIONES DE VIAJES
    // ============================================================

    public void agregarBusAViaje(int idViaje, int idBus, int capacidad) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(idViaje);

        if (!viaje.tieneBus(idBus)) {
            viaje.agregarBus(idBus, capacidad);
        }
    }

    public void eliminarBusDeViaje(int idViaje, int idBus) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(idViaje);
        Buses bus = viaje.obtenerBus(idBus);

        if (bus != null && bus.getCantidadPasajeros() > 0) {
            throw new IllegalArgumentException("No se puede eliminar un bus que tiene pasajeros.");
        }

        viaje.eliminarBus(idBus);
    }

    public Buses obtenerBus(int idViaje, int idBus) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(idViaje);
        Buses bus = viaje.obtenerBus(idBus);

        if (bus == null) {
            throw new ElementoNoEncontradoException("No existe el bus " + idBus + " en el viaje " + idViaje + ".");
        }

        return bus;
    }

    public Pasajeros buscarPasajero(int idViaje, int idPasajero) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(idViaje);
        return viaje.buscarPasajero(idPasajero);
    }

    public void reservarPasaje(int idViaje, int idPasajero, int edad, String nombre) throws ElementoNoEncontradoException, CapacidadExcedidaException {
        Viajes viaje = buscarViaje(idViaje);

        if (!viaje.estaDisponible()) {
            throw new IllegalArgumentException("El viaje ya comenzó o no está disponible.");
        }

        try {
            viaje.buscarPasajero(idPasajero);
            throw new IllegalArgumentException("Ya existe un pasajero con el ID " + idPasajero + " en este viaje.");
        } catch (ElementoNoEncontradoException e) {
        }

        Buses bus = viaje.buscarBusDisponible();

        if (bus == null) {
            throw new CapacidadExcedidaException("No hay buses con asientos disponibles para este viaje.");
        }

        Pasajeros pasajero = new Pasajeros(idPasajero, edad, nombre);
        bus.agregarPasajero(pasajero);
    }

    public void cancelarReserva(int idViaje, int idPasajero) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(idViaje);
        Pasajeros pasajero = viaje.buscarPasajero(idPasajero);
        Buses bus = pasajero.getBus();

        if (bus == null) {
            throw new ElementoNoEncontradoException("El pasajero no está asociado a ningún bus.");
        }

        bus.eliminarPasajero(pasajero);
    }

    public void reagendarPasajero(int idViaje, int idPasajero, int idBus) throws ElementoNoEncontradoException, CapacidadExcedidaException {
        Viajes viaje = buscarViaje(idViaje);
        Pasajeros pasajero = viaje.buscarPasajero(idPasajero);
        Buses nuevoBus = viaje.obtenerBus(idBus);

        if (nuevoBus == null) {
            throw new ElementoNoEncontradoException("No existe el bus " + idBus + " en este viaje.");
        }

        Buses busActual = pasajero.getBus();

        if (busActual == nuevoBus) {
            return;
        }

        nuevoBus.agregarPasajero(pasajero);

        if (busActual != null) {
            busActual.eliminarPasajero(pasajero);
        }
    }

    // ============================================================
    // PERSISTENCIA
    // ============================================================

    public void guardarCambios() throws IOException {
        if (archivo == null || archivo.trim().isEmpty()) {
            throw new IOException("No se ha definido un archivo de guardado.");
        }

        GuardarDatos.guardar(this, archivo);
    }

    public void guardarCambios(String ruta) throws IOException {
        archivo = ruta;
        GuardarDatos.guardar(this, ruta);
    }

    public void cargarDatos(String ruta) throws IOException {
        archivo = ruta;
        CargarDatos.cargar(this, ruta);
        actualizarContador();
    }

    public void limpiarDatos() {
        listaViajes.clear();
        contadorViajes = 1;
    }

    private void actualizarContador() {
        contadorViajes = 1;

        for (Viajes viaje : listaViajes) {
            if (viaje.getIdViaje() >= contadorViajes) {
                contadorViajes = viaje.getIdViaje() + 1;
            }
        }
    }

    // ============================================================
    // DATOS INICIALES
    // ============================================================

    public void cargarDatosIniciales() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        Viajes viaje = new Viajes(contadorViajes, "Valparaiso", "Santiago", 100000, 5000, fecha);

        Buses bus1 = new Buses(1, 40);
        Buses bus2 = new Buses(2, 40);
        Buses bus3 = new Buses(3, 50);

        viaje.agregarBus(bus1);
        viaje.agregarBus(bus2);
        viaje.agregarBus(bus3);

        try {
            Pasajeros pasajero = new Pasajeros(1, 30, "Pasajero Ejemplo");
            bus1.agregarPasajero(pasajero);
        } catch (CapacidadExcedidaException e) {
            System.out.println("No se pudo agregar el pasajero inicial: " + e.getMessage());
        }

        agregarViaje(viaje);
    }

    public String obtenerInformacionViaje(int idViaje) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(idViaje);
        StringBuilder informacion = new StringBuilder();

        informacion.append("Viaje: ").append(viaje.getIdViaje()).append("\n");
        informacion.append("Origen: ").append(viaje.getOrigen()).append("\n");
        informacion.append("Destino: ").append(viaje.getDestino()).append("\n");
        informacion.append("Costo viaje: $").append(viaje.getCostoViaje()).append("\n");
        informacion.append("Costo pasaje: $").append(viaje.getCostoPasaje()).append("\n");
        informacion.append("Inicio: ").append(viaje.getFechaHoraInicio().format(FORMATO)).append("\n");
        informacion.append("Fin: ").append(viaje.getFechaHoraFin().format(FORMATO)).append("\n");
        informacion.append("Buses: ").append(viaje.getCantidadBuses()).append("\n");
        informacion.append("Pasajeros: ").append(viaje.getCantidadPasajeros()).append("\n");

        return informacion.toString();
    }

    public void mostrarViaje() {
        if (listaViajes.isEmpty()) {
            System.out.println("No existen viajes registrados.");
            return;
        }

        for (Viajes viaje : listaViajes) {
            viaje.mostrarViaje();
            System.out.println();
        }
    }

    public void mostrarViaje(int idViaje) throws ElementoNoEncontradoException {
        Viajes viaje = buscarViaje(idViaje);
        viaje.mostrarViaje();
    }

    //Estos son métodos que son usamos como sobrecarga por cambios en la estructura de datos
    public void cancelarViaje(int id) throws ElementoNoEncontradoException {
        eliminarPasajero(id);
    }

    public void reagendarViaje(int id, String fechaHora) throws ElementoNoEncontradoException {
        try {
            LocalDateTime fecha = LocalDateTime.parse(fechaHora, FORMATO);
            reagendarViaje(id, fecha);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida. Use el formato dd/MM/yyyy HH:mm.");
        }
    }

    public void mostrarViajesRentables() {
        ArrayList<Viajes> rentables = obtenerViajesRentables();

        if (rentables.isEmpty()) {
            System.out.println("No hay viajes rentables.");
            return;
        }

        for (Viajes viaje : rentables) {
            System.out.println(viaje);
        }
    }

    public String listarBusesTexto() {
        ArrayList<Buses> buses = obtenerBusesUnicos();

        if (buses.isEmpty()) {
            return "No hay buses registrados.";
        }

        StringBuilder texto = new StringBuilder();

        for (Buses bus : buses) {
            texto.append(bus).append(System.lineSeparator());
        }

        return texto.toString();
    }

    public String listarViajesTexto() {
        if (listaViajes.isEmpty()) {
            return "No hay viajes registrados.";
        }

        StringBuilder texto = new StringBuilder();

        for (Viajes viaje : listaViajes) {
            texto.append(viaje).append(System.lineSeparator());
        }

        return texto.toString();
    }

    public void guardarEnArchivo(String ruta) throws IOException {
        guardarCambios(ruta);
    }

    public String buscarBusTexto(int id) throws ElementoNoEncontradoException {
        return buscarBus(id).toString();
    }

    public Pasajeros buscarPasajero(String nombre) throws ElementoNoEncontradoException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ElementoNoEncontradoException("El nombre no puede estar vacío.");
        }

        for (Viajes viaje : listaViajes) {
            for (int i = 0; i < viaje.getCantidadBuses(); i++) {
                Buses bus = viaje.obtenerBusPorPosicion(i);

                for (int j = 0; j < bus.getCantidadPasajeros(); j++) {
                    Pasajeros pasajero = bus.obtenerPasajero(j);

                    if (pasajero.getNombre().equalsIgnoreCase(nombre.trim())) {
                        return pasajero;
                    }
                }
            }
        }

        throw new ElementoNoEncontradoException("No existe un pasajero con el nombre " + nombre + ".");
    }


}


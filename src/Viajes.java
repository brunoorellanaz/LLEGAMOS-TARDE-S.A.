import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Viajes {
    private int id_viaje;
    private String origen;
    private String destino;
    private HashMap<Integer, Buses> buses;
    private double costoViaje;
    private double costoPasaje;
    private LocalDateTime fechaHoraInicio;
    private int duracionMinutos;

    public Viajes(int id_viaje, String origen, String destino, double costoViaje, double costoPasaje, LocalDateTime fechaHoraInicio) {
        this.id_viaje = id_viaje;
        this.origen = origen;
        this.destino = destino;
        this.costoViaje = costoViaje;
        this.costoPasaje = costoPasaje;
        this.fechaHoraInicio = fechaHoraInicio;
        this.duracionMinutos = 120;
        this.buses = new HashMap<>();
    }

    public void setIdViaje(int id_viaje) {
        this.id_viaje = id_viaje;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public void setCostoViaje(double costoViaje) {
        this.costoViaje = costoViaje;
    }

    public void setCostoPasaje(double costoPasaje) {
        this.costoPasaje = costoPasaje;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public int getIdViaje() {
        return id_viaje;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public double getCostoViaje() {
        return costoViaje;
    }

    public double getCostoPasaje() {
        return costoPasaje;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraInicio.plusMinutes(duracionMinutos);
    }

    public java.time.LocalTime getHoraInicio() {
        return fechaHoraInicio.toLocalTime();
    }

    public void agregarBus(Buses bus) {
        if (bus != null) {
            buses.put(bus.getIdBus(), bus);
        }
    }

    public void agregarBus(int idBus, int capacidad) {
        agregarBus(new Buses(idBus, capacidad));
    }

    public boolean eliminarBus(int idBus) {
        return buses.remove(idBus) != null;
    }

    public boolean tieneBus(int idBus) {
        return buses.containsKey(idBus);
    }

    public Buses obtenerBus(int idBus) {
        return buses.get(idBus);
    }

    public int getCantidadBuses() {
        return buses.size();
    }

    public Buses obtenerBusPorPosicion(int posicion) {
        if (posicion < 0 || posicion >= buses.size()) {
            return null;
        }

        return (Buses) buses.values().toArray()[posicion];
    }

    public Buses buscarBusDisponible() {
        for (Buses bus : buses.values()) {
            if (bus.getDisponibility() && bus.getCantidadPasajeros() < bus.getCapacity()) {
                return bus;
            }
        }

        return null;
    }

    public Pasajeros buscarPasajero(int id) throws ElementoNoEncontradoException {
        for (Buses bus : buses.values()) {
            for (int i = 0; i < bus.getCantidadPasajeros(); i++) {
                Pasajeros pasajero = bus.obtenerPasajero(i);

                if (pasajero.getIdPasajero() == id) {
                    return pasajero;
                }
            }
        }

        throw new ElementoNoEncontradoException("No existe el pasajero con ID " + id + " en este viaje.");
    }

    public Buses buscarBusDePasajero(int idPasajero) throws ElementoNoEncontradoException {
        for (Buses bus : buses.values()) {
            for (int i = 0; i < bus.getCantidadPasajeros(); i++) {
                Pasajeros pasajero = bus.obtenerPasajero(i);

                if (pasajero.getIdPasajero() == idPasajero) {
                    return bus;
                }
            }
        }

        throw new ElementoNoEncontradoException("No existe el pasajero con ID " + idPasajero + " en este viaje.");
    }

    public boolean estaDisponible() {
        return LocalDateTime.now().isBefore(fechaHoraInicio);
    }

    public boolean esRentable(Buses bus) {
        return bus != null && bus.getCantidadPasajeros() * costoPasaje > costoViaje;
    }

    public void reasignarBus(Buses bus) {
        if (bus == null || !buses.containsKey(bus.getIdBus())) {
            return;
        }

        for (Buses otroBus : buses.values()) {
            if (otroBus.getIdBus() == bus.getIdBus()) {
                continue;
            }

            while (otroBus.getCantidadPasajeros() < otroBus.getCapacity() && bus.getCantidadPasajeros() > 0) {
                Pasajeros pasajero = bus.obtenerPasajero(0);

                try {
                    otroBus.agregarPasajero(pasajero);
                    bus.eliminarPasajero(pasajero);
                } catch (CapacidadExcedidaException e) {
                    break;
                }
            }

            if (bus.getCantidadPasajeros() == 0) {
                break;
            }
        }
    }

    public int getCantidadPasajeros() {
        int cantidad = 0;

        for (Buses bus : buses.values()) {
            cantidad += bus.getCantidadPasajeros();
        }

        return cantidad;
    }

    public void mostrarViaje() {
        System.out.println(this);

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        System.out.println("Inicio: " + fechaHoraInicio.format(formato));
        System.out.println("Fin: " + getFechaHoraFin().format(formato));
        System.out.println("Costo viaje: $" + costoViaje + " | Costo pasaje: $" + costoPasaje);

        for (Buses bus : buses.values()) {
            System.out.println("  " + bus);
        }
    }

    @Override
    public String toString() {
        return "Viaje " + id_viaje + " | " + origen + " -> " + destino;
    }
}
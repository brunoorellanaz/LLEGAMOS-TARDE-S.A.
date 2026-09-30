import java.util.ArrayList;

public class Buses {
    private int id_bus;
    private int capacity;
    private boolean disponibility;
    private ArrayList<Pasajeros> pasajeros;

    public Buses(int id_bus, int capacity) {
        this.id_bus = id_bus;
        this.capacity = capacity;
        this.disponibility = true;
        this.pasajeros = new ArrayList<>();
    }

    public void setIdBus(int id_bus) {
        this.id_bus = id_bus;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        this.disponibility = pasajeros.size() < capacity;
    }

    public void setDisponibility(boolean disponibility) {
        this.disponibility = disponibility;
    }

    public int getIdBus() {
        return id_bus;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean getDisponibility() {
        return disponibility;
    }

    public int getCantidadPasajeros() {
        return pasajeros.size();
    }

    public void agregarPasajero(Pasajeros nuevo_pasajero) throws CapacidadExcedidaException {
        if (nuevo_pasajero == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }

        if (pasajeros.size() >= capacity || !disponibility) {
            setDisponibility(false);
            throw new CapacidadExcedidaException("El bus " + id_bus + " no tiene asientos disponibles.");
        }

        pasajeros.add(nuevo_pasajero);
        nuevo_pasajero.setBus(this);
        disponibility = pasajeros.size() < capacity;
    }

    public Pasajeros agregarPasajero(int id, int edad, String nombre) throws CapacidadExcedidaException {
        Pasajeros pasajero = new Pasajeros(id, edad, nombre);
        agregarPasajero(pasajero);
        return pasajero;
    }

    public boolean eliminarPasajero(Pasajeros pasajero) {
        if (pasajero != null && pasajeros.remove(pasajero)) {
            pasajero.setBus(null);
            disponibility = pasajeros.size() < capacity;
            return true;
        }

        return false;
    }

    public Pasajeros obtenerPasajero(int posicion) {
        return pasajeros.get(posicion);
    }

    public ArrayList<Pasajeros> getPasajeros() {
        return pasajeros;
    }

    public void mostrarPasajeros() {
        if (pasajeros.isEmpty()) {
            System.out.println("El bus no tiene pasajeros.");
            return;
        }

        for (Pasajeros pasajero : pasajeros) {
            System.out.println(pasajero);
        }
    }

    @Override
    public String toString() {
        return "Bus " + id_bus + " | Capacidad: " + capacity + " | Pasajeros: " + pasajeros.size() + "/" + capacity + " | Disponible: " + disponibility;
    }
}
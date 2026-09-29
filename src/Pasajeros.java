public class Pasajeros {
    private int id_pasajero;
    private Buses bus;
    private int edad;
    private String nombre;

    public Pasajeros(int id_pasajero, int edad, String nombre) {
        this.id_pasajero = id_pasajero;
        this.edad = edad;
        this.nombre = nombre;
        this.bus = null;
    }

    public void setBus(Buses bus) {
        this.bus = bus;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setIdPasajero(int id_pasajero) {
        this.id_pasajero = id_pasajero;
    }

    public int getIdPasajero() {
        return id_pasajero;
    }

    public Buses getBus() {
        return bus;
    }

    public int getEdad() {
        return edad;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return "ID: " + id_pasajero + " | Nombre: " + nombre + " | Edad: " + edad + " | Bus: " + (bus == null ? "Sin asignar" : bus.getIdBus());
    }
}
//SIA-6: subclase de Pasajeros
public class PasajeroAdultoMayor extends Pasajeros {
    public PasajeroAdultoMayor(int id_pasajero, int edad, String nombre) {
        super(id_pasajero, edad, nombre);
    }

    @Override
    public double factorTarifa() {
        return 0.7; //30% de descuento
    }

    @Override
    public String getTipo() {
        return "AdultoMayor";
    }
}
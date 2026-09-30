//SIA-6: subclase de Pasajeros
public class PasajeroEstudiante extends Pasajeros {
    public PasajeroEstudiante(int id_pasajero, int edad, String nombre) {
        super(id_pasajero, edad, nombre);
    }

    @Override
    public double factorTarifa() {
        return 0.5; //50% de descuento en la tarifa
    }
    
    @Override
    public String getTipo() {
        return "Estudiante";
    }
}
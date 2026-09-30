import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class GuardarDatos {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void guardar(gestionbuses gestion, String ruta) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ruta))) {
            escritor.write("TIPO;ID;DATO1;DATO2;DATO3;DATO4;DATO5;DATO6");
            escritor.newLine();

            for (int i = 0; i < gestion.getCantidadViajes(); i++) {
                Viajes viaje = gestion.obtenerViaje(i);

                escritor.write("VIAJE;" + viaje.getIdViaje() + ";" + limpiar(viaje.getOrigen()) + ";" + limpiar(viaje.getDestino()) + ";" + viaje.getCostoViaje() + ";" + viaje.getCostoPasaje() + ";" + viaje.getFechaHoraInicio().format(FORMATO) + ";" + viaje.getDuracionMinutos());
                escritor.newLine();

                for (int j = 0; j < viaje.getCantidadBuses(); j++) {
                    Buses bus = viaje.obtenerBusPorPosicion(j);

                    escritor.write("BUS_VIAJE;" + viaje.getIdViaje() + ";" + bus.getIdBus() + ";" + bus.getCapacity());
                    escritor.newLine();

                    for (int k = 0; k < bus.getCantidadPasajeros(); k++) {
                        Pasajeros pasajero = bus.obtenerPasajero(k);

                        escritor.write("PASAJERO;" + pasajero.getIdPasajero() + ";" + limpiar(pasajero.getNombre()) + ";" + pasajero.getEdad() + ";" + viaje.getIdViaje() + ";" + bus.getIdBus() + ";" + pasajero.getTipo());
                        escritor.newLine();
                    }
                }
            }
        }
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(";", " ");
    }
}
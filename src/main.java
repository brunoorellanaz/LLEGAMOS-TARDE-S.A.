import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.io.IOException;

public class main {
    private static final String ARCHIVO = "datos_SIA.csv";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        gestionbuses gestion = new gestionbuses();

        try {
            gestion.cargarDesdeArchivo(ARCHIVO);
        } catch (IOException e) {
            System.out.println("No se pudo cargar el archivo: " + e.getMessage());
            gestion.cargarDatosIniciales();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                gestion.guardarEnArchivo(ARCHIVO);
            } catch (Exception e) {
                System.out.println("No se pudieron guardar los datos al salir: " +
                    e.getMessage());
            }
        }));

        System.out.println("========================================");
        System.out.println(" SISTEMA DE INFORMACIÓN - SIA");
        System.out.println("========================================");
        System.out.println("1. Consola");
        System.out.println("2. Ventanas");
        System.out.print("Seleccione el modo: ");

        String opcion = scanner.nextLine().trim();
        if (opcion.equals("2")) {
            scanner.close();
            javax.swing.SwingUtilities.invokeLater(() -> new mainVentana(gestion, ARCHIVO).setVisible(true));
        } else if(opcion.equals("1")) {
            ejecutarConsola(scanner, gestion, ARCHIVO);
            scanner.close();
        }else{
            System.out.println("Opción no valida, saliendo del programa.");
        }
    }

    private static void ejecutarConsola(Scanner scanner, gestionbuses gestion, String archivo) {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n=== GESTIÓN DEL SISTEMA ===");
            System.out.println("1. Reservar viaje");
            System.out.println("2. Cancelar reserva");
            System.out.println("3. Reagendar reserva");
            System.out.println("4. Gestión de BUSES");
            System.out.println("5. Gestión de VIAJES");
            System.out.println("6. Buscar pasajero");
            System.out.println("7. Mostrar viajes rentables (utilidad de negocio)");
            System.out.println("8. Mostrar estructura completa");
            System.out.println("9. Salir y guardar");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();

            try {
                switch (opcion) {
                    case "1": reservar(scanner, gestion); break;
                    case "2":
                        System.out.print("ID del pasajero/reserva: ");
                        gestion.cancelarViaje(Integer.parseInt(scanner.nextLine()));
                        break;
                    case "3":
                        System.out.print("ID del pasajero/reserva: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Nueva fecha y hora (dd/MM/yyyy HH:mm): ");
                        gestion.reagendarViaje(id, scanner.nextLine());
                        break;
                    case "4": menuBuses(scanner, gestion); break;
                    case "5": menuViajes(scanner, gestion); break;
                    case "6": buscarPasajero(scanner, gestion); break;
                    case "7": gestion.mostrarViajesRentables(); break;
                    case "8":
                        System.out.println(gestion.listarBusesTexto());
                        System.out.println(gestion.listarViajesTexto());
                        break;
                    case "9":
                        gestion.guardarEnArchivo(archivo);
                        continuar = false;
                        break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número válido.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void reservar(Scanner s, gestionbuses g) {
        System.out.print("ID pasajero: "); int id = Integer.parseInt(s.nextLine());
        System.out.print("Nombre: "); String nombre = s.nextLine();
        System.out.print("Edad: "); int edad = Integer.parseInt(s.nextLine());
        System.out.print("Origen: "); String origen = s.nextLine();
        System.out.print("Destino: "); String destino = s.nextLine();
        System.out.print("Fecha y hora (dd/MM/yyyy HH:mm): "); String fecha = s.nextLine();
        System.out.print("Tipo (1 = Normal, 2 = Estudiante, 3 = Adulto Mayor): ");
        String t = s.nextLine().trim();
        String tipo = t.equals("2") ? "Estudiante" : (t.equals("3") ? "AdultoMayor" : "Normal");
        g.reservarViaje(id, nombre, edad, origen, destino, fecha, tipo);
    }

    private static void menuBuses(Scanner s, gestionbuses g) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- CRUD BUSES ---");
            System.out.println("1. Agregar bus");
            System.out.println("2. Listar buses");
            System.out.println("3. Modificar bus");
            System.out.println("4. Eliminar bus");
            System.out.println("5. Buscar bus");
            System.out.println("6. Mostrar pasajeros de un bus");
            System.out.println("7. Volver");
            System.out.print("Opción: ");
            try {
                switch (s.nextLine().trim()) {
                    case "1":
                        System.out.print("ID: "); int id = Integer.parseInt(s.nextLine());
                        System.out.print("Capacidad: "); int cap = Integer.parseInt(s.nextLine());
                        g.agregarBus(id, cap); System.out.println("Bus agregado."); break;
                    case "2": System.out.println(g.listarBusesTexto()); break;
                    case "3":
                        System.out.print("ID: "); id = Integer.parseInt(s.nextLine());
                        System.out.print("Nueva capacidad: "); cap = Integer.parseInt(s.nextLine());
                        g.modificarBus(id, cap); System.out.println("Bus modificado."); break;
                    case "4":
                        System.out.print("ID: "); id = Integer.parseInt(s.nextLine());
                        g.eliminarBus(id); System.out.println("Bus eliminado."); break;
                    case "5":
                        System.out.print("ID: "); id = Integer.parseInt(s.nextLine());
                        System.out.println(g.buscarBusTexto(id)); break;
                    case "6":
                        System.out.print("ID: "); id = Integer.parseInt(s.nextLine());
                        Buses bus = g.buscarBus(id); bus.mostrarPasajeros(); break;
                    case "7": volver = true; break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        }
    }

    private static void menuViajes(Scanner s, gestionbuses g) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- CRUD VIAJES ---");
            System.out.println("1. Agregar viaje");
            System.out.println("2. Listar viajes");
            System.out.println("3. Modificar viaje");
            System.out.println("4. Eliminar viaje");
            System.out.println("5. Buscar viaje");
            System.out.println("6. Volver");
            System.out.print("Opción: ");
            try {
                switch (s.nextLine().trim()) {
                    case "1":
                        System.out.print("Origen: "); String o = s.nextLine();
                        System.out.print("Destino: "); String d = s.nextLine();
                        System.out.print("Costo del viaje: "); double cv = Double.parseDouble(s.nextLine());
                        System.out.print("Costo del pasaje: "); double cp = Double.parseDouble(s.nextLine());
                        System.out.print("Fecha y hora (dd/MM/yyyy HH:mm): "); LocalDateTime f = LocalDateTime.parse(s.nextLine(), FORMATO);
                        System.out.print("Cantidad de buses: "); int n = Integer.parseInt(s.nextLine());
                        g.agregarViaje(o, d, cv, cp, f, n); System.out.println("Viaje agregado."); break;
                    case "2": System.out.println(g.listarViajesTexto()); break;
                    case "3":
                        System.out.print("ID viaje: "); int id = Integer.parseInt(s.nextLine());
                        System.out.print("Origen: "); o = s.nextLine();
                        System.out.print("Destino: "); d = s.nextLine();
                        System.out.print("Costo viaje: "); cv = Double.parseDouble(s.nextLine());
                        System.out.print("Costo pasaje: "); cp = Double.parseDouble(s.nextLine());
                        System.out.print("Nueva fecha/hora (dd/MM/yyyy HH:mm): "); f = LocalDateTime.parse(s.nextLine(), FORMATO);
                        g.modificarViaje(id, o, d, cv, cp, f); System.out.println("Viaje modificado."); break;
                    case "4":
                        System.out.print("ID viaje: "); id = Integer.parseInt(s.nextLine());
                        g.eliminarViaje(id); System.out.println("Viaje eliminado."); break;
                    case "5":
                        System.out.print("ID viaje: "); id = Integer.parseInt(s.nextLine());
                        try { g.buscarViaje(id).mostrarViaje(); }
                        catch (ElementoNoEncontradoException e) { System.out.println(e.getMessage()); }
                        break;
                    case "6": volver = true; break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        }
    }

    private static void buscarPasajero(Scanner s, gestionbuses g) {
        System.out.println("1. Buscar por ID");
        System.out.println("2. Buscar por nombre");
        System.out.print("Opción: ");
        try {
            if (s.nextLine().trim().equals("1")) {
                System.out.print("ID: "); System.out.println(g.buscarPasajero(Integer.parseInt(s.nextLine())));
            } else {
                System.out.print("Nombre: "); System.out.println(g.buscarPasajero(s.nextLine()));
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }
}
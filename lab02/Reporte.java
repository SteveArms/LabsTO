public class Reporte {

    public Reporte() {}

    public String generarReporte(Estudiante estudiante) {
        return "=== REPORTE DE ESTUDIANTE ===\n" +
               "Nombre: " + estudiante.getNombre() + "\n" +
               "Edad: " + estudiante.getEdad() + "\n" +
               "============================";
    }

    @Override
    public String toString() {
        return "Reporte{}";
    }
}

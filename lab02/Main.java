public class Main {
    public static void main(String[] args) {

        // Herencia: 2 profesores y 3 estudiantes (sin atributos propios)
        Profesor prof1 = new Profesor("Carlos Ruiz", 45);
        Profesor prof2 = new Profesor("Ana López", 38);

        Estudiante est1 = new Estudiante("Luis Torres", 20);
        Estudiante est2 = new Estudiante("María Gómez", 21);
        Estudiante est3 = new Estudiante("Pedro Díaz", 19);

        System.out.println("=== PROFESORES ===");
        System.out.println(prof1);
        System.out.println(prof2);

        System.out.println("\n=== ESTUDIANTES ===");
        System.out.println(est1);
        System.out.println(est2);
        System.out.println(est3);

        // Composición: Horario vive dentro del Curso
        Horario horario1 = new Horario("Lunes", "08:00");
        Curso curso1 = new Curso("Cálculo I", horario1);

        Horario horario2 = new Horario("Miércoles", "14:00");
        Curso curso2 = new Curso("Programación Java", horario2);

        System.out.println("\n=== CURSOS ===");
        System.out.println(curso1);
        System.out.println(curso2);

        // Agregación: la universidad agrupa los cursos
        Universidad universidad = new Universidad("Universidad Nacional");
        universidad.agregarCurso(curso1);
        universidad.agregarCurso(curso2);

        System.out.println("\n=== UNIVERSIDAD ===");
        System.out.println(universidad);

        // Dependencia: Reporte usa Estudiante como parámetro temporal
        Reporte reporte = new Reporte();
        System.out.println("\n" + reporte.generarReporte(est1));
    }
}

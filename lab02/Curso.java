public class Curso {
    private String nombre;
    private Horario horario; // composición: el horario vive con el curso

    public Curso(String nombre, Horario horario) {
        this.nombre = nombre;
        this.horario = horario;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Horario getHorario() { return horario; }
    public void setHorario(Horario horario) { this.horario = horario; }

    @Override
    public String toString() {
        return "Curso{nombre='" + nombre + "', " + horario + "}";
    }
}

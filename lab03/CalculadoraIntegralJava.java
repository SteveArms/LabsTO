import java.util.concurrent.*;

public class CalculadoraIntegralJava {
    private double a;
    private double b;
    private double tolerancia = 0.0001;
    private double resultadoAnterior = 0;
    private int numeroTrapecios = 1;
    private static final int NUCLEOS = Runtime.getRuntime().availableProcessors();

    public CalculadoraIntegralJava(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double evaluarFuncion(double x) {
        return 2 * x * x + 3 * x + 0.5;
    }

    public double calcularIntegralConThreads() throws InterruptedException {
        double ancho = (b - a) / numeroTrapecios;
        double suma = 0;

        Thread[] threads = new Thread[NUCLEOS];
        double[] resultados = new double[NUCLEOS];

        int trapeciosPorThread = numeroTrapecios / NUCLEOS;
        int trapeciosRestantes = numeroTrapecios % NUCLEOS;

        for (int i = 0; i < NUCLEOS; i++) {
            final int indice = i;
            final int inicio = i * trapeciosPorThread;
            final int fin = (i == NUCLEOS - 1) ? inicio + trapeciosPorThread + trapeciosRestantes : inicio + trapeciosPorThread;
            final double anchoFinal = ancho;

            threads[i] = new Thread(() -> {
                double suma_local = 0;
                for (int j = inicio; j < fin; j++) {
                    double x1 = a + j * anchoFinal;
                    double x2 = x1 + anchoFinal;
                    double altura = (evaluarFuncion(x1) + evaluarFuncion(x2)) / 2;
                    suma_local += altura * anchoFinal;
                }
                resultados[indice] = suma_local;
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (double resultado : resultados) {
            suma += resultado;
        }

        return suma;
    }

    public double calcularIntegralConPoolThreads() throws InterruptedException, ExecutionException {
        ExecutorService ejecutor = Executors.newFixedThreadPool(NUCLEOS);
        Future<?>[] futures = new Future[NUCLEOS];

        double ancho = (b - a) / numeroTrapecios;
        double[] resultados = new double[NUCLEOS];

        int trapeciosPorThread = numeroTrapecios / NUCLEOS;
        int trapeciosRestantes = numeroTrapecios % NUCLEOS;

        for (int i = 0; i < NUCLEOS; i++) {
            final int indice = i;
            final int inicio = i * trapeciosPorThread;
            final int fin = (i == NUCLEOS - 1) ? inicio + trapeciosPorThread + trapeciosRestantes : inicio + trapeciosPorThread;
            final double anchoFinal = ancho;

            futures[i] = ejecutor.submit(() -> {
                double suma_local = 0;
                for (int j = inicio; j < fin; j++) {
                    double x1 = a + j * anchoFinal;
                    double x2 = x1 + anchoFinal;
                    double altura = (evaluarFuncion(x1) + evaluarFuncion(x2)) / 2;
                    suma_local += altura * anchoFinal;
                }
                resultados[indice] = suma_local;
            });
        }

        for (Future<?> future : futures) {
            future.get();
        }

        ejecutor.shutdown();

        double suma = 0;
        for (double resultado : resultados) {
            suma += resultado;
        }

        return suma;
    }

    public void simularHastaConvergencia(boolean usarPool) throws InterruptedException, ExecutionException {
        double resultado;
        
        System.out.println("Iniciando cálculo de integral: f(x) = 2x² + 3x + 0.5");
        System.out.println("Intervalo: [" + a + ", " + b + "]");
        System.out.println("Método: " + (usarPool ? "Pool de Threads" : "Threads Directos"));
        System.out.println("Núcleos disponibles: " + NUCLEOS);
        System.out.println("-----------------------------------");

        while (true) {
            resultado = usarPool ? calcularIntegralConPoolThreads() : calcularIntegralConThreads();

            System.out.printf("Trapecios: %d | Resultado: %.10f | Diferencia: %.10f%n", 
                numeroTrapecios, resultado, Math.abs(resultado - resultadoAnterior));

            if (numeroTrapecios > 1 && Math.abs(resultado - resultadoAnterior) < tolerancia) {
                System.out.println("-----------------------------------");
                System.out.println("Convergencia alcanzada!");
                System.out.printf("Resultado final: %.10f%n", resultado);
                break;
            }

            resultadoAnterior = resultado;
            numeroTrapecios *= 2;
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        CalculadoraIntegralJava calculadora = new CalculadoraIntegralJava(2, 20);

        System.out.println("=== USANDO THREADS DIRECTOS ===\n");
        calculadora.simularHastaConvergencia(false);

        System.out.println("\n\n=== USANDO POOL DE THREADS ===\n");
        
        calculadora = new CalculadoraIntegralJava(2, 20);
        calculadora.simularHastaConvergencia(true);
    }
}

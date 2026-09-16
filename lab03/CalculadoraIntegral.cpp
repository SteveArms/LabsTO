#include <iostream>
#include <thread>
#include <vector>
#include <mutex>
#include <queue>
#include <condition_variable>
#include <functional>
#include <cmath>
#include <iomanip>

class CalculadoraIntegral {
public:
    double a;
    double b;
    double tolerancia;
    double resultadoAnterior;
    int numeroTrapecios;
    int nucleos;

    CalculadoraIntegral(double a, double b) 
        : a(a), b(b), tolerancia(0.0001), resultadoAnterior(0), 
          numeroTrapecios(1), nucleos(std::thread::hardware_concurrency()) {}

    double evaluarFuncion(double x) {
        return 2 * x * x + 3 * x + 0.5;
    }

    double calcularIntegralConThreads() {
        double ancho = (b - a) / numeroTrapecios;
        std::vector<double> resultados(nucleos, 0.0);
        std::vector<std::thread> threads;

        int trapeciosPorThread = numeroTrapecios / nucleos;
        int trapeciosRestantes = numeroTrapecios % nucleos;

        for (int i = 0; i < nucleos; i++) {
            int inicio = i * trapeciosPorThread;
            int fin = (i == nucleos - 1) ? inicio + trapeciosPorThread + trapeciosRestantes : inicio + trapeciosPorThread;
            
            threads.emplace_back([this, &resultados, i, inicio, fin, ancho]() {
                double suma_local = 0.0;
                for (int j = inicio; j < fin; j++) {
                    double x1 = a + j * ancho;
                    double x2 = x1 + ancho;
                    double altura = (evaluarFuncion(x1) + evaluarFuncion(x2)) / 2;
                    suma_local += altura * ancho;
                }
                resultados[i] = suma_local;
            });
        }

        for (auto& thread : threads) {
            thread.join();
        }

        double suma = 0.0;
        for (double resultado : resultados) {
            suma += resultado;
        }

        return suma;
    }
};

class PoolThreads {
private:
    std::vector<std::thread> workers;
    std::queue<std::function<void()>> tareas;
    std::mutex queueMutex;
    std::condition_variable cv;
    bool detener;

public:
    PoolThreads(int numWorkers) : detener(false) {
        for (int i = 0; i < numWorkers; i++) {
            workers.emplace_back([this]() {
                while (true) {
                    std::function<void()> tarea;
                    {
                        std::unique_lock<std::mutex> lock(queueMutex);
                        cv.wait(lock, [this]() { return !tareas.empty() || detener; });
                        
                        if (detener && tareas.empty()) {
                            return;
                        }
                        
                        if (!tareas.empty()) {
                            tarea = std::move(tareas.front());
                            tareas.pop();
                        }
                    }
                    if (tarea) {
                        tarea();
                    }
                }
            });
        }
    }

    template<class F>
    void encolaTarea(F f) {
        {
            std::unique_lock<std::mutex> lock(queueMutex);
            tareas.emplace(f);
        }
        cv.notify_one();
    }

    ~PoolThreads() {
        {
            std::unique_lock<std::mutex> lock(queueMutex);
            detener = true;
        }
        cv.notify_all();
        for (auto& worker : workers) {
            worker.join();
        }
    }
};

class CalculadoraConPool {
private:
    double a;
    double b;
    double tolerancia;
    double resultadoAnterior;
    int numeroTrapecios;
    int nucleos;

public:
    CalculadoraConPool(double a, double b)
        : a(a), b(b), tolerancia(0.0001), resultadoAnterior(0),
          numeroTrapecios(1), nucleos(std::thread::hardware_concurrency()) {}

    double evaluarFuncion(double x) {
        return 2 * x * x + 3 * x + 0.5;
    }

    double calcularIntegralConPoolThreads() {
        double ancho = (b - a) / numeroTrapecios;
        std::vector<double> resultados(nucleos, 0.0);
        PoolThreads pool(nucleos);

        int trapeciosPorThread = numeroTrapecios / nucleos;
        int trapeciosRestantes = numeroTrapecios % nucleos;

        std::mutex resultMutex;
        int tareasCompletadas = 0;
        std::condition_variable allDone;

        for (int i = 0; i < nucleos; i++) {
            int inicio = i * trapeciosPorThread;
            int fin = (i == nucleos - 1) ? inicio + trapeciosPorThread + trapeciosRestantes : inicio + trapeciosPorThread;
            
            pool.encolaTarea([this, &resultados, &resultMutex, &tareasCompletadas, &allDone, i, inicio, fin, ancho]() {
                double suma_local = 0.0;
                for (int j = inicio; j < fin; j++) {
                    double x1 = a + j * ancho;
                    double x2 = x1 + ancho;
                    double altura = (evaluarFuncion(x1) + evaluarFuncion(x2)) / 2;
                    suma_local += altura * ancho;
                }
                {
                    std::unique_lock<std::mutex> lock(resultMutex);
                    resultados[i] = suma_local;
                    tareasCompletadas++;
                }
                allDone.notify_all();
            });
        }

        {
            std::unique_lock<std::mutex> lock(resultMutex);
            allDone.wait(lock, [&tareasCompletadas, this]() { return tareasCompletadas == nucleos; });
        }

        double suma = 0.0;
        for (double resultado : resultados) {
            suma += resultado;
        }

        return suma;
    }

    void simularHastaConvergencia(bool usarPool) {
        std::string metodo = usarPool ? "Pool de Threads" : "Threads Directos";
        
        std::cout << "Iniciando calculo de integral: f(x) = 2x2 + 3x + 0.5" << std::endl;
        std::cout << "Intervalo: [" << a << ", " << b << "]" << std::endl;
        std::cout << "Metodo: " << metodo << std::endl;
        std::cout << "Nucleos disponibles: " << nucleos << std::endl;
        std::cout << "-----------------------------------" << std::endl;

        CalculadoraIntegral calc1(a, b);
        
        while (true) {
            double resultado = usarPool ? calcularIntegralConPoolThreads() : calc1.calcularIntegralConThreads();
            double diferencia = std::abs(resultado - resultadoAnterior);

            std::cout << "Trapecios: " << numeroTrapecios << " | Resultado: " 
                      << std::fixed << std::setprecision(10) << resultado 
                      << " | Diferencia: " << diferencia << std::endl;

            if (numeroTrapecios > 1 && diferencia < tolerancia) {
                std::cout << "-----------------------------------" << std::endl;
                std::cout << "Convergencia alcanzada!" << std::endl;
                std::cout << "Resultado final: " << std::fixed << std::setprecision(10) << resultado << std::endl;
                break;
            }

            resultadoAnterior = resultado;
            numeroTrapecios *= 2;
            calc1.numeroTrapecios = numeroTrapecios;
        }
    }
};

int main() {
    std::cout << "=== USANDO THREADS DIRECTOS ===" << std::endl << std::endl;
    CalculadoraConPool calc1(2, 20);
    calc1.simularHastaConvergencia(false);

    std::cout << std::endl << std::endl;
    std::cout << "=== USANDO POOL DE THREADS ===" << std::endl << std::endl;
    CalculadoraConPool calc2(2, 20);
    calc2.simularHastaConvergencia(true);

    return 0;
}

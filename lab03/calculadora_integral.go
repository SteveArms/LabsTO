package main

import (
	"fmt"
	"math"
	"runtime"
	"sync"
)

type CalculadoraIntegral struct {
	a                  float64
	b                  float64
	tolerancia         float64
	resultadoAnterior  float64
	numeroTrapecios    int
	nucleos            int
}

func NuevaCalculadora(a, b float64) *CalculadoraIntegral {
	return &CalculadoraIntegral{
		a:               a,
		b:               b,
		tolerancia:      0.0001,
		resultadoAnterior: 0,
		numeroTrapecios: 1,
		nucleos:         runtime.NumCPU(),
	}
}

func (c *CalculadoraIntegral) EvaluarFuncion(x float64) float64 {
	return 2*x*x + 3*x + 0.5
}

func (c *CalculadoraIntegral) CalcularIntegralConGoroutines() float64 {
	ancho := (c.b - c.a) / float64(c.numeroTrapecios)
	resultados := make([]float64, c.nucleos)
	var wg sync.WaitGroup

	trapeciosPorThread := c.numeroTrapecios / c.nucleos
	trapeciosRestantes := c.numeroTrapecios % c.nucleos

	wg.Add(c.nucleos)

	for i := 0; i < c.nucleos; i++ {
		go func(indice int) {
			defer wg.Done()

			inicio := indice * trapeciosPorThread
			fin := inicio + trapeciosPorThread
			if indice == c.nucleos-1 {
				fin += trapeciosRestantes
			}

			suma_local := 0.0
			for j := inicio; j < fin; j++ {
				x1 := c.a + float64(j)*ancho
				x2 := x1 + ancho
				altura := (c.EvaluarFuncion(x1) + c.EvaluarFuncion(x2)) / 2
				suma_local += altura * ancho
			}
			resultados[indice] = suma_local
		}(i)
	}

	wg.Wait()

	suma := 0.0
	for _, resultado := range resultados {
		suma += resultado
	}

	return suma
}

func (c *CalculadoraIntegral) CalcularIntegralConPoolWorkers() float64 {
	ancho := (c.b - c.a) / float64(c.numeroTrapecios)
	
	type Tarea struct {
		inicio int
		fin    int
		indice int
	}

	tareas := make(chan Tarea, c.nucleos)
	resultados := make([]float64, c.nucleos)
	var wg sync.WaitGroup

	trapeciosPorThread := c.numeroTrapecios / c.nucleos
	trapeciosRestantes := c.numeroTrapecios % c.nucleos

	for i := 0; i < c.nucleos; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for tarea := range tareas {
				suma_local := 0.0
				for j := tarea.inicio; j < tarea.fin; j++ {
					x1 := c.a + float64(j)*ancho
					x2 := x1 + ancho
					altura := (c.EvaluarFuncion(x1) + c.EvaluarFuncion(x2)) / 2
					suma_local += altura * ancho
				}
				resultados[tarea.indice] = suma_local
			}
		}()
	}

	for i := 0; i < c.nucleos; i++ {
		inicio := i * trapeciosPorThread
		fin := inicio + trapeciosPorThread
		if i == c.nucleos-1 {
			fin += trapeciosRestantes
		}
		tareas <- Tarea{inicio, fin, i}
	}

	close(tareas)
	wg.Wait()

	suma := 0.0
	for _, resultado := range resultados {
		suma += resultado
	}

	return suma
}

func (c *CalculadoraIntegral) SimularHastaConvergencia(usarPool bool) {
	metodo := "Goroutines Directas"
	if usarPool {
		metodo = "Pool de Workers"
	}

	fmt.Println("Iniciando cálculo de integral: f(x) = 2x² + 3x + 0.5")
	fmt.Printf("Intervalo: [%.0f, %.0f]\n", c.a, c.b)
	fmt.Printf("Método: %s\n", metodo)
	fmt.Printf("Núcleos disponibles: %d\n", c.nucleos)
	fmt.Println("-----------------------------------")

	for {
		var resultado float64
		if usarPool {
			resultado = c.CalcularIntegralConPoolWorkers()
		} else {
			resultado = c.CalcularIntegralConGoroutines()
		}

		diferencia := math.Abs(resultado - c.resultadoAnterior)
		fmt.Printf("Trapecios: %d | Resultado: %.10f | Diferencia: %.10f\n", 
			c.numeroTrapecios, resultado, diferencia)

		if c.numeroTrapecios > 1 && diferencia < c.tolerancia {
			fmt.Println("-----------------------------------")
			fmt.Println("Convergencia alcanzada!")
			fmt.Printf("Resultado final: %.10f\n", resultado)
			break
		}

		c.resultadoAnterior = resultado
		c.numeroTrapecios *= 2
	}
}

func main() {
	fmt.Println("=== USANDO GOROUTINES DIRECTAS ===\n")
	calculadora1 := NuevaCalculadora(2, 20)
	calculadora1.SimularHastaConvergencia(false)

	fmt.Println("\n\n=== USANDO POOL DE WORKERS ===\n")
	calculadora2 := NuevaCalculadora(2, 20)
	calculadora2.SimularHastaConvergencia(true)
}

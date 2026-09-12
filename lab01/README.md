# LabsTO — Ejercicio: countChange en Scala

Descripción
-----------
Este repositorio contiene el enunciado del ejercicio: escribir una función recursiva en Scala que cuente de cuántas maneras diferentes se puede dar cambio para una cantidad dada usando una lista de denominaciones de monedas.

Enunciado
---------
Escriba una función recursiva que cuente de cuántas maneras diferentes puede dar cambio para una determinada cantidad y de acuerdo a una lista de denominaciones de monedas.

Por ejemplo, si la cantidad = 4 y las monedas son 1 y 2, las 3 maneras son:
- 1 + 1 + 1 + 1
- 1 + 1 + 2
- 2 + 2

Implementa la función:
```scala
def countChange(money: Int, coins: List[Int]): Int
```
- `money`: cantidad a cambiar (Int, >= 0)
- `coins`: lista de denominaciones únicas (List[Int])

Restricciones / notas
---------------------
- Implementa la solución de forma recursiva.
- Puedes usar las funciones de lista: `isEmpty`, `head` y `tail`.
- Convención común:
  - `countChange(0, _)` debe devolver 1 (una forma de dar cambio: usar ninguna moneda).
  - Si `money < 0` o `coins` está vacío y `money > 0`, devuelve 0.
- Se espera que el resultado sea el número total de combinaciones (el orden de las monedas no importa): por ejemplo, 1+2 y 2+1 cuentan como la misma combinación.

Idea recursiva (pista)
----------------------
Para una lista no vacía `coins` y moneda `c = coins.head`:
- Formas que usan al menos una moneda `c`: `countChange(money - c, coins)` (si `money - c >= 0`)
- Formas que no usan la moneda `c`: `countChange(money, coins.tail)`

Entonces:
```
countChange(money, coins) = countChange(money - c, coins) + countChange(money, coins.tail)
```
con casos base para `money == 0`, `money < 0` y `coins.isEmpty`.

Ejemplos
--------
- `countChange(4, List(1,2)) == 3`
- `countChange(0, List(1,2,3)) == 1`
- `countChange(10, List(2,5,3,6)) == 5` (caso clásico)

Estructura de proyecto sugerida
-------------------------------
- src/
  - main/
    - scala/
      - CountChange.scala    # implementación de countChange y objeto Main para probar
- README.md

Ejemplo de uso / main de prueba
-------------------------------
Puedes crear un archivo `src/main/scala/CountChange.scala` con algo como:

```scala
object CountChange {
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (money < 0) 0
    else if (coins.isEmpty) 0
    else countChange(money - coins.head, coins) + countChange(money, coins.tail)
  }

  def main(args: Array[String]): Unit = {
    println(countChange(4, List(1,2)))           // 3
    println(countChange(10, List(2,5,3,6)))      // 5
  }
}
```

Cómo compilar y ejecutar
------------------------
Si tienes `sbt`:
- Crea el proyecto sbt básico o ejecuta `sbt run` en un proyecto configurado.

Con `scalac` / `scala` (depende de tu instalación):
- Compilar:
  - `scalac src/main/scala/CountChange.scala`
- Ejecutar:
  - `scala CountChange`

Pruebas adicionales
-------------------
Agrega más casos en `main` o escribe pruebas unitarias con ScalaTest / MUnit si lo prefieres. Prueba casos límite: `money = 0`, lista vacía, monedas con valor mayor que `money`, y combinaciones con múltiplos.

Complejidad y mejoras
---------------------
- La implementación recursiva simple tiene complejidad exponencial en el peor caso.
- Para entradas más grandes, considera memoización (programación dinámica) para evitar recalcular subproblemas.

Contribuir
----------
- Implementa la función en `src/main/scala/CountChange.scala`.
- Añade pruebas.
- Haz un PR con tu solución y un breve comentario sobre la complejidad.

Autor
-----
Ejercicio adaptado para LabsTO.

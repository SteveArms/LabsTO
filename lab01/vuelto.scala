object vuelto {

  def main(args: Array[String]): Unit = {
    val money = 13
    val coins = List(1, 2, 3)
    
    println("------------------------------------------------------------")
    println("PROBLEMAS DE CUANTAS DIFERENTES MANERAS SE ENTREGA EL CAMBIO")
    println("------------------------------------------------------------")

    println(s"Dinero: $money")
    println(s"Monedas: $coins")
    
    val resultado = countChange(money, coins)

    println(s"Cantidad de maneras encontradas: $resultado")
  }

  def countChange(money: Int, coins: List[Int]): Int = {

    if (money == 0) {
      1
    } 
    else if (money < 0) {
      0
    } 
    else if (coins.isEmpty) {
      0
    } 
    else {
      val sinMoneda = countChange(money, coins.tail)
      val conMoneda = countChange(money - coins.head, coins)

      sinMoneda + conMoneda
    }
  }
}
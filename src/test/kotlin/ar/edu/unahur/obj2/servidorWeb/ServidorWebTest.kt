package ar.edu.unahur.obj2.servidorWeb

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime

class ServidorWebTest : DescribeSpec({
  describe("Un servidor web") {
    val servidor = ServidorWeb()
    val modulo1 = Modulo(listOf("txt"), "todo bien", 100)
    val modulo2 = Modulo(listOf("jpg", "gif"), "qué linda foto", 100)
    servidor.agregarModulo(modulo1)
    servidor.agregarModulo(modulo2)
    val analizador1 = AnalizadorDemora(20)
    val analizador2 = AnalizadorIP(listOf("207.46.13.5", "207.46.13.3", "207.46.13.1"))
    val analizador3 = AnalizadorEstadisticas()
    servidor.agregarAnalizador(analizador1)
    servidor.agregarAnalizador(analizador2)
    servidor.agregarAnalizador(analizador3)


    it("devuelve 501 si recibe un pedido que no es HTTP") {
      val respuesta = servidor.realizarPedido(Pedido("207.46.13.5", "https://pepito.com.ar/hola.txt", LocalDateTime.now()))
      respuesta.codigo.shouldBe(CodigoHttp.NOT_IMPLEMENTED)
      respuesta.body.shouldBe("")
    }

    it("devuelve 200 si algún módulo puede trabajar con el pedido") {
      val respuesta = servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
      respuesta.codigo.shouldBe(CodigoHttp.OK)
      respuesta.body.shouldBe("todo bien")
    }

    it("devuelve 404 si ningún módulo puede trabajar con el pedido") {
      val respuesta = servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/playa.png", LocalDateTime.now()))
      respuesta.codigo.shouldBe(CodigoHttp.NOT_FOUND)
      respuesta.body.shouldBe("")
    }

    describe("Detección de demora en respuesta"){
      val respuesta = servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/playa.png", LocalDateTime.now()))
      servidor.analizarRespuesta(respuesta)

      it("deberia tener un analizador"){
        servidor.analizadores.size.shouldBe(1)
      }

      it("debería haber una respuesta demorada"){
        analizador1.cantidadRespuestasDemoradas().shouldBe(1)
      }

      it("deberia no tener ningun analizador"){
        servidor.quitarAnalizador(analizador1)
        servidor.analizadores.shouldBeEmpty()
      }
    }

    describe("IPs Sospechosas"){
      it("cantidad de pedidos que hizo una ip sospechosa"){
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/playa.jpg", LocalDateTime.now()))
        analizador2.pedidosRealizadosPor("207.46.13.5").shouldBe(2)
      }

      it("modulo más consultado"){
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/playa.jpg", LocalDateTime.now()))
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
        analizador2.moduloMasConsultado().shouldBe(modulo1)
      }

      it("ruta obtenida"){
        val pedido  = Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now())
        pedido.obtenerRuta().shouldBe("hola.txt")
      }

      it("IPs sospechosas con cierta ruta"){
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
        servidor.realizarPedido(Pedido("207.46.13.3", "http://pepito.com.ar/playa.jpg", LocalDateTime.now()))
        servidor.realizarPedido(Pedido("207.46.13.1", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
        analizador2.ipsSospechosasConRuta("hola.txt").shouldContainAll("207.46.13.5", "207.46.13.1")
      }
      it("Tiempo de respuesta promedio"){
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
        servidor.realizarPedido(Pedido("207.46.13.3", "http://pepito.com.ar/playa.jpg", LocalDateTime.now()))
        servidor.realizarPedido(Pedido("207.46.13.1", "http://pepito.com.ar/hola.txt", LocalDateTime.now()))
        analizador3.tiempoPromedio().shouldBe(100)
      }
      it("Cantidad de pedidos entre dos momentos"){
        servidor.realizarPedido(Pedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.of(2001,9,11,8,46,0)))
        servidor.realizarPedido(Pedido("207.46.13.3", "http://pepito.com.ar/playa.jpg", LocalDateTime.of(2015,5,14,12,46,35)))
        servidor.realizarPedido(Pedido("207.46.13.1", "http://pepito.com.ar/hola.txt", LocalDateTime.of(2020,5,25,0,8,46)))
        analizador3.cantidadPedidosEntre(LocalDateTime.of(2001,9,11,8,46,0), LocalDateTime.now()).shouldBe(2)
      }

    }

  }
})

package ar.edu.unahur.obj2.servidorWeb

import java.time.LocalDateTime

enum class CodigoHttp(val codigo: Int) {
  OK(200),
  NOT_IMPLEMENTED(501),
  NOT_FOUND(404),
}

class ServidorWeb {
  val modulos = mutableListOf<Modulo>()
  val analizadores = mutableListOf<Analizador>()

  fun realizarPedido( pedido: Pedido): Respuesta {
    if (!pedido.url.startsWith("http:")) {
      val respuesta = Respuesta(CodigoHttp.NOT_IMPLEMENTED, "", 10, pedido, null)
      this.analizadores.forEach{ it.analizarRespuesta(respuesta) }

      return respuesta
    }

    if (this.algunModuloSoporta(pedido.url)) {
      val moduloSeleccionado = this.modulos.find { it.puedeTrabajarCon(pedido.url) }!!
      val respuesta = Respuesta(CodigoHttp.OK, moduloSeleccionado.body, moduloSeleccionado.tiempoRespuesta, pedido, moduloSeleccionado)
      if(analizadores.isNotEmpty()){
        this.analizadores.forEach{ it.analizarRespuesta(respuesta) }
      }
      return respuesta
    }
    val respuesta = Respuesta(CodigoHttp.NOT_FOUND,"", 10, pedido, null)
    this.analizadores.forEach{ it.analizarRespuesta(respuesta) }
    return respuesta
  }

  fun algunModuloSoporta(url: String) = this.modulos.any { it.puedeTrabajarCon(url) }

  fun agregarModulo(modulo: Modulo) {
    this.modulos.add(modulo)
  }

  fun agregarAnalizador(analizador: Analizador) {
    this.analizadores.add(analizador)
  }

  fun quitarAnalizador(analizador: Analizador) {
    if (this.analizadores.contains(analizador)){
      this.analizadores.remove(analizador)
    }
    else {
      throw Exception("Ese analizador no se encuentra en el servidor.")
    }
  }

  fun analizarRespuesta(respuesta: Respuesta) {
    this.analizadores.forEach { it.analizarRespuesta(respuesta) }
  }
}

class Pedido(val ip: String, val url: String, val fechaHora: LocalDateTime){
  fun obtenerRuta(): String = url.split("/").drop(3).joinToString(separator = "/")

}
class Respuesta(val codigo: CodigoHttp, val body: String, val tiempo: Int, val pedidoCreador: Pedido, val moduloCreador: Modulo?)

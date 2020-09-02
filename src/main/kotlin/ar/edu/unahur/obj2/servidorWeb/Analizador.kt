package ar.edu.unahur.obj2.servidorWeb

import java.time.LocalDateTime

abstract class Analizador(){
    abstract fun analizarRespuesta(respuesta: Respuesta)
}

class AnalizadorDemora(val tiempoDemora: Int): Analizador(){
    val respuestasDemoradas = mutableListOf<Respuesta>()
    val tiemposRespuesta = mutableListOf<Int>()

    override fun analizarRespuesta(respuesta: Respuesta){
        if(respuesta.tiempo > tiempoDemora){
            this.respuestasDemoradas.add(respuesta)
        }
        this.tiemposRespuesta.add(respuesta.tiempo)
    }
    fun cantidadRespuestasDemoradas() = this.respuestasDemoradas.size

}

class AnalizadorIP(val ipsSospechosas: List<String>): Analizador(){
    val pedidosSospechosos = mutableListOf<Pedido>()
    val modulos = mutableListOf<Modulo?>()

    override fun analizarRespuesta(respuesta: Respuesta){
        if(ipsSospechosas.contains(respuesta.pedidoCreador.ip)){
            this.pedidosSospechosos.add(respuesta.pedidoCreador)
        }
        modulos.add(respuesta.moduloCreador)
    }

    fun pedidosRealizadosPor(ip: String) = pedidosSospechosos.map { it.ip == ip }.count()
    fun moduloMasConsultado() = modulos.groupBy { it }.mapValues { it.value.size }.maxBy { it.value }?.key
    fun ipsSospechosasConRuta(ruta: String): List<String> = pedidosSospechosos.filter { it.obtenerRuta() == ruta }.map { it.ip }

}

class AnalizadorEstadisticas(): Analizador(){
    val respuestas = mutableListOf<Respuesta>()
    override fun analizarRespuesta(respuesta: Respuesta) {
        respuestas.add(respuesta)
    }
    fun tiempoPromedio() = respuestas.map { it.tiempo }.average()
    fun cantidadPedidosEntre(primerTiempo: LocalDateTime, segundoTiempo: LocalDateTime) =
        respuestas.filter {
                r -> r.pedidoCreador.fechaHora.isAfter(primerTiempo) && r.pedidoCreador.fechaHora.isBefore(segundoTiempo)
        }.count()
    fun cantidadRespuestasBody(string: String) = respuestas.filter { it.body.contains(string) }.size
    fun porcentajeExitosos(): Int = respuestas.count { it.codigo == CodigoHttp.OK } * 100 / respuestas.count()
}
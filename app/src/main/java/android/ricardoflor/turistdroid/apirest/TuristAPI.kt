package android.ricardoflor.turistdroid.apirest

object TuristAPI {

    private const val server = "florius8.ddns.net" //

    // Puerto del microservicio
    private const val port = "6969"
    private const val API_URL = "http://$server:$port/"

    // Constructor del servicio con los elementos de la interfaz
    val service: TuristREST
        get() = TuristRetrofit.getClient(API_URL)!!.create(TuristREST::class.java)
}
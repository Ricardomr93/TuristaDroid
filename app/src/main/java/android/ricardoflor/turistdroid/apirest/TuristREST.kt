package android.ricardoflor.turistdroid.apirest

import android.ricardoflor.turistdroid.bd.*
import retrofit2.Call
import retrofit2.http.*

interface TuristREST {

    // SESIONES ------------------------------------------------------------------------------------------
    // Obtener Sesion por ID de usuario
    @GET("sesiones/{id}")
    fun sesionGetById(@Path("id") id: String): Call<SesionDTO>

    // Actualiza la sesion del usuario
    @PUT("sesiones/{id}")
    fun sesionUpdate(@Path("id") id: String, @Body sesion: SesionDTO): Call<SesionDTO>

    // Elimina la sesión
    @DELETE("sesiones/{id}")
    fun sesionDelete(@Path("id") id: String): Call<SesionDTO>

    // Inserta una sesión
    @POST("sesiones/")
    fun sesionPost(@Body sesion: SesionDTO): Call<SesionDTO>

    // USUARIOS ------------------------------------------------------------------------------------------
    // Obtener Sesion por ID de usuario
    @GET("usuarios/{id}")
    fun usuarioGetById(@Path("id") id: String): Call<UserDTO>

    // LUGARES ------------------------------------------------------------------------------------------
    // Obtiene todos los lugares
    @GET("lugares/")
    fun lugarGetAll(): Call<List<SiteDTO>>

    // Obtiene todos los lugares que tengan en su campo el userID que le paso
    @GET("lugares/")
    fun lugarGetAllByUserID(@Query("usuarioID") usuarioID: String): Call<List<SiteDTO>>

    // Actualiza un lugar
    @PUT("lugares/{id}")
    fun lugarUpdate(@Path("id") id: String, @Body lugarDTO: SiteDTO): Call<SiteDTO>

    // Inserta un lugar
    @POST("lugares/")
    fun lugarPost(@Body lugar: SiteDTO): Call<SiteDTO>

    // Elimina el lugar
    @DELETE("lugares/{id}")
    fun lugarDelete(@Path("id") id: String): Call<SiteDTO>

    // FOTOGRAFIAS ------------------------------------------------------------------------------------------
    // Obtener Fotografias por su id
    @GET("fotografias/{id}")
    fun fotografiaGetById(@Path("id") id: String): Call<FotografiaDTO>

    // Inserta una fotografia
    @POST("fotografias/")
    fun fotografiaPost(@Body fotografia: FotografiaDTO): Call<FotografiaDTO>

    // Elimina la fotografía
    @DELETE("fotografias/{id}")
    fun fotografiaDelete(@Path("id") id: String): Call<FotografiaDTO>

    // Actualiza una fotografia
    @PUT("fotografias/{id}")
    fun fotografiaUpdate(@Path("id") id: String, @Body fotografiaDTO: FotografiaDTO): Call<FotografiaDTO>

    // Obtiene todas las fotografías de un usuario
    @GET("fotografias/")
    fun fotografiaGetAllByUserID(@Query("usuarioID") usuarioID: String): Call<List<FotografiaDTO>>

}
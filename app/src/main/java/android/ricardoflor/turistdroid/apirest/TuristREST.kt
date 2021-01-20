package android.ricardoflor.turistdroid.apirest

import android.ricardoflor.turistdroid.bd.image.ImageDTO
import android.ricardoflor.turistdroid.bd.session.SessionDTO
import android.ricardoflor.turistdroid.bd.site.SiteDTO
import android.ricardoflor.turistdroid.bd.user.UserDTO
import retrofit2.Call
import retrofit2.http.*

interface TuristREST {

    // SESIONES ------------------------------------------------------------------------------------------
    // Obtener Sesion por ID de usuario
    @GET("sesiones/{userId}")
    fun sesionGetById(@Path("userId") userId: String): Call<SessionDTO>

    // Actualiza la sesion del usuario
    @PUT("sesiones/{userId}")
    fun sesionUpdate(@Path("userId") userId: String, @Body session: SessionDTO): Call<SessionDTO>

    // Elimina la sesión
    @DELETE("sesiones/{userId}")
    fun sesionDelete(@Path("userId") userId: String): Call<SessionDTO>

    // Inserta una sesión
    @POST("sesiones/")
    fun sesionPost(@Body session: SessionDTO): Call<SessionDTO>

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
    fun lugarGetAllByUserID(@Query("userID") userID: String): Call<List<SiteDTO>>

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
    fun fotografiaGetById(@Path("id") id: String): Call<ImageDTO>

    // Inserta una fotografia
    @POST("fotografias/")
    fun fotografiaPost(@Body fotografia: ImageDTO): Call<ImageDTO>

    // Elimina la fotografía
    @DELETE("fotografias/{id}")
    fun fotografiaDelete(@Path("id") id: String): Call<ImageDTO>

    // Actualiza una fotografia
    @PUT("fotografias/{id}")
    fun fotografiaUpdate(@Path("id") id: String, @Body fotografiaDTO: ImageDTO): Call<ImageDTO>

    // Obtiene todas las fotografías de un usuario
    @GET("fotografias/")
    fun fotografiaGetAllByUserID(@Query("userID") userID: String): Call<List<ImageDTO>>

}
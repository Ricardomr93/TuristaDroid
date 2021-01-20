package android.ricardoflor.turistdroid.apirest

import android.ricardoflor.turistdroid.bd.image.ImageDTO
import android.ricardoflor.turistdroid.bd.session.SessionDTO
import android.ricardoflor.turistdroid.bd.site.SiteDTO
import android.ricardoflor.turistdroid.bd.user.UserDTO
import retrofit2.Call
import retrofit2.http.*

interface TuristREST {

    // ****SESIONES ------------------------------------------------------------------------------------------
    /**
     * Obtiene la sesion por id de usuario
     */
    @GET("session/{userId}")
    fun sesionGetById(@Path("userId") userId: String): Call<SessionDTO>

    /**
     * Actualiza la sesion
     */
    @PUT("session/{userId}")
    fun sesionUpdate(@Path("userId") userId: String, @Body session: SessionDTO): Call<SessionDTO>

    /**
     * Elimina la session
     */
    @DELETE("session/{userId}")
    fun sesionDelete(@Path("userId") userId: String): Call<SessionDTO>

    /**
     * Crea la session
     */
    @POST("session/")
    fun sesionPost(@Body session: SessionDTO): Call<SessionDTO>



    // ****USUARIOS ------------------------------------------------------------------------------------------
    /**
     * Obtiene al usuario por id
     */
    @GET("user/{id}")
    fun userGetById(@Path("id") id: String): Call<UserDTO>

    /**
     * Modifica al usuario
     */
    @PUT("user/{id}")
    fun userUpdate(@Path("userId") id: String, @Body session: UserDTO): Call<UserDTO>

    /**
     * Elimina al usuario
     */
    @DELETE("user/{id}")
    fun userDelete(@Path("id") id: String): Call<UserDTO>

    /**
     * Crea un usuario
     */
    @POST("user/")
    fun userPost(@Body user: UserDTO): Call<UserDTO>



    // ****SITIOS ------------------------------------------------------------------------------------------
    /**
     * Obtiene todos los sitios
     */
    @GET("sites/")
    fun siteGetAll(): Call<List<SiteDTO>>

    /**
     * Obtiene todos los lugares con un IdUser expecifico
     */
    @GET("sites/")
    fun siteGetByUserID(@Query("userID") userID: String): Call<List<SiteDTO>>

    /**
     * Obtiene los lugares cercanos
     */
    @GET("sites/")
    fun siteGetByNear(@Query("latitude") latitude : Double,
                      @Query("longitude") longitude : Double,
                      @Query("distance") distance : Double,): Call<List<SiteDTO>>

    /**
     * Modifica el sitio
     */
    @PUT("sites/{id}")
    fun siteUpdate(@Path("id") id: String, @Body siteDTO: SiteDTO): Call<SiteDTO>

    /**
     * Crea un sitio
     */
    @POST("sites/")
    fun sitesPost(@Body site: SiteDTO): Call<SiteDTO>

    /**
     * Elimina un lugar
     */
    @DELETE("sites/{id}")
    fun siteDelete(@Path("id") id: String): Call<SiteDTO>



    // ****FOTOGRAFIAS ------------------------------------------------------------------------------------------
    /**
     * Obtiene la imagen segun id
     */
    @GET("image/{id}")
    fun fotografiaGetById(@Path("id") id: String): Call<ImageDTO>

    /**
     * Obtiene las imagenes del sitio expecifico
     */
    @GET("image/")
    fun imageGet(@Query("siteID") siteID: String): Call<List<ImageDTO>>

    /**
     * Crea una imagen
     */
    @POST("image/")
    fun imagePost(@Body image: ImageDTO): Call<ImageDTO>

    /**
     * Elimina una imagen
     */
    @DELETE("image/{id}")
    fun imageDelete(@Path("id") id: String): Call<ImageDTO>

    /**
     * Modifica una imagen
     */
    @PUT("image/{id}")
    fun imageUpdate(@Path("id") id: String, @Body fotografiaDTO: ImageDTO): Call<ImageDTO>
}
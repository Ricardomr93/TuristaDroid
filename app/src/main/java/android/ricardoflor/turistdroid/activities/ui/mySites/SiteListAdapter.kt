package android.ricardoflor.turistdroid.activities.ui.mySites

import android.graphics.Bitmap
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.image.ImageDTO
import android.ricardoflor.turistdroid.bd.image.ImageMapper
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteDTO
import android.ricardoflor.turistdroid.bd.site.SiteMapper
import android.ricardoflor.turistdroid.utils.UtilImage
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_site.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Adapter de la Lista de Sitios
 */
class SiteListAdapter(
    private val listaSitios: MutableList<Site>,
    private val listener: (Site) -> Unit
    ) :
    RecyclerView.Adapter<SiteListAdapter.SiteViewHolder>() {

        /**
         * Asociamos la vista
         *
         * @param parent
         * @param viewType
         * @return
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
            return SiteViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_site, parent, false)
            )
        }

        /**
         * Procesamos los sitios y las metemos en un Holder
         *
         * @param holder
         * @param position
         */
        override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
            var siteName: String = listaSitios[position].name
            var siteLocation: String = listaSitios[position].site
            var siteDate: String = listaSitios[position].date
            var siteRatings: Double = listaSitios[position].rating
            var siteNumVotos: Int = listaSitios[position].votos
            var siteMedia: String = String.format("%.1f", (siteRatings/siteNumVotos))

            //Controlamos la longitud para que si llega a una cantidad de caracteres, recortarlo
            if (siteName.length >= 30) {
                siteName = siteName.substring(0, 30)
                holder.siteName.text = "$siteName..."
            } else {
                holder.siteName.text = siteName
            }
            if (siteLocation.length >= 30) {
                siteLocation = siteLocation.substring(0, 30)
                holder.siteLocation.text = "$siteLocation..."
            } else {
                holder.siteLocation.text = siteLocation
            }

            holder.siteDate.text = siteDate
            holder.siteRating.text = siteMedia

            //Cargamos una imagen para mostrar en el listado de sitios
            var listaImg: MutableList<Image>? = null
            val turistREST = TuristAPI.service
            val call: Call<List<ImageDTO>> = turistREST.imageGetbyIDSite(listaSitios[position].id)
            call.enqueue(object : Callback<List<ImageDTO>> {
                override fun onResponse(call: Call<List<ImageDTO>>, response: Response<List<ImageDTO>>) {
                    if (response.isSuccessful) {
                        listaImg =
                            ImageMapper.fromDTO(response.body() as MutableList<ImageDTO>) as MutableList<Image>//saca todos los resultados

                        if (null != listaImg && !listaImg!!.isEmpty()){

                            for (it in listaImg!!){
                                holder.siteImage.setImageBitmap(UtilImage.toBitmap(it.uri))
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<ImageDTO>>, t: Throwable) {
                    /*Toast.makeText(
                        applicationContext,
                        getText(R.string.service_error).toString() + t.localizedMessage,
                        Toast.LENGTH_LONG
                    )
                        .show()*/
                }

            })

            // Programamos el clic de cada fila (itemView)
            holder.btnViewSiteFloating
                .setOnClickListener {
                    // Devolvemos el sitio
                    listener(listaSitios[position])
                }
        }

        /**
         * Elimina un item de la lista
         *
         * @param position
         */
        fun removeItem(position: Int) {
            listaSitios.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listaSitios.size)
        }

        /**
         * Recupera un Item de la lista
         *
         * @param item
         * @param position
         */
        fun restoreItem(item: Site, position: Int) {
            listaSitios.add(position, item)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, listaSitios.size)
        }

        /**
         * Devuelve el número de items de la lista
         *
         * @return
         */
        override fun getItemCount(): Int {
            return listaSitios.size
        }

        /**
         * Holder que encapsula los objetos a mostrar en la lista
         */
        class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // Elementos graficos con los que nos asociamos
            var siteName = itemView.txtSiteName
            var siteLocation = itemView.txtSite2
            var siteDate = itemView.txtDate2
            var siteRating = itemView.txtRatings2
            var siteImage = itemView.imgSite

            // Indicamos el Layout para el click
            //var relativeLayout = itemView.item_site
            var btnViewSiteFloating = itemView.btnViewSiteFloating
        }
    }

package android.ricardoflor.turistdroid.export

import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.site.Site

class Export(
    val sites: MutableList<Site>,
    val images: MutableList<Image>
)
package android.ricardoflor.turistdroid.impExp

import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.user.User

class ImpExp(
    val users: MutableList<User>,
    val sites: MutableList<Site>,
    val images: MutableList<Image>
)
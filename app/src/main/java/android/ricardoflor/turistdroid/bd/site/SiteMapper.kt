package android.ricardoflor.turistdroid.bd.site


/**
 * Mapea entre DTO y Clase Modelo
 */
object SiteMapper {
    /**
     * Lista de DTO a Modelos
     * @param items List<SiteDTO>
     * @return List<Site>
     */
    fun fromDTO(items: List<SiteDTO>): List<Site> {
        return items.map { fromDTO(it) }
    }

    /**
     * Lista de Modelos a DTO
     * @param items List<Site>
     * @return List<SiteDTO>
     */
    fun toDTO(items: List<Site>): List<SiteDTO> {
        return items.map { toDTO(it) }
    }

    /**
     * DTO a Modelo
     * @param dto SiteDTO
     * @return Site
     */
    fun fromDTO(dto: SiteDTO): Site {
        return Site(
            dto.id,
            dto.name,
            dto.site,
            dto.date,
            dto.rating,
            dto.latitude,
            dto.longitude,
            dto.userID,
            dto.votos,
            dto.imageID
        )
    }

    /**
     * Modelo a DTO
     * @param model Site
     * @return SiteDTO
     */
    fun toDTO(model: Site): SiteDTO {
        return SiteDTO(
            model.id!!,
            model.name!!,
            model.site!!,
            model.date!!,
            model.rating!!,
            model.latitude!!,
            model.longitude!!,
            model.userID!!,
            model.votos!!,
            model.imageID!!
        )
    }
}
package android.ricardoflor.turistdroid.bd.image

/**
 * Mapea entre DTO y Clase Modelo
 */
object ImageMapper {
    /**
     * Lista de DTO a Modelos
     * @param items List<ImageDTO>
     * @return List<Image>
     */
    fun fromDTO(items: List<ImageDTO>): List<Image> {
        return items.map { fromDTO(it) }
    }

    /**
     * Lista de Modelos a DTO
     * @param items List<Image>
     * @return List<ImageDTO>
     */
    fun toDTO(items: List<Image>): List<ImageDTO> {
        return items.map { toDTO(it) }
    }

    /**
     * DTO a Modelo
     * @param dto ImageDTO
     * @return Image
     */
    fun fromDTO(dto: ImageDTO): Image {
        return Image(
            dto.id,
            dto.uri,
            dto.userID,
            dto.siteID,
        )
    }

    /**
     * Modelo a DTO
     * @param model Image
     * @return ImageDTO
     */
    fun toDTO(model: Image): ImageDTO {
        return ImageDTO(
            model.id!!,
            model.uri,
            model.userID,
            model.siteID,
        )
    }
}
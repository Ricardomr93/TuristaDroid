package android.ricardoflor.turistdroid.bd.session

/**
 * Mapea entre DTO y Clase Modelo
 */
object SessionMapper {
    /**
     * Lista de DTO a Modelos
     * @param items List<SessionDTO>
     * @return List<Session>
     */
    fun fromDTO(items: List<SessionDTO>): List<Session> {
        return items.map { fromDTO(it) }
    }

    /**
     * Lista de Modelos a DTO
     * @param items List<Session>
     * @return List<SessionDTO>
     */
    fun toDTO(items: List<Session>): List<SessionDTO> {
        return items.map { toDTO(it) }
    }

    /**
     * DTO a Modelo
     * @param dto SessionDTO
     * @return Session
     */
    fun fromDTO(dto: SessionDTO): Session {
        return Session(
            dto.userId,
            dto.time,
            dto.token,
        )
    }

    /**
     * Modelo a DTO
     * @param model Session
     * @return SessionDTO
     */
    fun toDTO(model: Session): SessionDTO {
        return SessionDTO(
            model.userId!!,
            model.time!!,
            model.token!!,
        )
    }
}
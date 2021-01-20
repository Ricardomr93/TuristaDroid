package android.ricardoflor.turistdroid.bd.user

/**
 * Mapea entre DTO y Clase Modelo
 */
object UserMapper {
    /**
     * Lista de DTO a Modelos
     * @param items List<UserDTO>
     * @return List<User>
     */
    fun fromDTO(items: List<UserDTO>): List<User> {
        return items.map { fromDTO(it) }
    }

    /**
     * Lista de Modelos a DTO
     * @param items List<User>
     * @return List<UserDTO>
     */
    fun toDTO(items: List<User>): List<UserDTO> {
        return items.map { toDTO(it) }
    }

    /**
     * DTO a Modelo
     * @param dto UserDTO
     * @return User
     */
    fun fromDTO(dto: UserDTO): User {
        return User(
            dto.id,
            dto.name,
            dto.nameUser,
            dto.email,
            dto.password,
            dto.image,
            dto.twitter,
            dto.instagram,
            dto.facebook,
        )
    }

    /**
     * Modelo a DTO
     * @param model User
     * @return UserDTO
     */
    fun toDTO(model: User): UserDTO {
        return UserDTO(
            model.id!!,
            model.name!!,
            model.nameUser!!,
            model.email!!,
            model.password!!,
            model.image!!,
            model.twitter!!,
            model.instagram!!,
            model.facebook!!,
        )
    }
}



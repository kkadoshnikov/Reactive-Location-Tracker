package com.gmail.kadoshnikovkirill.locationtracker.mapper

interface EntityDTOMapper<E, D> {
    fun toEntity(dto: D) = dto.mapToEntity()
    fun toDTO(entity: E) = entity.mapToDTO()
    fun D.mapToEntity(): E
    fun E.mapToDTO(): D
}
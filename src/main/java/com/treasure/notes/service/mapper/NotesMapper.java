package com.treasure.notes.service.mapper;

import com.treasure.notes.domain.*;
import com.treasure.notes.service.dto.NotesDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Notes} and its DTO {@link NotesDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface NotesMapper extends EntityMapper<NotesDTO, Notes> {
    @Mapping(target = "user", source = "user", qualifiedByName = "login")
    NotesDTO toDto(Notes s);
}

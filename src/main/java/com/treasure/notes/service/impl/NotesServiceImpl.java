package com.treasure.notes.service.impl;

import com.treasure.notes.domain.Notes;
import com.treasure.notes.repository.NotesRepository;
import com.treasure.notes.service.NotesService;
import com.treasure.notes.service.UserService;
import com.treasure.notes.service.dto.NotesDTO;
import com.treasure.notes.service.dto.UserDTO;
import com.treasure.notes.service.mapper.NotesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link Notes}.
 */
@Service
@Transactional
public class NotesServiceImpl implements NotesService {

    private final Logger log = LoggerFactory.getLogger(NotesServiceImpl.class);

    private final NotesRepository notesRepository;

    private final NotesMapper notesMapper;

    private final UserService userService;

    public NotesServiceImpl(NotesRepository notesRepository,
                            NotesMapper notesMapper,
                            UserService userService) {
        this.notesRepository = notesRepository;
        this.notesMapper = notesMapper;
        this.userService = userService;
    }

    @Override
    public NotesDTO save(NotesDTO notesDTO) {
        log.debug("Request to save Notes : {}", notesDTO);
        userService.getUser().ifPresent(notesDTO::setUser);
        Notes notes = notesMapper.toEntity(notesDTO);
        notes = notesRepository.save(notes);
        return notesMapper.toDto(notes);
    }

    @Override
    public Optional<NotesDTO> partialUpdate(NotesDTO notesDTO) {
        log.debug("Request to partially update Notes : {}", notesDTO);
        userService.getUser().ifPresent(notesDTO::setUser);
        return notesRepository
            .findById(notesDTO.getId())
            .map(
                existingNotes -> {
                    notesMapper.partialUpdate(existingNotes, notesDTO);
                    return existingNotes;
                }
            )
            .map(notesRepository::save)
            .map(notesMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotesDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Notes");
        return notesRepository.findAll(pageable).map(notesMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotesDTO> findOne(Long id) {
        log.debug("Request to get Notes : {}", id);
        return notesRepository.findById(id).map(notesMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Notes : {}", id);
        notesRepository.deleteById(id);
    }
}

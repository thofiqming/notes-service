package com.treasure.notes.service;

import com.treasure.notes.service.dto.NotesDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.treasure.notes.domain.Notes}.
 */
public interface NotesService {
    /**
     * Save a notes.
     *
     * @param notesDTO the entity to save.
     * @return the persisted entity.
     */
    NotesDTO save(NotesDTO notesDTO);

    /**
     * Partially updates a notes.
     *
     * @param notesDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<NotesDTO> partialUpdate(NotesDTO notesDTO);

    /**
     * Get all the notes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<NotesDTO> findAll(Pageable pageable);

    /**
     * Get the "id" notes.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<NotesDTO> findOne(Long id);

    /**
     * Delete the "id" notes.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}

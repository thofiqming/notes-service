package com.treasure.notes.web.rest;

import com.treasure.notes.repository.NotesRepository;
import com.treasure.notes.security.SecurityUtils;
import com.treasure.notes.service.NotesQueryService;
import com.treasure.notes.service.NotesService;
import com.treasure.notes.service.criteria.NotesCriteria;
import com.treasure.notes.service.dto.NotesDTO;
import com.treasure.notes.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.treasure.notes.domain.Notes}.
 */
@RestController
@RequestMapping("/api")
public class NotesResource {

    private final Logger log = LoggerFactory.getLogger(NotesResource.class);

    private static final String ENTITY_NAME = "notes";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final NotesService notesService;

    private final NotesRepository notesRepository;

    private final NotesQueryService notesQueryService;

    public NotesResource(NotesService notesService, NotesRepository notesRepository, NotesQueryService notesQueryService) {
        this.notesService = notesService;
        this.notesRepository = notesRepository;
        this.notesQueryService = notesQueryService;
    }

    /**
     * {@code POST  /notes} : Create a new notes.
     *
     * @param notesDTO the notesDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new notesDTO, or with status {@code 400 (Bad Request)} if the notes has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/notes")
    public ResponseEntity<NotesDTO> createNotes(@Valid @RequestBody NotesDTO notesDTO) throws URISyntaxException {
        log.debug("REST request to save Notes : {}", notesDTO);
        if (notesDTO.getId() != null) {
            throw new BadRequestAlertException("A new notes cannot already have an ID", ENTITY_NAME, "idexists");
        }
        NotesDTO result = notesService.save(notesDTO);
        return ResponseEntity
            .created(new URI("/api/notes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /notes/:id} : Updates an existing notes.
     *
     * @param id the id of the notesDTO to save.
     * @param notesDTO the notesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated notesDTO,
     * or with status {@code 400 (Bad Request)} if the notesDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the notesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/notes/{id}")
    public ResponseEntity<NotesDTO> updateNotes(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody NotesDTO notesDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Notes : {}, {}", id, notesDTO);
        if (notesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, notesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!notesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        NotesDTO result = notesService.save(notesDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, notesDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /notes/:id} : Partial updates given fields of an existing notes, field will ignore if it is null
     *
     * @param id the id of the notesDTO to save.
     * @param notesDTO the notesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated notesDTO,
     * or with status {@code 400 (Bad Request)} if the notesDTO is not valid,
     * or with status {@code 404 (Not Found)} if the notesDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the notesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/notes/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<NotesDTO> partialUpdateNotes(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody NotesDTO notesDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Notes partially : {}, {}", id, notesDTO);
        if (notesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, notesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!notesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NotesDTO> result = notesService.partialUpdate(notesDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, notesDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /notes} : get all the notes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of notes in body.
     */
    @GetMapping("/notes")
    public ResponseEntity<List<NotesDTO>> getAllNotes(NotesCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Notes by criteria: {}", criteria);
        Page<NotesDTO> page = notesQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /notes/count} : count all the notes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/notes/count")
    public ResponseEntity<Long> countNotes(NotesCriteria criteria) {
        log.debug("REST request to count Notes by criteria: {}", criteria);
        return ResponseEntity.ok().body(notesQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /notes/:id} : get the "id" notes.
     *
     * @param id the id of the notesDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the notesDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/notes/{id}")
    public ResponseEntity<NotesDTO> getNotes(@PathVariable Long id) {
        log.debug("REST request to get Notes : {}", id);
        Optional<NotesDTO> notesDTO = notesService.findOne(id);
        return ResponseUtil.wrapOrNotFound(notesDTO);
    }

    /**
     * {@code DELETE  /notes/:id} : delete the "id" notes.
     *
     * @param id the id of the notesDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNotes(@PathVariable Long id) {
        log.debug("REST request to delete Notes : {}", id);
        notesService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

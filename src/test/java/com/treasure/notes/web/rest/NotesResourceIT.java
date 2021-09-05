package com.treasure.notes.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.treasure.notes.IntegrationTest;
import com.treasure.notes.domain.Notes;
import com.treasure.notes.domain.User;
import com.treasure.notes.repository.NotesRepository;
import com.treasure.notes.service.criteria.NotesCriteria;
import com.treasure.notes.service.dto.NotesDTO;
import com.treasure.notes.service.mapper.NotesMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link NotesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class NotesResourceIT {

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/notes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private NotesMapper notesMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNotesMockMvc;

    private Notes notes;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Notes createEntity(EntityManager em) {
        Notes notes = new Notes().content(DEFAULT_CONTENT);
        return notes;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Notes createUpdatedEntity(EntityManager em) {
        Notes notes = new Notes().content(UPDATED_CONTENT);
        return notes;
    }

    @BeforeEach
    public void initTest() {
        notes = createEntity(em);
    }

    @Test
    @Transactional
    void createNotes() throws Exception {
        int databaseSizeBeforeCreate = notesRepository.findAll().size();
        // Create the Notes
        NotesDTO notesDTO = notesMapper.toDto(notes);
        restNotesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(notesDTO)))
            .andExpect(status().isCreated());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeCreate + 1);
        Notes testNotes = notesList.get(notesList.size() - 1);
        assertThat(testNotes.getContent()).isEqualTo(DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    void createNotesWithExistingId() throws Exception {
        // Create the Notes with an existing ID
        notes.setId(1L);
        NotesDTO notesDTO = notesMapper.toDto(notes);

        int databaseSizeBeforeCreate = notesRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restNotesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(notesDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkContentIsRequired() throws Exception {
        int databaseSizeBeforeTest = notesRepository.findAll().size();
        // set the field null
        notes.setContent(null);

        // Create the Notes, which fails.
        NotesDTO notesDTO = notesMapper.toDto(notes);

        restNotesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(notesDTO)))
            .andExpect(status().isBadRequest());

        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllNotes() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get all the notesList
        restNotesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notes.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)));
    }

    @Test
    @Transactional
    void getNotes() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get the notes
        restNotesMockMvc
            .perform(get(ENTITY_API_URL_ID, notes.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(notes.getId().intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT));
    }

    @Test
    @Transactional
    void getNotesByIdFiltering() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        Long id = notes.getId();

        defaultNotesShouldBeFound("id.equals=" + id);
        defaultNotesShouldNotBeFound("id.notEquals=" + id);

        defaultNotesShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultNotesShouldNotBeFound("id.greaterThan=" + id);

        defaultNotesShouldBeFound("id.lessThanOrEqual=" + id);
        defaultNotesShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllNotesByContentIsEqualToSomething() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get all the notesList where content equals to DEFAULT_CONTENT
        defaultNotesShouldBeFound("content.equals=" + DEFAULT_CONTENT);

        // Get all the notesList where content equals to UPDATED_CONTENT
        defaultNotesShouldNotBeFound("content.equals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllNotesByContentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get all the notesList where content not equals to DEFAULT_CONTENT
        defaultNotesShouldNotBeFound("content.notEquals=" + DEFAULT_CONTENT);

        // Get all the notesList where content not equals to UPDATED_CONTENT
        defaultNotesShouldBeFound("content.notEquals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllNotesByContentIsInShouldWork() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get all the notesList where content in DEFAULT_CONTENT or UPDATED_CONTENT
        defaultNotesShouldBeFound("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT);

        // Get all the notesList where content equals to UPDATED_CONTENT
        defaultNotesShouldNotBeFound("content.in=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllNotesByContentIsNullOrNotNull() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get all the notesList where content is not null
        defaultNotesShouldBeFound("content.specified=true");

        // Get all the notesList where content is null
        defaultNotesShouldNotBeFound("content.specified=false");
    }

    @Test
    @Transactional
    void getAllNotesByContentContainsSomething() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get all the notesList where content contains DEFAULT_CONTENT
        defaultNotesShouldBeFound("content.contains=" + DEFAULT_CONTENT);

        // Get all the notesList where content contains UPDATED_CONTENT
        defaultNotesShouldNotBeFound("content.contains=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllNotesByContentNotContainsSomething() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        // Get all the notesList where content does not contain DEFAULT_CONTENT
        defaultNotesShouldNotBeFound("content.doesNotContain=" + DEFAULT_CONTENT);

        // Get all the notesList where content does not contain UPDATED_CONTENT
        defaultNotesShouldBeFound("content.doesNotContain=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllNotesByUserIsEqualToSomething() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        notes.setUser(user);
        notesRepository.saveAndFlush(notes);
        Long userId = user.getId();

        // Get all the notesList where user equals to userId
        defaultNotesShouldBeFound("userId.equals=" + userId);

        // Get all the notesList where user equals to (userId + 1)
        defaultNotesShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultNotesShouldBeFound(String filter) throws Exception {
        restNotesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notes.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)));

        // Check, that the count call also returns 1
        restNotesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultNotesShouldNotBeFound(String filter) throws Exception {
        restNotesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restNotesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingNotes() throws Exception {
        // Get the notes
        restNotesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewNotes() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        int databaseSizeBeforeUpdate = notesRepository.findAll().size();

        // Update the notes
        Notes updatedNotes = notesRepository.findById(notes.getId()).get();
        // Disconnect from session so that the updates on updatedNotes are not directly saved in db
        em.detach(updatedNotes);
        updatedNotes.content(UPDATED_CONTENT);
        NotesDTO notesDTO = notesMapper.toDto(updatedNotes);

        restNotesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notesDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(notesDTO))
            )
            .andExpect(status().isOk());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
        Notes testNotes = notesList.get(notesList.size() - 1);
        assertThat(testNotes.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void putNonExistingNotes() throws Exception {
        int databaseSizeBeforeUpdate = notesRepository.findAll().size();
        notes.setId(count.incrementAndGet());

        // Create the Notes
        NotesDTO notesDTO = notesMapper.toDto(notes);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notesDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(notesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchNotes() throws Exception {
        int databaseSizeBeforeUpdate = notesRepository.findAll().size();
        notes.setId(count.incrementAndGet());

        // Create the Notes
        NotesDTO notesDTO = notesMapper.toDto(notes);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(notesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNotes() throws Exception {
        int databaseSizeBeforeUpdate = notesRepository.findAll().size();
        notes.setId(count.incrementAndGet());

        // Create the Notes
        NotesDTO notesDTO = notesMapper.toDto(notes);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(notesDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateNotesWithPatch() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        int databaseSizeBeforeUpdate = notesRepository.findAll().size();

        // Update the notes using partial update
        Notes partialUpdatedNotes = new Notes();
        partialUpdatedNotes.setId(notes.getId());

        partialUpdatedNotes.content(UPDATED_CONTENT);

        restNotesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotes.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedNotes))
            )
            .andExpect(status().isOk());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
        Notes testNotes = notesList.get(notesList.size() - 1);
        assertThat(testNotes.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void fullUpdateNotesWithPatch() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        int databaseSizeBeforeUpdate = notesRepository.findAll().size();

        // Update the notes using partial update
        Notes partialUpdatedNotes = new Notes();
        partialUpdatedNotes.setId(notes.getId());

        partialUpdatedNotes.content(UPDATED_CONTENT);

        restNotesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotes.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedNotes))
            )
            .andExpect(status().isOk());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
        Notes testNotes = notesList.get(notesList.size() - 1);
        assertThat(testNotes.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void patchNonExistingNotes() throws Exception {
        int databaseSizeBeforeUpdate = notesRepository.findAll().size();
        notes.setId(count.incrementAndGet());

        // Create the Notes
        NotesDTO notesDTO = notesMapper.toDto(notes);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, notesDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(notesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNotes() throws Exception {
        int databaseSizeBeforeUpdate = notesRepository.findAll().size();
        notes.setId(count.incrementAndGet());

        // Create the Notes
        NotesDTO notesDTO = notesMapper.toDto(notes);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(notesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNotes() throws Exception {
        int databaseSizeBeforeUpdate = notesRepository.findAll().size();
        notes.setId(count.incrementAndGet());

        // Create the Notes
        NotesDTO notesDTO = notesMapper.toDto(notes);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotesMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(notesDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Notes in the database
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteNotes() throws Exception {
        // Initialize the database
        notesRepository.saveAndFlush(notes);

        int databaseSizeBeforeDelete = notesRepository.findAll().size();

        // Delete the notes
        restNotesMockMvc
            .perform(delete(ENTITY_API_URL_ID, notes.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Notes> notesList = notesRepository.findAll();
        assertThat(notesList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

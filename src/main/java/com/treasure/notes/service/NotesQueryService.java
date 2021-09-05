package com.treasure.notes.service;

import com.treasure.notes.domain.Notes;
import com.treasure.notes.domain.Notes_;
import com.treasure.notes.domain.User_;
import com.treasure.notes.repository.NotesRepository;
import com.treasure.notes.service.criteria.NotesCriteria;
import com.treasure.notes.service.dto.NotesDTO;
import com.treasure.notes.service.mapper.NotesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import tech.jhipster.service.filter.LongFilter;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Notes} entities in the database.
 * The main input is a {@link NotesCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link NotesDTO} or a {@link Page} of {@link NotesDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class NotesQueryService extends QueryService<Notes> {

    private final Logger log = LoggerFactory.getLogger(NotesQueryService.class);

    private final NotesRepository notesRepository;

    private final NotesMapper notesMapper;

    private final UserService userService;

    public NotesQueryService(NotesRepository notesRepository,
                             NotesMapper notesMapper,
                             UserService userService) {
        this.notesRepository = notesRepository;
        this.notesMapper = notesMapper;
        this.userService = userService;
    }

    /**
     * Return a {@link List} of {@link NotesDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<NotesDTO> findByCriteria(NotesCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Notes> specification = createSpecification(criteria);
        return notesMapper.toDto(notesRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link NotesDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<NotesDTO> findByCriteria(NotesCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Notes> specification = createSpecification(criteria);
        return notesRepository.findAll(specification, page).map(notesMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(NotesCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Notes> specification = createSpecification(criteria);
        return notesRepository.count(specification);
    }

    /**
     * Function to convert {@link NotesCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Notes> createSpecification(NotesCriteria criteria) {
        LongFilter userIdFilter = new LongFilter();
        userService.getUser().ifPresent(user -> {
            userIdFilter.setEquals(user.getId());
            criteria.setUserId(userIdFilter);
        });
        Specification<Notes> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Notes_.id));
            }
            if (criteria.getContent() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContent(), Notes_.content));
            }
            if (criteria.getUserId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getUserId(), root -> root.join(Notes_.user, JoinType.LEFT).get(User_.id))
                    );
            }
        }
        return specification;
    }
}

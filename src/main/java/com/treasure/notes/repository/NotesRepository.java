package com.treasure.notes.repository;

import com.treasure.notes.domain.Notes;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Notes entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotesRepository extends JpaRepository<Notes, Long>, JpaSpecificationExecutor<Notes> {
    @Query(value = "SELECT * FROM Notes notes WHERE user_id = ?1 ORDER BY ?#{#pageable}",
        countQuery = "SELECT count(*) FROM USERS WHERE user_id = ?1",
        nativeQuery = true)
    Page<Notes> findByUserIsCurrentUser(Long userId, Pageable pageable);

}

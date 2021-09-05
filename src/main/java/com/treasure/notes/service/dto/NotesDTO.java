package com.treasure.notes.service.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.treasure.notes.domain.Notes} entity.
 */
public class NotesDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 3000)
    private String content;

    private UserDTO user;

    private Date createdDate;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotesDTO)) {
            return false;
        }

        NotesDTO notesDTO = (NotesDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, notesDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotesDTO{" +
            "id=" + getId() +
            ", content='" + getContent() + "'" +
            ", user=" + getUser() +
            "}";
    }
}

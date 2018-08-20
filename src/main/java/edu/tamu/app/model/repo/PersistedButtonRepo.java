/*
 * PersistedButtonRepo.java
 *
 * Version:
 *     $Id$
 *
 * Revisions:
 *     $Log$
 */
package edu.tamu.app.model.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.tamu.app.model.PersistedButton;

/**
 * Repository for PersistedButton.
 *
 */
@Repository
public interface PersistedButtonRepo extends JpaRepository<PersistedButton, Long>, PersistedButtonRepoCustom {

}

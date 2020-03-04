package mops.klausurzulassung.database;

import mops.klausurzulassung.model.Modul;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModulRepository extends JpaRepository<Modul, String> {
}

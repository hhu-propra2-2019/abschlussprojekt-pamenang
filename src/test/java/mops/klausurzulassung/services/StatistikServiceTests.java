package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.ModulStatistiken;
import mops.klausurzulassung.repositories.StatistikRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatistikServiceTests {

  private StatistikRepository statistikRepository;
  private StatistikService statistikService;

  @BeforeEach
  void setUp() {
    this.statistikRepository = mock(StatistikRepository.class);
    this.statistikService = new StatistikService(statistikRepository);
  }

  @Test
  void modulInDatabaseModulIsPresent() {
    String frist = "01/02/2222";
    ModulStatistiken modul = new ModulStatistiken(9L, 1L, frist, 180L, 150L);
    when(statistikRepository.findByFristAndModulId(any(), any())).thenReturn(Optional.of(modul));

    Long result = statistikService.modulInDatabase(frist, 1L);

    assertEquals(9L, result);
  }

  @Test
  void modulInDatabaseModulIsNotPresent() {
    String frist = "01/02/2222";
    when(statistikRepository.findByFristAndModulId(any(), any())).thenReturn(Optional.empty());

    Long result = statistikService.modulInDatabase(frist, 1L);

    assertNull(result);
  }
}

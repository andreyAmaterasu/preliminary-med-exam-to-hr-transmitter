package ru.kostromin.preliminarymedexamtohrtransmitter.data.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.entity.HstMedResultSending;

/**
 * Репозитория для получения данных о переданных случаях
 */
public interface HstMedResultSendingRepository extends JpaRepository<HstMedResultSending, Integer> {

  @Query(value = "select top 1 rs.DateChange "
      + "from hst_MedResultSending rs "
      + "where rs.SendingStatus = 1 order by rs.DateChange desc", nativeQuery = true)
  Optional<LocalDateTime> findLastDateChangeBySendingStatusIsTrue();

  Optional<HstMedResultSending> findFirstByTapGuidAndPid(String tapGuid, Integer pid);
}

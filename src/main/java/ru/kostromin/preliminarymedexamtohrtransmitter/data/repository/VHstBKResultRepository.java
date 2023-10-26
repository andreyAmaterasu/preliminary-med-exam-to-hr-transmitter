package ru.kostromin.preliminarymedexamtohrtransmitter.data.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.entity.VHstBKResult;

/**
 * Репозитория для получения данных о случаях, которые необходимо передать
 */
@Repository
public interface VHstBKResultRepository extends JpaRepository<VHstBKResult, String> {

  @Query("select r from VHstBKResult r "
      + "left join HstMedResultSending rs on rs.tapGuid = r.tapGuid "
      + "where (r.dateCreate between :dateFrom and :dateTo and not (coalesce(rs.sendingStatus, false) = true "
      + "and r.result = coalesce(rs.result, -1))) or rs.sendingStatus = false")
  List<VHstBKResult> findAllByDateCreateBetweenAndSendingStatusFalse(LocalDateTime dateFrom, LocalDateTime dateTo);
}

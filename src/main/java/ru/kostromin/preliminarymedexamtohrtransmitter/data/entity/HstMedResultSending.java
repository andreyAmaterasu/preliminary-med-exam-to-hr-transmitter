package ru.kostromin.preliminarymedexamtohrtransmitter.data.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Представление результата передачи случая
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hst_MedResultSending")
public class HstMedResultSending {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "MedResultSendingId")
  private Long medResultSendingId;

  @Column(name = "rf_TAPGUID")
  private String tapGuid;

  @Column(name = "MedMessageId")
  private String messageId;

  @Column(name = "PID")
  private Integer pid;

  @Column(name = "DateChange")
  private LocalDateTime dateChange;

  @Column(name = "MedResult")
  private Integer result;

  @Column(name = "SendingStatus")
  private Boolean sendingStatus;
}

package ru.kostromin.preliminarymedexamtohrtransmitter.data.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Представление случая для передачи
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "V_hst_BK_Result")
public class VHstBKResult {

  @Id
  @Column(name = "TAPGUID")
  private String tapGuid;

  @Column(name = "PID")
  private Integer pid;

  @Column(name = "Date_b")
  private LocalDateTime dateBegin;

  @Column(name = "Date_e")
  private LocalDateTime dateEnd;

  @Column(name = "x_DateTime")
  private LocalDateTime dateCreate;

  @Column(name = "Result")
  private Integer result;
}

# preliminary-med-exam-to-hr-transmitter

SOAP клиент для передачи случаев в систему "Босс Кадровик"

### Используемое ПО
- Java 17
- Spring Boot 2.7.12
- MSSQL-JDBC 12.2.0.jre11

## Сборка
```sh
mvn clean install 
```

### Описание сервиса
Сервис **preliminary-med-exam-to-hr-transmitter** это сервис-scheduler, выполняющий работы по расписанию. 
Сервис имеет одну работу по передаче случаев во внешнюю систему.

### Описание параметров конфигурации (application.yaml)
Настройки подключения к БД: \
<span style="color:green;">datasource.url</span> - адрес подключения к БД \
<span style="color:green;">datasource.username</span> - имя пользователя БД \
<span style="color:green;">datasource.password</span> - пароль пользователя БД

Настройка времени выполнения работы: \
<span style="color:green;">job.cron</span> - крон работы \

Общие настройки для работы сервиса: \
<span style="color:green;">app.date-from</span> - дата начала периода выгрузки \
<span style="color:green;">app.date-to</span> - дата окончания периода выгрузки \
<span style="color:green;">app.kind-interval-time</span> - стратегия инициализации дат периода выгрузки \
<span style="color:green;">app.services.bk.url</span> - url адрес до внешней системы \
<span style="color:green;">app.services.bk.context-path</span> - контекстный путь JAXB

### Алгоритм работы сервиса
**1 -** Обращение к представлению V_hst_BK_Result и выгрузка случаев, которые попадают под критерии выгрузки.
Критерии выгрузки представляют собой диапазон дат, которые инициализируются в момент срабатывания job
по следующей стратегии, основанной на настройке kind-interval-time (+ случаи из журнала hst_MedResultSending с StatusSending = false):
- 1: dateFrom = начало текущего дня, dateTo = дата срабатывания job;
- 2: dateFrom = начало вчерашнего дня, dateTo = конец вчерашнего дня;
- 3: dateFrom = дата последнего успешно отправленного случая, dateTo = дата срабатывания job. 
Если случаи ранее не были отправлены или отправлены неуспешно, то dateFrom = дата запуска сервиса;
- null: dateFrom/dateTo из конфига.

**2 -** Если случаи найдены, то выполняется шаг 3. Иначе запрос не формируется и не отправляется, 
о чем сообщается в логах; \
**3 -** Происходит формирование запроса на основании полученных случаев. Происходит сопоставление представления 
случая с представлением запроса:

| Представление запроса |                                                                                        Представление случая                                                                                         |
|:---------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|       messageID       |                                                                       select max(MedMessageID) + 1 from hst_MedResultSending                                                                        |
|         fromd         |                                                                                               Date_b                                                                                                |
|          tod          |                                                                                               Date_e                                                                                                |
|          pid          |                                                                                                 PID                                                                                                 |
|      description      |                                                                                               Result                                                                                                |
|        status         | ins - если нет ранее добавленной записи по двум идентификаторам rf_TAPGUID и PID в таблице hst_MedResultSending;<br/> upd - если есть существующая запись по двум идентификаторам rf_TAPGUID и PID. |

В результате будет сформирован SOAP-запрос, например:
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Header/>
  <SOAP-ENV:Body>
    <ns3:MT_MedicalExamRequest xmlns:ns3="http://test.ru/BOSS/MIS">
      <messageID>1</messageID>
      <row>
        <fromd>2021-10-15T00:00</fromd>
        <tod>2222-01-01T00:00</tod>
        <pid>2</pid>
        <description>4</description>
        <status>ins</status>
      </row>
      <row>
        <fromd>2022-06-07T00:00</fromd>
        <tod>2222-01-01T00:00</tod>
        <pid>1</pid>
        <description>4</description>
        <status>ins</status>
      </row>
    </ns3:MT_MedicalExamRequest>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
**4 -** Сформированный запрос отправляется во внешнюю систему. После отправки запроса формируется результат. \
Запрос может быть отправлен успешно. В таком случае тело ответа будет пустым и HTTP status code будет равен 200:
```xml
<SOAP:Envelope xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP:Header/>
  <SOAP:Body/>
</SOAP:Envelope>
```
В представлении результата, в свойстве SendingResult указывается значение true, например:
```json
{
  "medResultSendingId": 1,
  "tapGuid": "69FD6E8B-2D01-4BAC-A1E3-C1B0B7684EE4",
  "messageId": 1,
  "pid": 2,
  "dateChange": "2023-06-27T10:27:25.422847",
  "result": 4,
  "sendingStatus": true
}
```
Также запрос может быть отправлен неуспешно, HTTP status code будет равен 40x или 50x. При этом будет выброшено и 
перехвачено исключение, на основании которого в свойстве SendingResult указывается значение false, например: \
<sub>_* Такой случай будет отправлен повторно при следующем срабатывании job_</sub>
```json
{
  "medResultSendingId": 1,
  "tapGuid": "69FD6E8B-2D01-4BAC-A1E3-C1B0B7684EE4",
  "messageId": 1,
  "pid": 2,
  "dateChange": "2023-06-27T10:27:25.422847",
  "result": 4,
  "sendingStatus": false
}
```

**5 -** Сформированной результат сохраняется в журнал hst_MedResultSending. Если в журнале нет ранее 
добавленной записи по двум идентификаторам rf_TAPGUID и PID, то добавляется новая запись. Иначе
обновляются поля MedMessageID, DateChange, MedResult, SendingStatus существующей записи в журнале.
--Author: Скоков Денис
--Create date: 14.06.2023
--Task: #120549
--Description: После получения ответа от Шины/Внешней системы добавить новую запись в таблицу hst_MedResultSending в независимости от того, была она до того отправлена в БК или нет
--Region: Алроса

IF OBJECT_ID('hst_MedResultSending', 'U') IS NULL
CREATE TABLE hst_MedResultSending (MedResultSendingID int IDENTITY(1, 1) NOT NULL PRIMARY KEY
								  ,rf_TAPGUID uniqueidentifier NOT NULL	--V_hst_BK_Result.TAPGUID
								  ,MedMessageID	int NOT NULL
								  ,PID int NOT NULL --V_hst_BK_Result.PID
								  ,DateChange datetime NOT NULL DEFAULT '19000101' --Дата и время запроса
								  ,MedResult int NOT NULL --V_hst_BK_Result.Result
								  ,SendingStatus bit NOT NULL DEFAULT 0); --1 - статус отправлен, 0 - не отправлен

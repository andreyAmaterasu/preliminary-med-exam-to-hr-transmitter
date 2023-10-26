USE [mis_alrosa]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


IF OBJECT_ID('V_hst_BK_Result') IS NOT NULL DROP VIEW [dbo].[V_hst_BK_Result]
GO
CREATE VIEW [dbo].[V_hst_BK_Result]
AS
 -------------------------------
-- Author:		Никита Гончаров
-- Create date: 29.05.2023
-- Description:	Добавление представления для сервиса передачи результатов из МИС в БК.
-- Region:		Якутия (Алроса)

-- Update:		15.06.2023		Денис Скоков		#122034
-- Update:		19.06.2023		Александр Мостовских		#122034-9
 -------------------------------
select distinct
	   t.UGUID as TAPGUID,
	   em.ExternalWorkerID as PID, 
	   t.DateTAP as Date_b, 
	   t.DateClose as Date_e, 
	   case
			when scr.CODE = '391' then 1
			when scr.CODE = '392' then 2
			when scr.CODE = '393' then 3
			when scr.CODE = '395' then 5
			else 4 
	   end as Result,
	   t.DateCreateTAP as x_DateTime
from hlt_TAP t
     join oms_kl_DDService dds On dds.kl_DDServiceID=t.rf_kl_DDServiceID
     join hst_EnterpriseMKAB em On em.rf_MKABID=t.rf_MKABID
	 join hlt_MKAB hm on em.rf_MKABID = hm.MKABID 
	 join oms_kl_StatCureResult scr ON scr.kl_StatCureResultID = t.rf_kl_StatCureResultID
	 join oms_kl_ProfitType okp on t.rf_kl_ProfitTypeID = okp.kl_ProfitTypeID 
where okp.CODE !=1
  and dds.CODE in ('ПРМО')
  and t.IsClosed = 1
  and em.ExternalWorkerID  > 0;
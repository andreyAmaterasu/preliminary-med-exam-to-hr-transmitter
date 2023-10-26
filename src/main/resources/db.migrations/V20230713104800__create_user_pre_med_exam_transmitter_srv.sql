begin
    TRY declare @dbname nvarchar(30) = 'mis_alrosa' ,	-- имя БД
    @dbuser nvarchar(30) = 'pre_med_exam_transmitter_srv' ,				-- имя учетной записи / логин
    @pwd varchar(20) ,
    @pwdchars varchar(100) ,
    @pwdlen int,
    @counter int
select
    @pwdchars = '0123456789qwertyuiopasdfghjklzxcvbnm9876543210QWERTYUIOPASDFGHJKLZXCVBNM9876543210',
    @pwdlen = rand()* 8 + 20,
    @counter = 0,
    @pwd = N'' while @counter<@pwdlen
begin
        select
    @pwd = @pwd + substring(@pwdchars, cast(rand()* len(@pwdchars)+ 1 as int) , 1),
    @counter = @counter + 1
end declare @SQL NVARCHAR(max)
select
    @SQL = (
    select
        '
USE [master]

CREATE LOGIN [' + @dbuser + '] WITH PASSWORD=N''' + @pwd + ''', DEFAULT_DATABASE=[master], CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF

USE [' + @dbname + ']

CREATE USER [' + @dbuser + '] FOR LOGIN [' + @dbuser + ']
ALTER USER [' + @dbuser + '] WITH DEFAULT_SCHEMA=[dbo]

GRANT SELECT ON [dbo].[V_hst_BK_Result] TO ' + @dbuser + '
GRANT SELECT, INSERT, UPDATE ON [dbo].[hst_MedResultSending] TO ' + @dbuser + '

    ' for xml path(''),
        type).value('.',
    'NVARCHAR(MAX)')
EXEC sys.sp_executesql @SQL 
PRINT 'В базе данных ' + @dbname + ' 
создан пользователь ' + @dbuser + ' 
с именем входа ' + @dbuser + ' 
и паролем ' + @pwd
end TRY
begin
    CATCH PRINT 'This is the error: ' + error_message()
end CATCH
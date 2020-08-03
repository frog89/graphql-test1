CREATE OR REPLACE package get_data_pkg as 
  function get_data(aSql in clob, aArgInfo in varchar2_200_table, aArgNumberList in number_table, aArgCharList in varchar2_200_table) return sys_refcursor;
end get_data_pkg;
/

create or replace package body get_data_pkg as 
  TYPE arg_rec is RECORD (
    valueStartIndex number,
    valueCount number,
    valueType varchar2(20),
    argName varchar2(20)
    );
  type arg_table is table of arg_rec;
  
  TYPE bind_rec is RECORD (
    valueType varchar2(20),
    varcharVal varchar2(200),
    varcharVal2 varchar2(200),
    varcharTableVal varchar2_200_table,
    numberVal varchar2(200),
    numberVal2 varchar2(200),
    numberTableVal number_table
    );
  type bind_table is table of bind_rec;
      
  function get_arg_table(aArgInfo in varchar2_200_table) return arg_table is
    tmpArgTable arg_table := arg_table();
    tmpArgRec arg_rec;
    tmpEmptyArgRec arg_rec;
    tmpArg varchar2(200);
    tmpToken varchar2(200);
    tmpToken2StartPos number;
    tmpToken3StartPos number;
    tmpToken4StartPos number;
  begin
    for i in 1..aArgInfo.count loop
      tmpArg := aArgInfo(i);
      tmpArgRec := tmpEmptyArgRec;
      
      tmpToken2StartPos := instr(tmpArg, '-', 1) + 1;
      tmpToken3StartPos := instr(tmpArg, '-', tmpToken2StartPos + 1) + 1;
      tmpToken4StartPos := instr(tmpArg, '-', tmpToken3StartPos + 1) + 1;
      
      tmpToken := substr(tmpArg, 1, tmpToken2StartPos - 2);
      tmpArgRec.valueStartIndex := to_number(tmpToken);
      
      tmpToken := substr(tmpArg, tmpToken2StartPos, tmpToken3StartPos - tmpToken2StartPos - 1);
      tmpArgRec.valueCount := to_number(tmpToken);
      
      tmpToken := substr(tmpArg, tmpToken3StartPos, tmpToken4StartPos - tmpToken3StartPos - 1);
      tmpArgRec.valueType := tmpToken;
      
      tmpToken := substr(tmpArg, tmpToken4StartPos);
      tmpArgRec.argName := tmpToken;
      
      tmpArgTable.extend;
      tmpArgTable(tmpArgTable.count) := tmpArgRec;
    end loop;
    
    return tmpArgTable;
    exception when others then
      raise_application_error(-20001,  dbms_utility.format_error_backtrace || 
        'Error persing: ' || tmpArg || '-' || tmpToken || '(' || tmpToken2StartPos || ',' || tmpToken3StartPos || ',' || tmpToken4StartPos || '): ' || sqlerrm);
  end get_arg_table;
  
  function get_bind_table(aArgTable in arg_table, aArgNumberList in number_table, aArgCharList in varchar2_200_table) return bind_table is
    tmpBindTable bind_table := bind_table();
    tmpBindRec bind_rec;
    tmpEmptyBindRec bind_rec;
    tmpArgRec arg_rec;
    tmpCharTable varchar2_200_table;
    tmpNumberTable number_table;
  begin 
    for i in 1..aArgTable.count loop
      tmpArgRec := aArgTable(i);
      
      tmpBindRec := tmpEmptyBindRec;
      tmpBindRec.valueType := tmpArgRec.valueType;
      
      -- String, StringBetween, StringTable, Number, NumberBetween, NumberTable
      if tmpBindRec.valueType = 'String' then
        tmpBindRec.varcharVal := aArgCharList(tmpArgRec.valueStartIndex);
      elsif tmpBindRec.valueType = 'StringBetween' then
        tmpBindRec.varcharVal := aArgCharList(tmpArgRec.valueStartIndex);
        tmpBindRec.varcharVal2 := aArgCharList(tmpArgRec.valueStartIndex + 1);
      elsif tmpBindRec.valueType = 'Number' then
        tmpBindRec.numberVal := aArgNumberList(tmpArgRec.valueStartIndex);
      elsif tmpBindRec.valueType = 'NumberBetween' then
        tmpBindRec.numberVal := aArgNumberList(tmpArgRec.valueStartIndex);
        tmpBindRec.numberVal2 := aArgNumberList(tmpArgRec.valueStartIndex + 1);
      elsif tmpBindRec.valueType = 'StringTable' then
        tmpCharTable := varchar2_200_table();
        for j in 1..tmpArgRec.valueCount loop
          tmpCharTable.extend;
          tmpCharTable(tmpCharTable.count) := aArgCharList(tmpArgRec.valueStartIndex + j - 1);
        end loop;
        tmpBindRec.varcharTableVal := tmpCharTable;
      elsif tmpBindRec.valueType = 'NumberTable' then
        tmpNumberTable := number_table();
        for j in 1..tmpArgRec.valueCount loop
          tmpNumberTable.extend;
          tmpNumberTable(tmpNumberTable.count) := aArgNumberList(tmpArgRec.valueStartIndex + j - 1);
        end loop;
        tmpBindRec.numberTableVal := tmpNumberTable;
      else
        raise_application_error(-20001, 'Wrong value type given: ' || tmpBindRec.valueType);
      end if;
      
      tmpBindTable.extend;
      tmpBindTable(tmpBindTable.count) := tmpBindRec;
    end loop;
    return tmpBindTable;
  end get_bind_table;
  
  function getCur(aSql in clob, aBindTable in bind_table) return sys_refcursor is
    tmpBindRec bind_rec;
    tmpCurNum number;  
    tmpDummy pls_integer;  
    tmpCur sys_refcursor;  
  begin
    tmpCurNum := DBMS_SQL.open_cursor;  
    DBMS_SQL.parse(tmpCurNum, aSql, DBMS_SQL.native);  
  
    for i in 1..aBindTable.count loop
      tmpBindRec := aBindTable(i);
      
      -- String, StringBetween, StringTable, Number, NumberBetween, NumberTable
      if tmpBindRec.valueType = 'String' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.varcharVal);
      elsif tmpBindRec.valueType = 'StringBetween' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i) || '_v1', tmpBindRec.varcharVal);
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i) || '_v2', tmpBindRec.varcharVal2);
      elsif tmpBindRec.valueType = 'Number' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.numberVal);
      elsif tmpBindRec.valueType = 'NumberBetween' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i) || '_v1', tmpBindRec.numberVal);
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i) || '_v2', tmpBindRec.numberVal2);
      elsif tmpBindRec.valueType = 'StringTable' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.varcharTableVal);
      elsif tmpBindRec.valueType = 'NumberTable' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.numberTableVal);
      else
        raise_application_error(-20001, 'Wrong value type given: ' || tmpBindRec.valueType);
      end if;

    end loop;
  
    tmpDummy := DBMS_SQL.EXECUTE(tmpCurNum);  
    tmpCur := DBMS_SQL.to_refcursor(tmpCurNum);  
    return tmpCur;  
  end getCur;

  function num_tbl_to_clob(aTbl in number_table, aSeparator in varchar2, aMaxCount in number) return clob is
    tmpRet clob;
    tmpVal varchar2(100);
    tmpCount number := least(aMaxCount, aTbl.count);
  begin
    DBMS_LOB.CREATETEMPORARY(tmpRet, true, DBMS_LOB.SESSION);
    for i in 1..tmpCount loop
      if i > 1 then
        dbms_lob.writeappend(tmpRet, length(aSeparator), aSeparator);
      end if;
      tmpVal := to_char(aTbl(i));
      dbms_lob.writeappend(tmpRet, length(tmpVal), tmpVal);
    end loop;
    return tmpRet;
  end num_tbl_to_clob;

  function char_tbl_to_clob(aTbl in varchar2_200_table, aSeparator in varchar2, aMaxCount in number) return clob is
    tmpRet clob;
    tmpVal varchar2(200);
    tmpCount number := least(aMaxCount, aTbl.count);
  begin
    DBMS_LOB.CREATETEMPORARY(tmpRet, true, DBMS_LOB.SESSION);
    for i in 1..tmpCount loop
      if i > 1 then
        dbms_lob.writeappend(tmpRet, length(aSeparator), aSeparator);
      end if;
      dbms_lob.writeappend(tmpRet, length(aTbl(i)), aTbl(i));
    end loop;
    return tmpRet;
  end char_tbl_to_clob;
  
  function createLogArgs(aArgTable in arg_table, aBindTable in bind_table) return clob is
    tmpArgRec arg_rec;
    tmpBindRec bind_rec;
    tmpRet clob := '*** ArgInfo:' || chr(10);
  begin
    for i in 1..aArgTable.count loop
      tmpArgRec := aArgTable(i);
      tmpBindRec := aBindTable(i);
      
      tmpRet := tmpRet || '*** ' || tmpArgRec.argName || '(Value count=' || tmpArgRec.valueCount || '): ';
      if tmpArgRec.valueType = 'String' then
        tmpRet := tmpRet || tmpBindRec.varcharVal;
      elsif tmpArgRec.valueType = 'StringBetween' then
        tmpRet := tmpRet || tmpBindRec.varcharVal || ',' || tmpBindRec.varcharVal2;
      elsif tmpArgRec.valueType = 'StringTable' then
        tmpRet := char_tbl_to_clob(tmpBindRec.varcharTableVal, ',', 10);
      elsif tmpArgRec.valueType = 'Number' then
        tmpRet := tmpRet || tmpBindRec.numberVal;
      elsif tmpArgRec.valueType = 'NumberBetween' then
        tmpRet := tmpRet || tmpBindRec.numberVal || ',' || tmpBindRec.numberVal2;
      elsif tmpArgRec.valueType = 'NumberTable' then
        tmpRet := tmpRet || num_tbl_to_clob(tmpBindRec.numberTableVal, ',', 10);
      end if;
      tmpRet := tmpRet || chr(10);
    end loop;
    return tmpRet;
  end createLogArgs;
  
  function get_data(aSql in clob, aArgInfo in varchar2_200_table, aArgNumberList in number_table, aArgCharList in varchar2_200_table) return sys_refcursor is
    PRAGMA AUTONOMOUS_TRANSACTION;
    tmpCur sys_refcursor;
    tmpLastNamePart varchar2(200);
    tmpEmpIdTable number_table;
    tmpLogArgs clob;
    tmpLogArgsDetails clob;
    tmpSql clob;
    tmpTimeDiff number;
    tmpBindTable bind_table;
    tmpArgTable arg_table;
    tmpTimeStart number := dbms_utility.get_time;
  begin
    tmpArgTable := get_arg_table(aArgInfo);
    tmpBindTable := get_bind_table(tmpArgTable, aArgNumberList, aArgCharList);
    tmpTimeDiff := (dbms_utility.get_time - tmpTimeStart) * 10;
    tmpLogArgs := 'Time2Split[ms]:' || tmpTimeDiff;
    
    tmpTimeStart := dbms_utility.get_time;
    tmpLogArgsDetails := createLogArgs(tmpArgTable, tmpBindTable);
    tmpTimeDiff := (dbms_utility.get_time - tmpTimeStart) * 10; 
    tmpLogArgs := tmpLogArgs || chr(10) || 'Time2Log[ms]:' || tmpTimeDiff || chr(10) || tmpLogArgsDetails;

    insert into sql_log(log_date, sql, args) values (sysdate, aSql, tmpLogArgs);
    commit;
    
    tmpCur := getCur(aSql, tmpBindTable);
    return tmpCur;
  end get_data;

end get_data_pkg;
/